package com.minhden.minimal.application;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLConnection;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.minhden.minimal.util.BTree;
import com.minhden.minimal.util.BookMetadata;
import com.minhden.minimal.util.Entry;
import com.minhden.minimal.util.TreeNode;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import javafx.concurrent.Task;

public class BookManager {
    private static BookManager instance;

    private BTree bTree;

    private Map<String, List<Integer>> titleIndex;

    private Map<String, List<Integer>> bookshelfIndex;

    private BookManager() {
        this.bTree = new BTree(10);
        this.titleIndex = new HashMap<>();
        this.bookshelfIndex = new HashMap<>();
        loadCatalog();
    }

    public static BookManager getInstance() {
        if (instance == null) {
            instance = new BookManager();
        }

        return instance;
    }

    public void loadCatalog() {
        String[] columns;

        try {
            InputStream in = getClass().getResourceAsStream("/files/pg_catalog.csv");
            if (in == null) {
                throw new FileNotFoundException("Catalog not found.");
            }
            CSVReader csvReader = new CSVReader(new InputStreamReader(in));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            // Read the header line
            csvReader.readNext();

            // Process each line
            while ((columns = csvReader.readNext()) != null) {
                int id = Integer.parseInt(columns[0]);
                String type = columns[1];
                LocalDate issued = LocalDate.parse(columns[2], formatter);
                String title = columns[3];
                String language = columns[4];
                String authors = columns[5];
                List<String> subjects = Arrays.asList(columns[6].split(";"));
                List<String> locc = Arrays.asList(columns[7].split(";"));
                List<String> bookshelves = Arrays.asList(columns[8].split(";"));
                List<String> bookshelvesStrimmed = new ArrayList<>();
                for (String s : bookshelves) {
                    bookshelvesStrimmed.add(s.trim());
                }
                BookMetadata metadata = new BookMetadata(id, type, issued, title, language, authors, locc, subjects,
                        bookshelvesStrimmed);
                bTree.insert(new Entry<>(id, metadata));
                addTitleIndex(id, title);
                addBookshelfIndex(id, bookshelves);
                System.out.println("Indexed: " + id);
            }
            csvReader.close();
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }
    }

    public List<String> getBookshelves() {
        return new ArrayList<>(bookshelfIndex.keySet());
    }

    public List<String> getKeywordList() {
        return new ArrayList<>(titleIndex.keySet());
    }

    public BookMetadata searchById(int key) {
        TreeNode node = bTree.search(key);
        Entry<Integer, Object>[] entries = node.getEntries();
        for (Entry<Integer, Object> entry : entries) {
            if (entry.getKey() == key) {
                return (BookMetadata) entry.getValue();
            }
        }

        return null;
    }

    private void addTitleIndex(int bookId, String title) {
        String[] words = title.split("\\W+");
        for (String word : words) {
            word = word.toLowerCase();
            titleIndex.computeIfAbsent(word, k -> new ArrayList<>()).add(bookId);
        }
    }

    private void addBookshelfIndex(int id, List<String> bookshelves) {
        for (String bookshelf : bookshelves) {
            if (!bookshelf.isEmpty() && !bookshelf.isBlank()) {
                bookshelfIndex.computeIfAbsent(bookshelf.trim(), k -> new ArrayList<>()).add(id);
            }
        }
    }

    public List<Integer> searchForIdListByBookshelf(String bookshelf) {
        return bookshelfIndex.getOrDefault(bookshelf, Collections.emptyList());
    }

    private List<Integer> searchForIdListByKeyWord(String keyword) {
        return titleIndex.getOrDefault(keyword.toLowerCase(), Collections.emptyList());
    }

    public List<BookMetadata> searchByKeyWord(String keyword) {
        List<BookMetadata> results = new ArrayList<>();
        List<Integer> listIds = new ArrayList<>();

        for (String word : keyword.toLowerCase().split(" ")) {
            listIds.addAll(searchForIdListByKeyWord(word));
        }

        for (Integer i : listIds) {
            BookMetadata book = searchById(i);
            results.add(book);
        }

        return results;
    }

    public Task<Void> downloadEpubById(int id) {
        String FILE_URL = "https://www.gutenberg.org/ebooks/" + id + ".epub.noimages";
        String bookFolder = UserManager.getInstance().getUserDataPath() + "/gutenberg/";
        Path path = Paths.get(bookFolder);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        String FILE_NAME = UserManager.getInstance().getUserDataPath() + id + ".epub";
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                URL url = new URL(FILE_URL);
                URLConnection connection =  url.openConnection();
                int fileSize = connection.getContentLength();
                try (
                        BufferedInputStream in =
                                new BufferedInputStream(connection.getInputStream());

                        FileOutputStream fileOutputStream =
                                new FileOutputStream(FILE_NAME)) {

                    byte dataBuffer[] = new byte[1024];
                    int bytesRead;
                    int totalBytesRead = 0;

                    while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                        fileOutputStream.write(dataBuffer, 0, bytesRead);
                        totalBytesRead += bytesRead;
                        updateProgress(totalBytesRead, fileSize);
                        updateMessage("Downloaded " + totalBytesRead/1024
                                + "KBs of " + fileSize/1024 + "KBs");
                    }
                } catch (IOException e) {
                    // handle exception
                    e.printStackTrace();
                }
                return null;
            }
        };
    }

    public Task<Void> downloadMediumCoverById(int id) {
        String FILE_URL = "https://www.gutenberg.org/cache/epub/" + id + "/pg" + id + ".cover.medium.jpg";
        String coverFolderPath = UserManager.getInstance().getUserDataPath() + "/covers/";
        Path path = Paths.get(coverFolderPath);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        String FILE_NAME = coverFolderPath + id + ".cover.medium.jpg";
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                URL url = new URL(FILE_URL);
                URLConnection connection =  url.openConnection();
                int fileSize = connection.getContentLength();
                try (
                        BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
                        FileOutputStream fileOutputStream = new FileOutputStream(FILE_NAME)) {

                    byte dataBuffer[] = new byte[1024];
                    int bytesRead;
                    int totalBytesRead = 0;
                    while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                        fileOutputStream.write(dataBuffer, 0, bytesRead);
                        totalBytesRead += bytesRead;
                        updateProgress(totalBytesRead, fileSize);
                    }
                } catch (IOException e) {
                    // handle exception
                    e.printStackTrace();
                }

                return null;
            }
        };
    }
}
