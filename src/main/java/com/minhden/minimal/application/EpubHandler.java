package com.minhden.minimal.application;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.epub.EpubReader;

public class EpubHandler {
    public EpubHandler() {
    }

    public static Book loadEpub(String filePath) {
        try (InputStream epubInputStream = new FileInputStream(filePath)) {
            Book book = (new EpubReader()).readEpub(epubInputStream);
            return book;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
