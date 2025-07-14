package com.example.cs3560projectfx;

import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.persistence.*;
import javax.persistence.Entity;
import java.time.LocalDate;
import java.util.*;

@Entity
@Table(name = "library")
public class Library {

    @Id
    @Column(name = "library_id")
    private int library_id;

    @OneToMany(mappedBy = "library", cascade = CascadeType.ALL)
    private List<Student> students;

    @OneToMany(mappedBy = "library", cascade = CascadeType.ALL)
    private List<Book> books;

    @OneToMany(mappedBy = "library", cascade = CascadeType.ALL)
    private List<Loan> loans;


    public Library() {
        students = new ArrayList<>();
        books = new ArrayList<>();
        loans = new ArrayList<>();
    }

    // --- Student management ---

    public void addStudent(Student student) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.persist(student);
            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateStudent(String broncoId, String newAddress, String newDegree) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Student student = session.get(Student.class, broncoId);
            if (student != null) {
                student.setAddress(newAddress);
                student.setDegree(newDegree);
                session.update(student);
                tx.commit();
            } else {
                System.out.println("Student not found with Bronco ID: " + broncoId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void removeStudent(String broncoId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            Student student = session.get(Student.class, broncoId);
            if (student != null) {
                session.delete(student);
                System.out.println("Student removed: " + broncoId);
            } else {
                System.out.println("Student not found: " + broncoId);
            }

            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // --- Book management ---

    public void addBook(Book book) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            session.persist(book);

            tx.commit();
            books.add(book);
            System.out.println("Book added: " + book.getTitle());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to add book: " + book.getTitle());
        }
    }


    public void addBookCopy(String isbn, BookCopy copy) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            Book book = session.get(Book.class, isbn);
            copy.setBook(book);
            book.addCopy(copy);
            if (book != null) {
                book.addCopy(copy); // update association
                session.persist(copy); // save the new copy
            } else {
                System.out.println("Book not found for ISBN: " + isbn);
            }

            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // Search books by title (partial match, case-insensitive)
    public List<Book> searchBooksByTitle(String title) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Book b WHERE lower(b.title) LIKE :pattern";
            return session.createQuery(hql, Book.class)
                    .setParameter("pattern", "%" + title.toLowerCase() + "%")
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }


    // Get available copies of a book
    public List<BookCopy> getAvailableCopies(String isbn) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM BookCopy bc WHERE bc.book.isbn = :isbn AND bc.status = :status";
            return session.createQuery(hql, BookCopy.class)
                    .setParameter("isbn", isbn)
                    .setParameter("status", BookCopy.Status.AVAILABLE)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }



    // Get due dates for copies of a book (map BookCopy -> dueDate)
    public Map<BookCopy, LocalDate> getDueDates(Book book) {
        Map<BookCopy, LocalDate> dueDates = new HashMap<>();

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Use HQL to join Loan and BookCopy, filter by book and unreturned loans
            String hql = "select copy, loan.dueDate " +
                    "from Loan loan join loan.borrowedCopies copy " +
                    "where loan.returnDate is null and copy.book = :book";

            List<Object[]> results = session.createQuery(hql, Object[].class)
                    .setParameter("book", book)
                    .getResultList();

            for (Object[] row : results) {
                BookCopy copy = (BookCopy) row[0];
                LocalDate dueDate = (LocalDate) row[1];
                dueDates.put(copy, dueDate);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dueDates;
    }


    // --- Loan management ---

    // Create a loan; returns Loan if successful, null if failure (due to rules)
    public Loan createLoan(String broncoId, List<String> copyBarcodes, int durationDays) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            Student student = session.get(Student.class, broncoId);
            if (student == null) {
                System.out.println("Student not found.");
                return null;
            }

            // Business rules checks
            if (durationDays > 180) {
                System.out.println("Loan duration exceeds 180 days.");
                return null;
            }
            if (student.hasOverdueItems()) {
                System.out.println("Student has overdue items; loan denied.");
                return null;
            }
            int currentBorrowed = student.countActiveBookCopies();
            if (currentBorrowed + copyBarcodes.size() > 5) {
                System.out.println("Student cannot borrow more than 5 books simultaneously.");
                return null;
            }

            // Validate and fetch BookCopy entities
            List<BookCopy> validCopies = new ArrayList<>();
            for (String barcode : copyBarcodes) {
                BookCopy managedCopy = session.get(BookCopy.class, barcode);
                if (managedCopy == null || managedCopy.getStatus() != BookCopy.Status.AVAILABLE) {
                    System.out.println("Book copy " + barcode + " is not available.");
                    return null;
                }
                managedCopy.setStatus(BookCopy.Status.BORROWED); // update status to borrowed
                session.update(managedCopy);
                validCopies.add(managedCopy);
            }


            LocalDate borrowDate = LocalDate.now();
            LocalDate dueDate = borrowDate.plusDays(durationDays);


            Loan loan = new Loan(student, borrowDate, dueDate, validCopies);

            for (BookCopy copy : validCopies) {
                copy.setLoan(loan); // Link each copy to the loan
                copy.setStatus(BookCopy.Status.BORROWED);
                session.update(copy);
            }

            session.persist(loan);
            // Add loan to student and library lists for in-memory sync if needed
            student.addLoan(loan);
            loans.add(loan);

            tx.commit();

            displayLoanReceipt(loan);
            return loan;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void returnLoan(String loanNumberStr, LocalDate returnDate) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            Long loanNumber = Long.parseLong(loanNumberStr);
            Loan loan = session.get(Loan.class, loanNumber);


            if (loan == null) {
                System.out.println("Loan not found.");
                return;
            }

            if (loan.isReturned()) {
                System.out.println("Loan already returned.");
                return;
            }

            // Mark loan as returned (e.g., set returnDate)
            loan.returnAllItems(returnDate);

            // Re-fetch each BookCopy by barcode to ensure they're managed by the session
            for (BookCopy copy : loan.getBorrowedCopies()) {
                BookCopy managedCopy = session.get(BookCopy.class, copy.getBarcode());

                if (managedCopy != null) {
                    System.out.println("Before update: " + managedCopy.getBarcode() + " -> " + managedCopy.getStatus());
                    managedCopy.setStatus(BookCopy.Status.AVAILABLE);
                    managedCopy.setLoan(null);
                    System.out.println("After update: " + managedCopy.getBarcode() + " -> " + managedCopy.getStatus());
                } else {
                    System.out.println("Copy not found in DB: " + copy.getBarcode());
                }
            }

            // Persist the loan changes
            session.update(loan);

            tx.commit();
            System.out.println("Loan returned successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to return loan.");
        }
    }





    // Display loan receipt on screen
    public void displayLoanReceipt(Loan loan) {
        System.out.println("\n--- Loan Receipt ---");
        System.out.println(loan.toString());
        System.out.println("--------------------\n");
    }

    // --- Reporting ---

    // Get loans by student within a date range
    public List<Loan> getLoansByStudentAndPeriod(String broncoId, LocalDate start, LocalDate end) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("""
                select distinct l
                from Loan l
                left join fetch l.borrowedCopies bc
                left join fetch bc.book
                where l.student.broncoId = :broncoId
                  and l.borrowDate between :start and :end
            """, Loan.class)
                    .setParameter("broncoId", broncoId)
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
