package com.minhden.minimal;

import com.minhden.minimal.application.BookManager;
import com.minhden.minimal.application.EpubHandler;
import com.minhden.minimal.application.UserManager;
import com.minhden.minimal.util.BookMetadata;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Callback;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.TOCReference;

import java.io.IOException;
import java.util.List;

public class BookTabController {
    BookManager bookManager = BookManager.getInstance();
    UserManager userManager = UserManager.getInstance();
    BookMetadata bookMetadata;
    @FXML private Label bookTitle;
    @FXML private WebView epubDisplay;
    @FXML private TreeView<TOCReference> tableOfContentDisplay;
    
    public void setBookMetadata(BookMetadata bookMetadata) {
        this.bookMetadata = bookMetadata;
        bookTitle.setText(bookMetadata.getTitle());
        int id = bookMetadata.getId();
        String filepath = userManager.getUserDataPath() + id + ".epub";
        Book book = EpubHandler.loadEpub(filepath);
        if (book != null) {
            displayEpub(book);
            buildTocTree(book);
        }

    }

    public void displayEpub(Book book) {
        WebEngine webEngine = epubDisplay.getEngine();
        try {
            String xhtml = new String(book.getContents().get(1).getData());
            webEngine.loadContent(xhtml, "application/xhtml+xml");
            webEngine.documentProperty().addListener((observable, oldDoc, newDoc) -> {
                if (newDoc != null) {
                    webEngine.executeScript(
                            "var style = document.createElement('style');" +
                                    "style.innerHTML = 'body { padding-right: 20px; }';" +
                                    "document.head.appendChild(style);"
                    );
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void buildTocTree(Book book) {
        TreeItem<TOCReference> root = new TreeItem<>(new TOCReference());
        List<TOCReference> tocReferences = book.getTableOfContents().getTocReferences();
        for (TOCReference ref : tocReferences) {
            recursiveToc(ref, root);
        }
        tableOfContentDisplay.setRoot(root);
        tableOfContentDisplay.setShowRoot(false);
        tableOfContentDisplay.setCellFactory(new Callback<>() {
            @Override
            public TreeCell<TOCReference> call(TreeView<TOCReference> tocReferenceTreeView) {
                return new TreeCell<>() {
                    @Override
                    protected void updateItem(TOCReference tocReference, boolean b) {
                        super.updateItem(tocReference, b);
                        if (b || tocReference == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            setText(tocReference.getTitle());
                        }
                    }
                };
            }
        });
        tableOfContentDisplay.getSelectionModel()
                .selectedItemProperty()
                .addListener((observableValue, tocReferenceTreeItem, t1) -> {
            if (t1 != null) {
                epubOpen(t1.getValue());
            }
        });
    }

    private void recursiveToc(TOCReference ref, TreeItem<TOCReference> above) {
        TreeItem<TOCReference> node = new TreeItem<>(ref);
        above.getChildren().add(node);
        for (TOCReference tocReference : ref.getChildren()) {
            recursiveToc(tocReference, node);
        }
    }

    public void epubOpen(TOCReference ref) {
        try {
            String xhtml = new String(ref.getResource().getData());
            epubDisplay.getEngine().loadContent(xhtml, "application/xhtml+xml");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
