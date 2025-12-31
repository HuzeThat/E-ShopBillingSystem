<<<<<<< HEAD
# Professional JavaFX Shop Billing System

A comprehensive point-of-sale (POS) system built with JavaFX and MySQL, designed for retail shops and small businesses.

## Features

### 🛒 Billing & Sales
- **Product Search**: Quick search functionality to find products
- **Customer Management**: Select existing customers or process walk-in sales
- **Real-time Bill Calculation**: Automatic calculation of totals, taxes, and discounts
- **Multiple Payment Methods**: Support for Cash, Card, UPI, Cheque, and Bank Transfer
- **Stock Management**: Automatic stock deduction after sales

### 📦 Product Management
- **Product CRUD Operations**: Add, view, update, and delete products
- **Inventory Tracking**: Real-time stock quantity management
- **Product Categories**: Organize products by categories
- **Barcode Support**: Unique barcode for each product

### 👥 Customer Management
- **Customer Database**: Maintain customer information
- **Customer Search**: Quick customer lookup
- **Purchase History**: Track customer purchase patterns

### 📊 Sales Reporting
- **Bill History**: Complete record of all transactions
- **Sales Analytics**: Track sales performance
- **Date-wise Reports**: Filter bills by date ranges

## Technology Stack

- **Frontend**: JavaFX 19
- **Backend**: Java 17
- **Database**: MySQL 8.0
- **Build Tool**: Maven
- **Architecture**: MVC Pattern with DAO Layer

## Prerequisites

Before running the application, ensure you have:

1. **Java 17 or higher** installed
2. **MySQL Server** running on your system
3. **Maven** for dependency management
4. **JavaFX SDK** (included in dependencies)

## Database Setup

1. **Install MySQL** and start the MySQL service
2. **Create a MySQL user** (or use root)
3. **Update database credentials** in `DatabaseManager.java`:
   ```java
   private static final String USERNAME = "your_username";
   private static final String PASSWORD = "your_password";
   ```

The application will automatically:
- Create the database `shop_billing_system`
- Create all required tables
- Insert sample data for testing

## Installation & Running

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd shop-billing-system
   ```

2. **Update database credentials** in `src/main/java/com/shopbilling/database/DatabaseManager.java`

3. **Build the project**:
   ```bash
   mvn clean compile
   ```

4. **Run the application**:
   ```bash
   mvn javafx:run
   ```

## Database Schema

### Products Table
- `id` (Primary Key)
- `name` (Product name)
- `description` (Product description)
- `price` (Product price)
- `stock_quantity` (Available stock)
- `category` (Product category)
- `barcode` (Unique barcode)
- `created_at`, `updated_at` (Timestamps)

### Customers Table
- `id` (Primary Key)
- `name` (Customer name)
- `email` (Customer email)
- `phone` (Customer phone)
- `address` (Customer address)
- `created_at`, `updated_at` (Timestamps)

### Bills Table
- `id` (Primary Key)
- `customer_id` (Foreign Key to customers)
- `total_amount` (Bill total before tax)
- `discount_amount` (Applied discount)
- `tax_amount` (Tax amount - 18% GST)
- `final_amount` (Final payable amount)
- `payment_method` (Payment method used)
- `bill_date` (Bill creation timestamp)

### Bill Items Table
- `id` (Primary Key)
- `bill_id` (Foreign Key to bills)
- `product_id` (Foreign Key to products)
- `quantity` (Item quantity)
- `unit_price` (Price per unit)
- `total_price` (Total price for the item)

## Usage Guide

### 1. Billing Process
1. Navigate to the **Billing** tab
2. Search and select products (double-click to add to bill)
3. Select customer (optional)
4. Apply discount if needed
5. Choose payment method
6. Click **Process Bill** to complete the sale

### 2. Product Management
1. Go to the **Products** tab
2. Fill in product details in the form
3. Click **Add Product** to save
4. View all products in the table

### 3. Customer Management
1. Navigate to the **Customers** tab
2. Enter customer information
3. Click **Add Customer** to save
4. View all customers in the table

### 4. View Sales History
1. Go to the **Bills History** tab
2. View all processed bills
3. Check bill details including customer and amounts

## Key Features Explained

### Automatic Tax Calculation
- 18% GST is automatically calculated on all sales
- Tax amount is clearly displayed in the bill summary

### Stock Management
- Stock is automatically reduced when items are sold
- Out-of-stock items cannot be added to bills
- Real-time stock updates across the application

### Professional UI
- Clean, modern interface designed for retail environments
- Intuitive navigation with tabbed interface
- Responsive design that works on different screen sizes

### Data Integrity
- Foreign key constraints ensure data consistency
- Transaction-based bill processing prevents data corruption
- Automatic timestamps for audit trails

## Customization

### Adding New Payment Methods
Update the payment method ComboBox in `MainController.java`:
```java
paymentMethodComboBox.setItems(FXCollections.observableArrayList(
    "CASH", "CARD", "UPI", "CHEQUE", "BANK_TRANSFER", "NEW_METHOD"
));
```

### Modifying Tax Rate
Change the tax calculation in `Bill.java` and `MainController.java`:
```java
// Change from 18% to desired rate
taxAmount = totalAmount.multiply(new BigDecimal("0.15")); // 15% tax
```

### Custom Styling
Modify `src/main/resources/css/styles.css` to change the application appearance.

## Troubleshooting

### Common Issues

1. **Database Connection Failed**
   - Ensure MySQL is running
   - Check username/password in DatabaseManager.java
   - Verify MySQL port (default: 3306)

2. **JavaFX Runtime Error**
   - Ensure JavaFX is properly configured
   - Check Java version compatibility

3. **Build Errors**
   - Run `mvn clean install` to refresh dependencies
   - Check internet connection for Maven downloads

### Error Logs
Check console output for detailed error messages and stack traces.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For support and questions:
- Create an issue in the repository
- Check the troubleshooting section
- Review the code documentation

---

**Professional JavaFX Shop Billing System** - Built for modern retail businesses with efficiency and reliability in mind.
=======
# SocialAnalystPro
>>>>>>> e514db69615bad3ddf4c54ed8be90dab43f4359b
