package com.minhden.minimal.application;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class UserManager {
    Preferences userPrefs;

    private static final String DOWNLOADED_BOOK = "downloadedBooks";
    private static final String DOWNLOADED_COVER = "downloadedCovers";
    private static final String DATA_PATH = "dataPath";

    private String defaultDataPath;

    private static UserManager instance;

    private String downloadedBooks;
    private String downloadedCovers;

    private UserManager() {
        userPrefs = Preferences.userRoot();
        String userHome = System.getProperty("user.home");
        defaultDataPath = userHome + File.separator + "library system" + File.separator + "data";
    }

    public static UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }

        return instance;
    }

    public void userDownloadedBookById(int id) {
        try {
            downloadedBooks = downloadedBooks + "," + Integer.toString(id);
            userPrefs.put(DOWNLOADED_BOOK, downloadedBooks);
            userPrefs.flush();
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
    }

    public void userDownloadedCoverById(int id) {
        try {
            downloadedCovers = downloadedCovers + "," + Integer.toString(id);
            userPrefs.put(DOWNLOADED_COVER, downloadedCovers);
            userPrefs.flush();
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
    }

    public List<Integer> getDownloadedBooks() {
        downloadedBooks = userPrefs.get(DOWNLOADED_BOOK, "");
        String[] idArray = downloadedBooks.split(",");
        List<Integer> integersList = new ArrayList<>();
        for (String s: idArray) {
            if (!s.isBlank() || !s.isEmpty()) {
                integersList.add(Integer.parseInt(s));
            }
        }
        return integersList;
    }

    public List<Integer> getDownloadedCovers() {
        downloadedCovers = userPrefs.get(DOWNLOADED_COVER, "");
        String[] ids = downloadedCovers.split(",");
        List<Integer> list = new ArrayList<>();
        for (String s : ids) {
            if (!s.isBlank() || !s.isEmpty()) {
                list.add(Integer.parseInt(s));
            }
        }

        return list;
    }

    public void setUserDataPath(String s) {
        try {
            userPrefs.put(DATA_PATH, s);
            userPrefs.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getUserDataPath() {
        return userPrefs.get(DATA_PATH, defaultDataPath);
    }
}
