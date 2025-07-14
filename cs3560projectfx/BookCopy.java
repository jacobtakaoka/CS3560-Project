package com.example.cs3560projectfx;

import javax.persistence.Entity;
import javax.persistence.*;

@Entity
@Table(name = "book_copy")
public class BookCopy {

    public enum Status {
        AVAILABLE,
        BORROWED
    }

    @Id
    @Column(name = "barcode")
    private String barcode;

    @Column(name = "location")
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book book;

    @ManyToOne
    @JoinColumn(name = "loan_number")
    private Loan loan;


    public BookCopy() {
        this.status = Status.AVAILABLE;
    }

    public BookCopy(String barcode, String location) {
        this.barcode = barcode;
        this.location = location;
        this.status = Status.AVAILABLE;
    }

    // Getters
    public Book getBook(){
        return book;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getLocation() {
        return location;
    }

    public Status getStatus() {
        return status;
    }

    public Loan getLoan(){
        return loan;
    }

    // Setters
    public void setBook(Book book){
        this.book = book;
    }
    public void setLocation(String location) {
        this.location = location;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setLoan(Loan loan){
        this.loan = loan;
    }

    @Override
    public String toString() {
        return "[Copy #" + barcode + "] at " + location + " (" + status + ")";
    }
}
