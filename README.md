Overview
---------
The Smart Library Assistant is a JavaFX-based desktop application designed to manage a library's book borrowing system. It allows users to create accounts, sign in/out, search for books, borrow/return books, view their borrowed books, and handle overdue fines. The application integrates voice recognition for searching books and connects to a MySQL database for data persistence.

Features
----------
User Account Management: Create an account with a username and password (passwords are securely hashed using BCrypt).
Sign in and sign out securely.
Book Management: Search for books by title or author using text input or voice commands (via Vosk).
Borrow and return books, with availability tracking.
Borrowed Books List: View a list of currently borrowed books, including borrow date, due date, and any fines.
Fine System:Books borrowed for over one month incur a 5-euro fine, displayed in the borrowed books view.
Voice Recognition: Use voice commands to search for books (e.g., say "Harry Potter" to search).

Run the application
--------------------
mvn javafx:run
