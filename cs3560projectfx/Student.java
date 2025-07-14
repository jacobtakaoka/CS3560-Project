package com.example.cs3560projectfx;

import javax.persistence.*;
import javax.persistence.Entity;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "student")
public class Student {

    @Id
    @Column(name = "bronco_id")
    private String broncoId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "degree")
    private String degree;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
    private List<Loan> loanHistory = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "library_id")
    private Library library;

    public Student() {}

    public Student(String broncoId, String name, String address, String degree) {
        this.broncoId = broncoId;
        this.name = name;
        this.address = address;
        this.degree = degree;
    }

    // Getters and setters

    public String getBroncoId() {
        return broncoId;
    }

    public void setBroncoId(String broncoId) {
        this.broncoId = broncoId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDegree() {
        return degree;
    }

    public void setDegree(String degree) {
        this.degree = degree;
    }

    public List<Loan> getLoanHistory() {
        return loanHistory;
    }

    public void setLoanHistory(List<Loan> loanHistory) {
        this.loanHistory = loanHistory;
    }

    public void addLoan(Loan loan) {
        loanHistory.add(loan);
        loan.setStudent(this);
    }

    public boolean hasOverdueItems() {
        for (Loan loan : loanHistory) {
            if (!loan.isReturned() && loan.isOverdue()) {
                return true;
            }
        }
        return false;
    }

    public int countActiveBookCopies() {
        int total = 0;
        for (Loan loan : loanHistory) {
            if (!loan.isReturned()) {
                total += loan.getBorrowedCopies().size();
            }
        }
        return total;
    }

    public boolean canBorrow() {
        return !hasOverdueItems() && countActiveBookCopies() < 5;
    }

    @Override
    public String toString() {
        return name + " (Bronco ID: " + broncoId + ")";
    }
}
