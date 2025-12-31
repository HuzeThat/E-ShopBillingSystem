package com.shopbilling;

import com.shopbilling.controller.CustomerDashboardController;
import com.shopbilling.controller.MainController;
import com.shopbilling.controller.UnifiedLoginController;
import com.shopbilling.database.DatabaseManager;
import com.shopbilling.model.Customer;
import com.shopbilling.model.User;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ShopBillingApplication extends Application {

    private static Stage primaryStage;
    private static Customer currentCustomer;
    private static String css;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        css = this.getClass().getResource("/css/styles.css").toExternalForm();
        // Initialize database
        DatabaseManager.getInstance().initializeDatabase();

        // Load Unified Login Screen
        showUnifiedLogin();
    }

    public static void showLogin() {
        showUnifiedLogin();
    }

    public static void showUnifiedLogin() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(ShopBillingApplication.class.getResource("/fxml/UnifiedLogin.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 400, 450);
            scene.getStylesheets().add(css);
            
            UnifiedLoginController controller = fxmlLoader.getController();
            controller.setStage(primaryStage);

            primaryStage.setTitle("E-Shop_Billing_System - Login");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void showMainDashboard(User user) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(ShopBillingApplication.class.getResource("/fxml/main.fxml"));
            Parent root = fxmlLoader.load();
            
            MainController controller = fxmlLoader.getController();
            controller.setStage(primaryStage);
            controller.setCurrentUser(user);
            
            Scene scene = new Scene(root, 1200, 800);
            scene.getStylesheets().add(css);
            primaryStage.setTitle("E-Shop_Billing_System - Admin Dashboard");
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void showCustomerRegistration() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(ShopBillingApplication.class.getResource("/fxml/CustomerRegistration.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 800, 600);
            scene.getStylesheets().add(css);
            primaryStage.setTitle("E-Shop_Billing_System - Customer Registration");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void showCustomerDashboard(Customer customer) {
        currentCustomer = customer;
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(ShopBillingApplication.class.getResource("/fxml/customer_dashboard.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 1200, 800);
            scene.getStylesheets().add(css);

            CustomerDashboardController controller = fxmlLoader.getController();
            controller.setCustomer(customer);
            controller.setStage(primaryStage);

            primaryStage.setTitle("E-Shop_Billing_System - Customer Dashboard");
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void showCustomerLogin() {
        showUnifiedLogin();
    }

    public static void main(String[] args) {
        launch();
    }
}
