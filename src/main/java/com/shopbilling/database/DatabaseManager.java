package com.shopbilling.database;

import java.sql.*;
import java.util.Properties;

public class DatabaseManager {
    private static DatabaseManager instance;
    private Connection connection;

    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/";
    // Changed database name again to force recreation of tables with new schema
    private static final String DB_NAME = "shop_billing_v3";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "huzei12";

    private DatabaseManager() {
        try {
            // Create database if it doesn't exist
            createDatabaseIfNotExists();

            // Connect to the database
            Properties props = new Properties();
            props.setProperty("user", USERNAME);
            props.setProperty("password", PASSWORD);
            props.setProperty("useSSL", "false");
            props.setProperty("allowPublicKeyRetrieval", "true");
            props.setProperty("serverTimezone", "UTC");

            connection = DriverManager.getConnection(DB_URL + DB_NAME, props);
            System.out.println("Connected to MySQL database successfully!");

            // Check and update schema for image_path
            updateSchema();
            updateCustomerSchema();
            updateCustomerSchemaForUsername();

        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                // Reconnect if connection is closed
                Properties props = new Properties();
                props.setProperty("user", USERNAME);
                props.setProperty("password", PASSWORD);
                props.setProperty("useSSL", "false");
                props.setProperty("allowPublicKeyRetrieval", "true");
                props.setProperty("serverTimezone", "UTC");

                connection = DriverManager.getConnection(DB_URL + DB_NAME, props);
            }
        } catch (SQLException e) {
            System.err.println("Failed to get database connection: " + e.getMessage());
        }
        return connection;
    }

    private void createDatabaseIfNotExists() throws SQLException {
        Properties props = new Properties();
        props.setProperty("user", USERNAME);
        props.setProperty("password", PASSWORD);
        props.setProperty("useSSL", "false");
        props.setProperty("allowPublicKeyRetrieval", "true");
        props.setProperty("serverTimezone", "UTC");

        try (Connection conn = DriverManager.getConnection(DB_URL, props);
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
            System.out.println("Database created or already exists.");
        }
    }

    public void initializeDatabase() {
        try {
            createTables();
            insertSampleData();
            ensureDefaultCustomer();
        } catch (SQLException e) {
            System.err.println("Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createTables() throws SQLException {
        // We need to create tables in order because of foreign key constraints
        // 1. Products, Customers, Users (independent)
        // 2. Bills (depends on Customers and Users)
        // 3. Bill Items (depends on Bills and Products)

        String createProductsTable = """
      CREATE TABLE IF NOT EXISTS products (
        id INT AUTO_INCREMENT PRIMARY KEY,
        name VARCHAR(255) NOT NULL,
        description TEXT,
        price DECIMAL(10,2) NOT NULL,
        stock_quantity INT NOT NULL DEFAULT 0,
        category VARCHAR(100),
        barcode VARCHAR(100) UNIQUE,
        image_path VARCHAR(255),
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
      ) ENGINE=InnoDB;
      """;

        String createCustomersTable = """
      CREATE TABLE IF NOT EXISTS customers (
        id INT AUTO_INCREMENT PRIMARY KEY,
        name VARCHAR(255) NOT NULL,
        username VARCHAR(255) UNIQUE,
        email VARCHAR(255),
        phone VARCHAR(20),
        address TEXT,
        password VARCHAR(255),
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
      ) ENGINE=InnoDB;
      """;

        String createUsersTable = """
      CREATE TABLE IF NOT EXISTS users (
        id INT AUTO_INCREMENT PRIMARY KEY,
        username VARCHAR(50) NOT NULL UNIQUE,
        password VARCHAR(255) NOT NULL,
        full_name VARCHAR(100) NOT NULL,
        role VARCHAR(20) NOT NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
      ) ENGINE=InnoDB;
      """;

        String createBillsTable = """
      CREATE TABLE IF NOT EXISTS bills (
        id INT AUTO_INCREMENT PRIMARY KEY,
        customer_id INT,
        created_by_user_id INT,
        total_amount DECIMAL(10,2) NOT NULL,
        discount_amount DECIMAL(10,2) DEFAULT 0,
        tax_amount DECIMAL(10,2) DEFAULT 0,
        final_amount DECIMAL(10,2) NOT NULL,
        payment_method VARCHAR(50) DEFAULT 'CASH',
        bill_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        status VARCHAR(20) DEFAULT 'PENDING',
        FOREIGN KEY (customer_id) REFERENCES customers(id),
        FOREIGN KEY (created_by_user_id) REFERENCES users(id)
      ) ENGINE=InnoDB;
      """;

        String createBillItemsTable = """
      CREATE TABLE IF NOT EXISTS bill_items (
        id INT AUTO_INCREMENT PRIMARY KEY,
        bill_id INT NOT NULL,
        product_id INT NOT NULL,
        quantity INT NOT NULL,
        unit_price DECIMAL(10,2) NOT NULL,
        total_price DECIMAL(10,2) NOT NULL,
        FOREIGN KEY (bill_id) REFERENCES bills(id) ON DELETE CASCADE,
        FOREIGN KEY (product_id) REFERENCES products(id)
      ) ENGINE=InnoDB;
      """;

        try (Statement stmt = connection.createStatement()) {
            // Execute each query separately to ensure order
            stmt.executeUpdate(createProductsTable);
            stmt.executeUpdate(createCustomersTable);
            stmt.executeUpdate(createUsersTable);
            stmt.executeUpdate(createBillsTable);
            stmt.executeUpdate(createBillItemsTable);
            System.out.println("All tables created successfully!");
        }
    }

    private void updateSchema() {
        try (Statement stmt = connection.createStatement()) {
            // Check if image_path column exists in products table
            DatabaseMetaData meta = connection.getMetaData();
            ResultSet rs = meta.getColumns(null, null, "products", "image_path");
            if (!rs.next()) {
                // Column doesn't exist, add it
                stmt.executeUpdate("ALTER TABLE products ADD COLUMN image_path VARCHAR(255)");
                System.out.println("Added image_path column to products table.");
            }
        } catch (SQLException e) {
            System.err.println("Error updating schema: " + e.getMessage());
        }
    }

    private void updateCustomerSchema() {
        try (Statement stmt = connection.createStatement()) {
            // Check if password column exists in customers table
            DatabaseMetaData meta = connection.getMetaData();
            ResultSet rs = meta.getColumns(null, null, "customers", "password");
            if (!rs.next()) {
                // Column doesn't exist, add it
                stmt.executeUpdate("ALTER TABLE customers ADD COLUMN password VARCHAR(255)");
                System.out.println("Added password column to customers table.");
            }
        } catch (SQLException e) {
            System.err.println("Error updating customer schema: " + e.getMessage());
        }
    }

    private void updateCustomerSchemaForUsername() {
        try (Statement stmt = connection.createStatement()) {
            // Check if username column exists in customers table
            DatabaseMetaData meta = connection.getMetaData();
            ResultSet rs = meta.getColumns(null, null, "customers", "username");
            if (!rs.next()) {
                // Column doesn't exist, add it
                stmt.executeUpdate("ALTER TABLE customers ADD COLUMN username VARCHAR(255) UNIQUE");
                System.out.println("Added username column to customers table.");
            }
        } catch (SQLException e) {
            System.err.println("Error updating customer schema for username: " + e.getMessage());
        }
    }

    private void insertSampleData() throws SQLException {
        // Insert default admin user if not exists
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
            if (rs.next() && rs.getInt(1) == 0) {
                // In a real app, use a secure password hash. For this demo, plaintext is fine.
                stmt.executeUpdate("INSERT INTO users (username, password, full_name, role) VALUES ('huze', 'huze321', 'Administrator', 'ADMIN')");
                System.out.println("Default admin user created.");
            } else {
                // Attempt to update the old default user 'admin' back to 'huze' if it exists
                // This ensures the change takes effect even if the database is already populated
                try {
                    stmt.executeUpdate("UPDATE users SET username='huze', password='huze321' WHERE username='admin'");
                } catch (SQLException e) {
                    // Ignore if 'admin' doesn't exist or other error
                }
            }
        }

        // Check if sample product data already exists
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM products")) {

            if (rs.next() && rs.getInt(1) > 0) {
                return; // Sample data already exists
            }
        }

        // Insert sample products
        String insertProducts = """
      INSERT INTO products (name, description, price, stock_quantity, category, barcode) VALUES
      ('Laptop Dell Inspiron', 'High-performance laptop for business', 45000.00, 10, 'Electronics', '1234567890123'),
      ('Wireless Mouse', 'Ergonomic wireless mouse', 1200.00, 50, 'Electronics', '1234567890124'),
      ('Office Chair', 'Comfortable ergonomic office chair', 8500.00, 15, 'Furniture', '1234567890125'),
      ('Coffee Mug', 'Ceramic coffee mug 350ml', 250.00, 100, 'Kitchen', '1234567890126'),
      ('Notebook A4', 'Spiral bound notebook 200 pages', 150.00, 200, 'Stationery', '1234567890127'),
      ('Smartphone Samsung', 'Latest Android smartphone', 25000.00, 25, 'Electronics', '1234567890128'),
      ('Desk Lamp', 'LED desk lamp with adjustable brightness', 1800.00, 30, 'Electronics', '1234567890129'),
      ('Water Bottle', 'Stainless steel water bottle 1L', 450.00, 75, 'Kitchen', '1234567890130')
      """;

        // Insert sample customers
        String insertCustomers = """
      INSERT INTO customers (name, username, email, phone, address, password) VALUES
      ('Walk-in Customer', 'walkin', '', '', '', ''),
      ('John Doe', 'johndoe', 'john.doe@email.com', '+91-9876543210', '123 Main Street, City', 'password123'),
      ('Jane Smith', 'janesmith', 'jane.smith@email.com', '+91-9876543211', '456 Oak Avenue, City', 'password123'),
      ('Mike Johnson', 'mikejohnson', 'mike.johnson@email.com', '+91-9876543212', '789 Pine Road, City', 'password123'),
      ('Sarah Wilson', 'sarahwilson', 'sarah.wilson@email.com', '+91-9876543213', '321 Elm Street, City', 'password123')
      """;

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(insertProducts);
            stmt.executeUpdate(insertCustomers);
            System.out.println("Sample data inserted successfully!");
        }
    }

    private void ensureDefaultCustomer() throws SQLException {
        // Ensure "Walk-in Customer" exists with ID 1 or at least exists
        String checkQuery = "SELECT id FROM customers WHERE id = 1";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(checkQuery)) {
            if (!rs.next()) {
                // If ID 1 doesn't exist, insert it.
                // Note: If auto-increment is already past 1, this might fail or create a different ID depending on SQL mode.
                // Safer to just check by name or insert if empty.
                // But for foreign key constraint failure on ID 1 (if hardcoded), we need ID 1.
                // Let's try to insert explicitly with ID 1 if possible, or just rely on the fact that we inserted it first in sample data.

                // If sample data wasn't inserted because products existed, we might still need the customer.
                String insertDefault = "INSERT INTO customers (id, name, username, email, phone, address) VALUES (1, 'Walk-in Customer', 'walkin', '', '', '') ON DUPLICATE KEY UPDATE name=name";
                try {
                    stmt.executeUpdate(insertDefault);
                } catch (SQLException e) {
                    // If explicit ID insert fails (e.g. permission), try without ID
                    stmt.executeUpdate("INSERT INTO customers (name, username, email, phone, address) VALUES ('Walk-in Customer', 'walkin', '', '', '')");
                }
            }
        }
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }
}
