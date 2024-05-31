package com.minhden.minimal;

import com.minhden.minimal.application.BookManager;
import com.minhden.minimal.application.UserManager;
import com.minhden.minimal.util.BookMetadata;
import com.minhden.minimal.util.TextProcessing;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

public class MainController {
    BookManager bookManager = BookManager.getInstance();
    UserManager userManager = UserManager.getInstance();
    List<String> bookshelves = bookManager.getBookshelves();
    List<String> keywords = bookManager.getKeywordList();


    @FXML private ListView<String> listBookshelvesGutenberg;
    @FXML private HBox bookshelfHeaderGutenberg;
    @FXML private ListView<BookMetadata> listBooksInBookshelf;
    @FXML private TextField bookshelfSearch;
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private TabPane tabPane;
    @FXML private ImageView coverView;
    @FXML private VBox coverDownloadingIndicator;
    @FXML private VBox previewPanel;
    @FXML private Tab gutenbergTab;
    @FXML private VBox bookPreviewPanel;

    public void initialize() {
        Collections.sort(bookshelves);
        for (String s : bookshelves) {
            listBookshelvesGutenberg.getItems().add(s);
        }
        ChangeListener<String> listBookshelvesListener = new ListBookshelvesListener();

        listBookshelvesGutenberg
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(listBookshelvesListener);
    }

    private class ListBookshelvesListener implements ChangeListener<String> {
        @Override
        public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
            if (t1 != null) {
                openGutenbergBookshelf(t1);
            }
        }
    }

    public void openGutenbergBookshelf(String bookshelf) {
        // Clear existing content of the header
        bookshelfHeaderGutenberg.getChildren().clear();

        // Create and add the new label
        Label label = new Label(bookshelf);
        label.setStyle("-fx-font-size: 30px");
        bookshelfHeaderGutenberg.getChildren().add(label);

        // Retrieve the books in bookshelf
        List<Integer> ids = bookManager.searchForIdListByBookshelf(bookshelf);
        ObservableList<BookMetadata> books = FXCollections.observableArrayList();
        for (int i : ids) {
            books.add(bookManager.searchById(i));
        }
        listBooksInBookshelf.getItems().clear();
        listBooksInBookshelf.setItems(books);
        listBooksInBookshelf.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(BookMetadata bookMetadata, boolean b) {
                super.updateItem(bookMetadata, b);

                if (b || bookMetadata == null) {
                    setText(null);
                } else {
                    setText(bookMetadata.getTitle());
                }
            }
        });
        WeakChangeListener<BookMetadata> listBookInBookshelfListener =
                new WeakChangeListener<>(new ListBookInBookshelfListener());

        listBooksInBookshelf
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(listBookInBookshelfListener);
    }

    private class ListBookInBookshelfListener implements ChangeListener<BookMetadata> {
        @Override
        public void changed(ObservableValue<? extends BookMetadata> observableValue,
                            BookMetadata bookMetadata, BookMetadata t1) {
            if (t1 != null) {
                openPreview(t1);
            }
        }
    }

    public void loadCover(BookMetadata bookMetadata) {
        int id = bookMetadata.getId();
        String filepath = userManager.getUserDataPath()
                + File.separator + "covers" + File.separator + id + ".cover.medium.jpg";
        File file = new File(filepath);
        if (!file.exists()) {
            Task<Void> task = bookManager.downloadMediumCoverById(bookMetadata.getId());
            ProgressIndicator progressIndicator = new ProgressIndicator(0);
            progressIndicator.progressProperty().bind(task.progressProperty());
            coverDownloadingIndicator.getChildren().clear();
            coverDownloadingIndicator.getChildren().add(progressIndicator);
            coverDownloadingIndicator.setVisible(true);

            task.stateProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue == Worker.State.SUCCEEDED) {
                    // Ensure this part runs on the JavaFX Application Thread
                    // Load the image after the download is complete
                    Image image = new Image("file:" + filepath);
                    coverView.setImage(image);
                    coverDownloadingIndicator.setVisible(false);
                } else if (newValue == Worker.State.FAILED || newValue == Worker.State.CANCELLED) {
                    // Handle failure or cancellation if needed
                    coverDownloadingIndicator.getChildren().add(new Label("Download cover failed."));
                }
            });

            Thread downloadThread = new Thread(task);
            downloadThread.setDaemon(true);
            downloadThread.start();
        }
        Image image = new Image("file:" + filepath);
        coverView.setImage(image);
    }

    public void openPreview(BookMetadata bookMetadata) {
        // Load cover
        loadCover(bookMetadata);

        double maxWidth = 290;

        Text text = new Text();
        text.setWrappingWidth(maxWidth);
        previewPanel.getChildren().clear();
        text.setText(bookMetadata.getTitle());

        Label titleLabel = new Label(bookMetadata.getTitle());
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(maxWidth);
        titleLabel.setStyle("-fx-font-style: italic; -fx-font-weight: bold");

        Label authorsLabel = new Label("Author(s): " + bookMetadata.getAuthors());
        authorsLabel.setWrapText(true);
        authorsLabel.setMaxWidth(maxWidth);

        Label languageLabel = new Label("Language: " + bookMetadata.getLanguage());
        languageLabel.setWrapText(true);
        languageLabel.setMaxWidth(maxWidth);

        Label issuedLabel = new Label("Issued: " + bookMetadata.getIssued());
        issuedLabel.setWrapText(true);
        issuedLabel.setMaxWidth(maxWidth);

        Label subjectsLabel = new Label("Subjects: " + bookMetadata.getSubjects());
        subjectsLabel.setWrapText(true);
        subjectsLabel.setMaxWidth(maxWidth);

        Label loccLabel = new Label("LoCC: " + bookMetadata.getLocc());
        loccLabel.setWrapText(true);
        loccLabel.setMaxWidth(maxWidth);

        Label bookshelvesLabel = new Label("Bookshelves: " + bookMetadata.getBookshelves());
        bookshelvesLabel.setWrapText(true);
        bookshelvesLabel.setMaxWidth(maxWidth);

        previewPanel.getChildren()
                .addAll(createBookButton(bookMetadata), titleLabel, authorsLabel, languageLabel,
                        issuedLabel, subjectsLabel, loccLabel,
                        bookshelvesLabel);
        previewPanel.setVisible(true);
    }

    private Button createBookButton(BookMetadata bookMetadata) {
        Button button = new Button();
        int id = bookMetadata.getId();
        String filepath = userManager.getUserDataPath() + id + ".epub";
        File file = new File(filepath);
        if (!file.exists()) {
            button.setText("Download");
            button.setOnAction(actionEvent -> {
                Task<Void> task = bookManager.downloadEpubById(id);
                ProgressBar progressBar = new ProgressBar(0);
                progressBar.setPrefWidth(200);
                progressBar.setMinWidth(200);
                progressBar.setMaxWidth(200);

                progressBar.progressProperty().bind(task.progressProperty());
                previewPanel.getChildren().remove(button);
                previewPanel.getChildren().addFirst(progressBar);

                task.stateProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue == Worker.State.SUCCEEDED) {
                        button.setText("Open");
                        button.setOnAction(actionEvent1 -> {
                            createBookTab(bookMetadata);
                        });
                        previewPanel.getChildren().removeFirst();
                        previewPanel.getChildren().addFirst(button);
                    } else if (newValue == Worker.State.FAILED || newValue == Worker.State.CANCELLED) {
                        button.setText("Download");
                        previewPanel.getChildren().removeFirst();
                        previewPanel.getChildren().addFirst(button);
                    }
                });

                Thread thread = new Thread(task);
                thread.setDaemon(true);
                thread.start();
            });
        } else {
            button.setOnAction(actionEvent -> {
                createBookTab(bookMetadata);
            });
            button.setText("Open");
        }

        return button;
    }

    public void createBookTab(BookMetadata bookMetadata) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("book-tab.fxml"));
            BorderPane pane = fxmlLoader.load();
            BookTabController bookTabController = fxmlLoader.getController();
            bookTabController.setBookMetadata(bookMetadata);
            Tab tab = new Tab(bookMetadata.getTitle());
            tab.setContent(pane);
            tabPane.getTabs().add(tab);
            tabPane.getSelectionModel().select(tab);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void searchForBookshelf() {
        String text = bookshelfSearch.getText();

        // If no text
        if (text.isBlank() || text.isEmpty()) {
            listBookshelvesGutenberg.getItems().clear();
            listBookshelvesGutenberg.getItems().addAll(bookshelves);
        } else {
            // Search for bookshelves
            List<String> results = new ArrayList<>();

            for (String s : bookshelves) {
                List<Integer> idx = TextProcessing.search(s.toLowerCase(), text.toLowerCase());
                if (!idx.isEmpty()) {
                    results.add(s);
                }
            }

            // Add to ListView
            listBookshelvesGutenberg.getItems().clear();
            listBookshelvesGutenberg.getItems().addAll(results);
        }
    }

    public void searchForBook() {
        // Get the text
        String text = searchField.getText();
        // If no text
        if (text.isEmpty() || text.isBlank()) {
            return;
        }

        // Perform search
        List<BookMetadata> list = bookManager.searchByKeyWord(text);
        ListView<BookMetadata> listView = new ListView<>();
        for (BookMetadata book : list) {
            listView.getItems().add(book);
        }
        listView.setCellFactory(new Callback<>() {
            @Override
            public ListCell<BookMetadata> call(ListView<BookMetadata> bookMetadataListView) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(BookMetadata bookMetadata, boolean b) {
                        super.updateItem(bookMetadata, b);
                        if (b || bookMetadata == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            setText(bookMetadata.getTitle());
                        }
                    }

                };
            }
        });


        // Create new tab
        Tab newTab = new Tab("Search results");
        newTab.setClosable(true);
        // Create Border Pane
        SplitPane splitPane = new SplitPane();
        splitPane.setDividerPositions(0.7, 0.3);
        VBox vbox = new VBox();
        vbox.setSpacing(10);
        VBox.setVgrow(vbox, Priority.ALWAYS);
        vbox.setPadding(new Insets(10, 0, 0, 0));
        Label label = new Label();
        VBox.setMargin(label, new Insets(0, 0, 0, 20));
        if (list.isEmpty()) {
            label.setText("No result found for \"" + text + "\"");
        } else {
            label.setText("Showing " + list.size() + " results for " + text);
        }
        vbox.getChildren().add(label);

        vbox.getChildren().add(listView);
        VBox.setVgrow(listView, Priority.ALWAYS);
        splitPane.getItems().add(vbox);
        splitPane.getItems().add(bookPreviewPanel);
        WeakChangeListener<BookMetadata> listChangeListener =
                new WeakChangeListener<>(new ListBookInBookshelfListener());
        listView.getSelectionModel().selectedItemProperty().addListener(listChangeListener);

        newTab.setContent(splitPane);
        tabPane.getTabs().add(newTab);
        tabPane.getSelectionModel().select(newTab);
    }
}