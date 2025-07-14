package com.example.cs3560projectfx;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LibraryApp extends Application {

    private Library library = new Library();
    private TextArea outputArea;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        primaryStage.setTitle("CPP Library Management System");

        // Output area for logs, receipts, reports
        outputArea = new TextArea();
        outputArea.setEditable(false);

        // Buttons for basic operations
        Button addStudentBtn = new Button("Add Student");
        Button updateStudentBtn = new Button("Update Student");
        Button removeStudentBtn = new Button("Remove Student");
        Button addBookBtn = new Button("Add Book");
        Button searchBookBtn = new Button("Search Book");
        Button createLoanBtn = new Button("Create Loan");
        Button returnLoanBtn = new Button("Return Loan");
        Button showReportBtn = new Button("Show Loans Report");

        // Layout
        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(addStudentBtn, updateStudentBtn, removeStudentBtn, addBookBtn, searchBookBtn, createLoanBtn, returnLoanBtn, showReportBtn, outputArea);
        vbox.setPrefSize(600, 400);

        // Button actions

        addStudentBtn.setOnAction(e -> addStudent());
        updateStudentBtn.setOnAction(e -> updateStudent());
        removeStudentBtn.setOnAction(e -> removeStudent());
        addBookBtn.setOnAction(e -> addBook());
        searchBookBtn.setOnAction(e -> searchBook());
        createLoanBtn.setOnAction(e -> createLoan());
        returnLoanBtn.setOnAction((e -> returnLoan()));
        showReportBtn.setOnAction(e -> showLoanReport());

        Scene scene = new Scene(vbox);
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    // Add student with simple input dialogs
    private void addStudent() {
        TextInputDialog idDialog = new TextInputDialog();
        idDialog.setHeaderText("Enter Bronco ID:");
        String broncoId = idDialog.showAndWait().orElse(null);
        if (broncoId == null || broncoId.isBlank()) return;

        TextInputDialog nameDialog = new TextInputDialog();
        nameDialog.setHeaderText("Enter Name:");
        String name = nameDialog.showAndWait().orElse(null);
        if (name == null || name.isBlank()) return;

        TextInputDialog addressDialog = new TextInputDialog();
        addressDialog.setHeaderText("Enter Address:");
        String address = addressDialog.showAndWait().orElse("");

        TextInputDialog degreeDialog = new TextInputDialog();
        degreeDialog.setHeaderText("Enter Degree:");
        String degree = degreeDialog.showAndWait().orElse("");

        Student student = new Student(broncoId, name, address, degree);

        library.addStudent(student);
        outputArea.appendText("Student Added: " + name + ", " + broncoId + "\n");
    }

    private void updateStudent() {
        TextInputDialog idDialog = new TextInputDialog();
        idDialog.setHeaderText("Enter Bronco ID of student to update:");
        String broncoId = idDialog.showAndWait().orElse(null);
        if (broncoId == null || broncoId.isBlank()) return;

        TextInputDialog addressDialog = new TextInputDialog();
        addressDialog.setHeaderText("Enter new address:");
        String newAddress = addressDialog.showAndWait().orElse("");

        TextInputDialog degreeDialog = new TextInputDialog();
        degreeDialog.setHeaderText("Enter new degree:");
        String newDegree = degreeDialog.showAndWait().orElse("");

        // Call Hibernate-based update in Library
        library.updateStudent(broncoId, newAddress, newDegree);
        outputArea.appendText("Student " + broncoId + " updated.\n");
    }

    private void removeStudent() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText("Enter Bronco ID of student to remove:");
        String broncoId = dialog.showAndWait().orElse(null);

        if (broncoId == null || broncoId.isBlank()) return;

        library.removeStudent(broncoId);
        outputArea.appendText("Removed student with ID: " + broncoId + "\n");
    }



    // Add book with simple input dialogs
    private void addBook() {
        TextInputDialog isbnDialog = new TextInputDialog();
        isbnDialog.setHeaderText("Enter ISBN:");
        String isbn = isbnDialog.showAndWait().orElse(null);
        if (isbn == null || isbn.isBlank()) return;

        TextInputDialog titleDialog = new TextInputDialog();
        titleDialog.setHeaderText("Enter Title:");
        String title = titleDialog.showAndWait().orElse(null);
        if (title == null || title.isBlank()) return;

        TextInputDialog authorsDialog = new TextInputDialog();
        authorsDialog.setHeaderText("Enter Authors (comma separated):");
        String authors = authorsDialog.showAndWait().orElse("");

        TextInputDialog descDialog = new TextInputDialog();
        descDialog.setHeaderText("Enter Description:");
        String description = descDialog.showAndWait().orElse("");

        TextInputDialog pagesDialog = new TextInputDialog();
        pagesDialog.setHeaderText("Enter Number of Pages:");
        int pages = 0;
        try {
            pages = Integer.parseInt(pagesDialog.showAndWait().orElse("0"));
        } catch (NumberFormatException ignored) {}

        TextInputDialog publisherDialog = new TextInputDialog();
        publisherDialog.setHeaderText("Enter Publisher:");
        String publisher = publisherDialog.showAndWait().orElse("");

        TextInputDialog pubDateDialog = new TextInputDialog();
        pubDateDialog.setHeaderText("Enter Publication Date (YYYY-MM-DD):");
        LocalDate pubDate = LocalDate.now();
        try {
            pubDate = LocalDate.parse(pubDateDialog.showAndWait().orElse(pubDate.toString()));
        } catch (Exception ignored) {}

        Book book = new Book(isbn, title, description, authors, pages, publisher, pubDate);

        try {
            library.addBook(book);
            outputArea.appendText("Added Book: " + book + "\n");
        } catch (Exception e) {
            e.printStackTrace();
            outputArea.appendText("Failed to save book.\n");
            return; // Don't continue if book saving failed
        }

        // Ask how many copies to add
        TextInputDialog copiesDialog = new TextInputDialog("1");
        copiesDialog.setHeaderText("How many copies to add?");
        int copiesCount = 1;
        try {
            copiesCount = Integer.parseInt(copiesDialog.showAndWait().orElse("1"));
        } catch (NumberFormatException ignored) {}

        for (int i = 1; i <= copiesCount; i++) {
            String barcode = isbn + "-C" + i;
            BookCopy copy = new BookCopy(barcode, "Default Location");
            book.addCopy(copy);
            try {
                library.addBookCopy(isbn, copy);
            } catch (Exception e) {
                e.printStackTrace();
                outputArea.appendText("Failed to save book copy " + barcode + "\n");
            }
        }

        outputArea.appendText("Added " + copiesCount + " copies of book.\n");
    }

    private void searchBook() {
        TextInputDialog titleDialog = new TextInputDialog();
        titleDialog.setHeaderText("Enter Title to Search:");
        String title = titleDialog.showAndWait().orElse(null);
        if (title == null || title.isBlank()) {
            outputArea.appendText("Search cancelled or empty title.\n");
            return;
        }

        List<Book> foundBooks;
        try {
            foundBooks = library.searchBooksByTitle(title);
        } catch (Exception e) {
            e.printStackTrace();
            outputArea.appendText("Error during book search.\n");
            return;
        }

        if (foundBooks.isEmpty()) {
            outputArea.appendText("No books found with title containing: " + title + "\n");
            return;
        }

        for (Book book : foundBooks) {
            outputArea.appendText("Book: " + book.getTitle() + " (ISBN: " + book.getIsbn() + ")\n");
            outputArea.appendText("Authors: " + book.getAuthors() + "\n");
            outputArea.appendText("Description: " + book.getDescription() + "\n");

            try {
                List<BookCopy> availableCopies = library.getAvailableCopies(book.getIsbn());
                Map<BookCopy, LocalDate> dueDates = library.getDueDates(book);

                outputArea.appendText("Available Copies:\n");
                if (availableCopies.isEmpty()) {
                    outputArea.appendText("  None\n");
                } else {
                    for (BookCopy copy : availableCopies) {
                        outputArea.appendText("  Barcode: " + copy.getBarcode() + "\n");
                    }
                }

                outputArea.appendText("Loaned Copies Due Dates:\n");
                if (dueDates.isEmpty()) {
                    outputArea.appendText("  None\n");
                } else {
                    for (Map.Entry<BookCopy, LocalDate> entry : dueDates.entrySet()) {
                        outputArea.appendText("  Barcode: " + entry.getKey().getBarcode() +
                                " - Due: " + entry.getValue().toString() + "\n");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                outputArea.appendText("Error fetching copies/due dates.\n");
            }
            outputArea.appendText("-------------------------\n");
        }
    }



    // Create loan with minimal inputs
    private void createLoan() {
        // Prompt for student Bronco ID
        TextInputDialog broncoDialog = new TextInputDialog();
        broncoDialog.setHeaderText("Enter Student Bronco ID:");
        String broncoId = broncoDialog.showAndWait().orElse(null);
        if (broncoId == null || broncoId.isBlank()) {
            outputArea.appendText("Loan creation cancelled.\n");
            return;
        }

        // Prompt for book copy barcodes (comma separated)
        TextInputDialog copiesDialog = new TextInputDialog();
        copiesDialog.setHeaderText("Enter Book Copy Barcodes (comma separated):");
        String barcodesInput = copiesDialog.showAndWait().orElse("");
        if (barcodesInput.isBlank()) {
            outputArea.appendText("No copies specified.\n");
            return;
        }
        List<String> barcodes = Arrays.stream(barcodesInput.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        // Prompt for loan duration days
        TextInputDialog durationDialog = new TextInputDialog("14"); // default 14 days
        durationDialog.setHeaderText("Enter Loan Duration (days):");
        int duration = 14;
        try {
            duration = Integer.parseInt(durationDialog.showAndWait().orElse("14"));
        } catch (NumberFormatException e) {
            outputArea.appendText("Invalid duration entered, using default 14 days.\n");
        }

        // Call library to create loan
        try {
            Loan loan = library.createLoan(broncoId, barcodes, duration);
            if (loan != null) {
                outputArea.appendText("Loan created successfully:\n" + loan.toString() + "\n");
            } else {
                outputArea.appendText("Failed to create loan.\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
            outputArea.appendText("Error creating loan.\n");
        }
    }

    private void returnLoan() {
        TextInputDialog loanDialog = new TextInputDialog();
        loanDialog.setHeaderText("Enter Loan Number to return:");
        String loanNumber = loanDialog.showAndWait().orElse(null);

        if (loanNumber == null || loanNumber.isBlank()) {
            outputArea.appendText("Return loan cancelled.\n");
            return;
        }

        // Optional: ask for return date or just use today
        LocalDate returnDate = LocalDate.now();
        TextInputDialog dateDialog = new TextInputDialog(returnDate.toString());
        dateDialog.setHeaderText("Enter Return Date (YYYY-MM-DD):");
        try {
            returnDate = LocalDate.parse(dateDialog.showAndWait().orElse(returnDate.toString()));
        } catch (Exception e) {
            outputArea.appendText("Invalid date entered, using today's date.\n");
        }

        try {
            library.returnLoan(loanNumber, returnDate);
            outputArea.appendText("Loan " + loanNumber + " returned successfully.\n");
        } catch (Exception e) {
            e.printStackTrace();
            outputArea.appendText("Failed to return loan.\n");
        }
    }



    // Show loans report for a student and period
    private void showLoanReport() {
        TextInputDialog broncoDialog = new TextInputDialog();
        broncoDialog.setHeaderText("Enter Bronco ID:");
        String broncoId = broncoDialog.showAndWait().orElse(null);
        if (broncoId == null || broncoId.isBlank()) return;

        TextInputDialog startDialog = new TextInputDialog(LocalDate.now().minusMonths(1).toString());
        startDialog.setHeaderText("Enter start date (YYYY-MM-DD):");
        LocalDate startDate = LocalDate.now().minusMonths(1);
        try {
            startDate = LocalDate.parse(startDialog.showAndWait().orElse(startDate.toString()));
        } catch (Exception ignored) {}

        TextInputDialog endDialog = new TextInputDialog(LocalDate.now().toString());
        endDialog.setHeaderText("Enter end date (YYYY-MM-DD):");
        LocalDate endDate = LocalDate.now();
        try {
            endDate = LocalDate.parse(endDialog.showAndWait().orElse(endDate.toString()));
        } catch (Exception ignored) {}

        List<Loan> loans = library.getLoansByStudentAndPeriod(broncoId, startDate, endDate);

        if (loans.isEmpty()) {
            outputArea.appendText("No loans found for that period.\n");
        } else {
            outputArea.appendText("Loan Report for " + broncoId + ":\n");
            for (Loan loan : loans) {
                outputArea.appendText(loan.toString() + "\n----------------\n");
            }
        }
    }
}

