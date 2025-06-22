package com.library;

import javafx.beans.property.*;

public class BorrowRecord {
    private final StringProperty title;
    private final StringProperty author;
    private final StringProperty borrowDate;
    private final StringProperty dueDate;
    private final DoubleProperty fine;

    public BorrowRecord(String title, String author, String borrowDate, String dueDate, double fine) {
        this.title = new SimpleStringProperty(title);
        this.author = new SimpleStringProperty(author);
        this.borrowDate = new SimpleStringProperty(borrowDate);
        this.dueDate = new SimpleStringProperty(dueDate);
        this.fine = new SimpleDoubleProperty(fine);
    }

    public StringProperty titleProperty() { return title; }
    public StringProperty authorProperty() { return author; }
    public StringProperty borrowDateProperty() { return borrowDate; }
    public StringProperty dueDateProperty() { return dueDate; }
    public DoubleProperty fineProperty() { return fine; }
}