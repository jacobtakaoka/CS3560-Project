package com.example.cs3560projectfx;

import javax.persistence.Entity;
import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "loan")
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "loan_number")
    private Long loanNumber;

    @ManyToOne
    @JoinColumn(name = "bronco_id", nullable = false)
    private Student student;

    @Column(name = "borrow_date", nullable = false)
    private LocalDate borrowDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @ManyToOne
    @JoinColumn(name = "library_id")
    private Library library;

    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL)
    private List<BookCopy> borrowedCopies;

    public Loan() {}

    public Loan( Student student, LocalDate borrowDate, LocalDate dueDate, List<BookCopy> borrowedCopies) {
        this.student = student;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.borrowedCopies = borrowedCopies;

        for (BookCopy copy : borrowedCopies) {
            copy.setStatus(BookCopy.Status.BORROWED);
        }
    }

    // Getters and setters

    public long getLoanNumber() {
        return loanNumber;
    }

    public void setLoanNumber(long loanNumber) {
        this.loanNumber = loanNumber;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(LocalDate borrowDate) {
        this.borrowDate = borrowDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public List<BookCopy> getBorrowedCopies() {
        return borrowedCopies;
    }

    public void setBorrowedCopies(List<BookCopy> borrowedCopies) {
        this.borrowedCopies = borrowedCopies;
    }


    public boolean isReturned() {
        return returnDate != null;
    }

    public boolean isOverdue() {
        return !isReturned() && LocalDate.now().isAfter(dueDate);
    }

    public void returnAllItems(LocalDate returnDate) {
        if (isReturned()) {
            System.out.println("Loan already returned.");
            return;
        }

        this.returnDate = returnDate;

        for (BookCopy copy : borrowedCopies) {
            copy.setStatus(BookCopy.Status.AVAILABLE);
            copy.setLoan(null);
        }
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Loan #").append(loanNumber).append("\n");
        sb.append("Student: ").append(student).append("\n");
        sb.append("Borrowed: ").append(borrowDate).append(" | Due: ").append(dueDate).append("\n");
        if (isReturned()) {
            sb.append("Returned: ").append(returnDate).append("\n");
        } else if (isOverdue()) {
            sb.append("Status: OVERDUE\n");
        } else {
            sb.append("Status: ACTIVE\n");
        }

        sb.append("Items:\n");
        for (BookCopy copy : borrowedCopies) {
            sb.append(" - ").append(copy.toString()).append("\n");
        }
        return sb.toString();
    }
}
