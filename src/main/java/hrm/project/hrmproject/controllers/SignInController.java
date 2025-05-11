package hrm.project.hrmproject.controllers;

import DAO.Employee;
import hrm.project.hrmproject.controllers.EmployeeController.HomeEmployeeController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import hrm.project.hrmproject.utils.DbConnection;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SignInController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    private Connection connection;

    @FXML
    public void initialize() {
        loginButton.setOnAction(this::handleLogin);
    }

    private void handleLogin(ActionEvent event) {
        String email = emailField.getText();
        String pass = passwordField.getText();

        if (email.isEmpty() || pass.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill in both fields.");
            return;
        }

        try {
            connection = DbConnection.getConnection();
            assert connection != null;
            Object result = Employee.SignIn(connection, email, pass);

            if (result != null) {
                int userId = (int) result;
                navigateToHomePage(event, userId, connection);
            } else {
                showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid email or password.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not connect to database: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load home page: " + e.getMessage());
        }
    }

    private void navigateToHomePage(ActionEvent event, int userId, Connection conn) throws IOException, SQLException {
        try {
            // Load the FXML for the home page
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/hrm/project/hrmproject/views/Employee/employee-home.fxml"));
            Parent homePageRoot = loader.load();

            // Get the controller and initialize it with user data
            HomeEmployeeController homeController = loader.getController();
            homeController.initializeData(conn, userId);

            // Set up the new scene
            Scene homeScene = new Scene(homePageRoot);

            // Get the current stage and set the new scene
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("Employee Dashboard");
            stage.setScene(homeScene);
            stage.show();
        } catch (Exception e) {
            // Close the connection if navigation fails
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
            throw e;
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Method to clean up resources when the controller is no longer needed
    public void cleanup() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}