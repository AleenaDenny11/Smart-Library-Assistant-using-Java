package com.library;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.sql.*;
import java.time.LocalDate;

public class MainApp extends Application {
    private Stage primaryStage;
    private Integer currentUserId = null; // Tracks logged-in user
    private ObservableList<Book> bookData = FXCollections.observableArrayList();
    private ObservableList<BorrowRecord> borrowData = FXCollections.observableArrayList();

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        showLoginScreen();
        primaryStage.setTitle("Smart Library Assistant");
        primaryStage.show();
    }

    private void showLoginScreen() {
        VBox loginPane = new VBox(10);
        loginPane.setPadding(new Insets(20));
        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();
        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();
        Button loginButton = new Button("Sign In");
        Button signupButton = new Button("Sign Up");
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");

        loginButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            Integer userId = TestDB.authenticateUser(username, password);
            if (userId != null) {
                currentUserId = userId;
                TestDB.updateFines(currentUserId); // Update fines on login
                showMainScreen();
            } else {
                errorLabel.setText("Invalid username or password!");
            }
        });

        signupButton.setOnAction(e -> showSignupScreen());

        loginPane.getChildren().addAll(usernameLabel, usernameField, passwordLabel, passwordField, loginButton, signupButton, errorLabel);
        primaryStage.setScene(new Scene(loginPane, 300, 250));
    }

    private void showSignupScreen() {
        VBox signupPane = new VBox(10);
        signupPane.setPadding(new Insets(20));
        Label usernameLabel = new Label("Choose Username:");
        TextField usernameField = new TextField();
        Label passwordLabel = new Label("Choose Password:");
        PasswordField passwordField = new PasswordField();
        Button createButton = new Button("Create Account");
        Button backButton = new Button("Back to Sign In");
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");

        createButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            if (username.isEmpty() || password.isEmpty()) {
                errorLabel.setText("Username and password cannot be empty!");
            } else if (TestDB.createUser(username, password)) {
                errorLabel.setStyle("-fx-text-fill: green;");
                errorLabel.setText("Account created! Please sign in.");
                usernameField.clear();
                passwordField.clear();
            } else {
                errorLabel.setText("Username already exists!");
            }
        });

        backButton.setOnAction(e -> showLoginScreen());

        signupPane.getChildren().addAll(usernameLabel, usernameField, passwordLabel, passwordField, createButton, backButton, errorLabel);
        primaryStage.setScene(new Scene(signupPane, 300, 250));
    }

    private void showMainScreen() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        Button voiceButton = new Button("Voice Command");
        TextField searchField = new TextField();
        searchField.setPromptText("Enter book title or author");
        Button searchButton = new Button("Search");
        Button borrowButton = new Button("Borrow Selected Book");
        Button returnButton = new Button("Return Selected Book");
        Button myBorrowsButton = new Button("My Borrowed Books");
        Button signOutButton = new Button("Sign Out");
        TableView<Book> table = new TableView<>();
        table.setItems(bookData);

        // Table columns
        TableColumn<Book, Number> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty());
        TableColumn<Book, String> titleColumn = new TableColumn<>("Title");
        titleColumn.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        TableColumn<Book, String> authorColumn = new TableColumn<>("Author");
        authorColumn.setCellValueFactory(cellData -> cellData.getValue().authorProperty());
        TableColumn<Book, String> genreColumn = new TableColumn<>("Genre");
        genreColumn.setCellValueFactory(cellData -> cellData.getValue().genreProperty());
        table.getColumns().add(idColumn);
        table.getColumns().add(titleColumn);
        table.getColumns().add(authorColumn);
        table.getColumns().add(genreColumn);

        // Search button action
        searchButton.setOnAction(e -> searchBooks(searchField.getText()));

        // Voice button action
        voiceButton.setOnAction(e -> {
            try {
                String command = VoiceRecognizer.recognize();
                if (command != null) {
                    searchField.setText(command);
                    searchBooks(command);
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Voice recognition failed!");
                    alert.showAndWait();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Borrow button action
        borrowButton.setOnAction(e -> {
            Book selectedBook = table.getSelectionModel().getSelectedItem();
            if (selectedBook != null) {
                borrowBook(selectedBook.idProperty().get());
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a book!");
                alert.showAndWait();
            }
        });

        // Return button action
        returnButton.setOnAction(e -> {
            Book selectedBook = table.getSelectionModel().getSelectedItem();
            if (selectedBook != null) {
                returnBook(selectedBook.idProperty().get());
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a book!");
                alert.showAndWait();
            }
        });

        // My Borrows button action
        myBorrowsButton.setOnAction(e -> showBorrowedBooksScreen());

        // Sign Out button action
        signOutButton.setOnAction(e -> {
            currentUserId = null;
            bookData.clear();
            showLoginScreen();
        });

        root.getChildren().addAll(voiceButton, searchField, searchButton, borrowButton, returnButton, myBorrowsButton, signOutButton, table);
        primaryStage.setScene(new Scene(root, 600, 400));

        // Load initial data
        loadBooks();
    }

    private void showBorrowedBooksScreen() {
        VBox borrowPane = new VBox(10);
        borrowPane.setPadding(new Insets(10));
        TableView<BorrowRecord> borrowTable = new TableView<>();
        borrowTable.setItems(borrowData);

        // Borrow table columns
        TableColumn<BorrowRecord, String> titleColumn = new TableColumn<>("Title");
        titleColumn.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        TableColumn<BorrowRecord, String> authorColumn = new TableColumn<>("Author");
        authorColumn.setCellValueFactory(cellData -> cellData.getValue().authorProperty());
        TableColumn<BorrowRecord, String> borrowDateColumn = new TableColumn<>("Borrow Date");
        borrowDateColumn.setCellValueFactory(cellData -> cellData.getValue().borrowDateProperty());
        TableColumn<BorrowRecord, String> dueDateColumn = new TableColumn<>("Due Date");
        dueDateColumn.setCellValueFactory(cellData -> cellData.getValue().dueDateProperty());
        TableColumn<BorrowRecord, Number> fineColumn = new TableColumn<>("Fine (€)");
        fineColumn.setCellValueFactory(cellData -> cellData.getValue().fineProperty());
        borrowTable.getColumns().add(titleColumn);
        borrowTable.getColumns().add(authorColumn);
        borrowTable.getColumns().add(borrowDateColumn);
        borrowTable.getColumns().add(dueDateColumn);
        borrowTable.getColumns().add(fineColumn);

        Button backButton = new Button("Back to Main");
        backButton.setOnAction(e -> showMainScreen());

        borrowPane.getChildren().addAll(borrowTable, backButton);
        primaryStage.setScene(new Scene(borrowPane, 600, 400));

        // Load borrowed books
        loadBorrowedBooks();
    }

    private void searchBooks(String query) {
        bookData.clear();
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM books WHERE title LIKE ? OR author LIKE ?")) {
            String likeQuery = "%" + query + "%";
            stmt.setString(1, likeQuery);
            stmt.setString(2, likeQuery);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                bookData.add(new Book(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("genre")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Database error: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void borrowBook(int bookId) {
        try (Connection conn = Database.getConnection()) {
            // Check if book is available
            PreparedStatement checkStmt = conn.prepareStatement("SELECT available FROM books WHERE id = ?");
            checkStmt.setInt(1, bookId);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getBoolean("available")) {
                // Update book availability
                PreparedStatement updateStmt = conn.prepareStatement("UPDATE books SET available = FALSE WHERE id = ?");
                updateStmt.setInt(1, bookId);
                updateStmt.executeUpdate();

                // Insert borrow record
                PreparedStatement borrowStmt = conn.prepareStatement(
                        "INSERT INTO borrows (user_id, book_id, borrow_date, due_date) VALUES (?, ?, ?, ?)");
                borrowStmt.setInt(1, currentUserId);
                borrowStmt.setInt(2, bookId);
                LocalDate borrowDate = LocalDate.now();
                borrowStmt.setDate(3, Date.valueOf(borrowDate));
                borrowStmt.setDate(4, Date.valueOf(borrowDate.plusMonths(1)));
                borrowStmt.executeUpdate();

                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Book borrowed successfully!");
                alert.showAndWait();
                loadBooks();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Book is not available!");
                alert.showAndWait();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Database error: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void returnBook(int bookId) {
        try (Connection conn = Database.getConnection()) {
            // Update borrow record
            PreparedStatement updateBorrowStmt = conn.prepareStatement(
                    "UPDATE borrows SET returned = TRUE WHERE book_id = ? AND user_id = ? AND returned = FALSE");
            updateBorrowStmt.setInt(1, bookId);
            updateBorrowStmt.setInt(2, currentUserId);
            int rows = updateBorrowStmt.executeUpdate();

            if (rows > 0) {
                // Update book availability
                PreparedStatement updateBookStmt = conn.prepareStatement("UPDATE books SET available = TRUE WHERE id = ?");
                updateBookStmt.setInt(1, bookId);
                updateBookStmt.executeUpdate();

                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Book returned successfully!");
                alert.showAndWait();
                loadBooks();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "No active borrow record found!");
                alert.showAndWait();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Database error: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void loadBooks() {
        searchBooks(""); // Load all books
    }

    private void loadBorrowedBooks() {
        borrowData.clear();
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT b.title, b.author, br.borrow_date, br.due_date, br.fine " +
                     "FROM borrows br JOIN books b ON br.book_id = b.id " +
                     "WHERE br.user_id = ? AND br.returned = FALSE")) {
            stmt.setInt(1, currentUserId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                borrowData.add(new BorrowRecord(
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getDate("borrow_date").toString(),
                        rs.getDate("due_date").toString(),
                        rs.getBigDecimal("fine").doubleValue()
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Database error: " + e.getMessage());
            alert.showAndWait();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}