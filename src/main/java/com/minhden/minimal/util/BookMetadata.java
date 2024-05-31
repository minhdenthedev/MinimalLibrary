package com.minhden.minimal.util;


import java.time.LocalDate;
import java.util.List;

public class BookMetadata {
    private Integer id;
    private String type;
    private LocalDate issued;
    private String title;
    private String language;
    private String authors;
    private List<String> locc;
    private List<String> subjects;
    private List<String> bookshelves;

    public BookMetadata(Integer id, String type, LocalDate issued, String title, String language, String authors,
                        List<String> locc, List<String> subjects, List<String> bookshelves) {
        this.id = id;
        this.type = type;
        this.issued = issued;
        this.title = title;
        this.language = language;
        this.authors = authors;
        this.locc = locc;
        this.subjects = subjects;
        this.bookshelves = bookshelves;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDate getIssued() {
        return issued;
    }

    public void setIssued(LocalDate issued) {
        this.issued = issued;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getAuthors() {
        return authors;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
    }

    public List<String> getLocc() {
        return locc;
    }

    public void setLocc(List<String> locc) {
        this.locc = locc;
    }

    public List<String> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<String> subjects) {
        this.subjects = subjects;
    }

    public List<String> getBookshelves() {
        return bookshelves;
    }

    public void setBookshelves(List<String> bookshelves) {
        this.bookshelves = bookshelves;
    }

    @Override
    public String toString() {
        return "BookMetadata [id=" + id + ", type=" + type + ", issued=" + issued + ", title=" + title + ", language="
                + language + ", authors=" + authors + ", locc=" + locc + ", subjects=" + subjects + ", bookshelves="
                + bookshelves + "]";
    }
}