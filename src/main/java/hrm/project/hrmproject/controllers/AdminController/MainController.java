package hrm.project.hrmproject.controllers.AdminController;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class MainController {
    @FXML
    private BorderPane mainBorderPane;

    @FXML
    public void initialize() {
        try {
            Parent adminHomePage = FXMLLoader.load(getClass().getResource("/hrm/project/hrmproject/views/Admin/homePage.fxml"));
            mainBorderPane.setCenter(adminHomePage);
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Could not load the home page.").showAndWait();
        }
    }

    public void handleHomeButton(ActionEvent actionEvent) {

    }

    public void handleManageUsersButton(ActionEvent actionEvent) {
    }

    public void handleManageEmployeesButton(ActionEvent actionEvent) {
    }

    public void handleManageLeavesButton(ActionEvent actionEvent) {
    }
}