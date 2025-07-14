package com.example.cs3560projectfx;

import javax.persistence.*;
import javax.persistence.Entity;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "book")
public class Book {

    @Id
    @Column(name = "isbn")
    private String isbn;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "authors")
    private String authors;

    @Column(name = "numberOfPages")
    private int numberOfPages;

    @Column(name = "publisher")
    private String publisher;

    @Column(name = "publicationDate")
    private LocalDate publicationDate;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL)
    private List<BookCopy> copies = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "library_id")
    private Library library;

    public Book(){}

    public Book(String isbn, String title, String description, String authors,
                int numberOfPages, String publisher, LocalDate publicationDate) {
        this.isbn = isbn;
        this.title = title;
        this.description = description;
        this.authors = authors;
        this.numberOfPages = numberOfPages;
        this.publisher = publisher;
        this.publicationDate = publicationDate;
        this.copies = new ArrayList<>();
    }

    // Getter Methods
    public String getIsbn() {
        return isbn;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getAuthors() {
        return authors;
    }

    public int getNumberOfPages() {
        return numberOfPages;
    }

    public String getPublisher() {
        return publisher;
    }

    public LocalDate getPublicationDate() {
        return publicationDate;
    }

    public List<BookCopy> getCopies() {
        return copies;
    }

    // Add a new book copy
    public void addCopy(BookCopy copy) {
        copies.add(copy);
        copy.setBook(this);
    }


    @Override
    public String toString() {
        return "\"" + title + "\" by " + String.join(", ", authors) + " (ISBN: " + isbn + ")";
    }
}

