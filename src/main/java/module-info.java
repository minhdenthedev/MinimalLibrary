module com.minhden.minimal.minimallibrary {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires com.opencsv;
    requires java.prefs;
    requires epublib.core;


    opens com.minhden.minimal to javafx.fxml;
    exports com.minhden.minimal;
}