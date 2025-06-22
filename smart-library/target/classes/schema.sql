DROP DATABASE IF EXISTS library;
CREATE DATABASE library;
USE library;

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL -- Stores hashed password
);

CREATE TABLE books (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    author VARCHAR(100) NOT NULL,
    genre VARCHAR(50),
    available BOOLEAN DEFAULT TRUE
);

CREATE TABLE borrows (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    book_id INT,
    borrow_date DATE NOT NULL,
    due_date DATE NOT NULL,
    returned BOOLEAN DEFAULT FALSE,
    fine DECIMAL(10,2) DEFAULT 0.00,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (book_id) REFERENCES books(id)
);

-- Sample data
INSERT INTO users (username, password) VALUES
('john_doe', '$2a$10$eImiTXuWVxfM37uY4JANjO2l6a98vO3z3b1b2q1z6x8y9z0w1v2u3'), -- Password: john123
('jane_smith', '$2a$10$Z3z4y5w6x7y8z9a0b1c2d3e4f5g6h7i8j9k0l1m2n3o4p5q6r7s8t'); -- Password: jane123

INSERT INTO books (title, author, genre, available) VALUES
('Harry Potter', 'J.K. Rowling', 'Fantasy', TRUE),
('The Da Vinci Code', 'Dan Brown', 'Mystery', TRUE),
('Python Programming', 'Eric Matthes', 'Technical', TRUE);

INSERT INTO borrows (user_id, book_id, borrow_date, due_date, returned, fine) VALUES
(1, 1, '2025-05-01', '2025-06-01', FALSE, 5.00); -- Overdue, with fine