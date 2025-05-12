package hrm.project.hrmproject.controllers.EmployeeController;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.Parent;

import java.io.IOException;
import java.sql.Connection;

public class MainEmployeeController {

    @FXML
    private BorderPane mainBorderPane;

    private Connection connection;
    private int userId;

    // This is called once from SignInController
    public void loadHomePage(Connection conn, int userId) throws IOException {
        this.connection = conn;
        this.userId = userId;
        loadHomePage(); // reuse the existing logic
    }

    // This is used internally when other buttons are clicked
    public void loadHomePage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/hrm/project/hrmproject/views/Employee/employee-home.fxml"));
        Parent homePage = loader.load();

        HomeEmployeeController homeController = loader.getController();
        homeController.initializeData(connection, userId);

        mainBorderPane.setCenter(homePage);
    }

    public void handleHomeButton(ActionEvent event) {
        try {
            loadHomePage();
        } catch (IOException e) {
            e.printStackTrace();  // Replace with proper logging
        }
    }

    public void handleManageLeavesByEmployeeButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/hrm/project/hrmproject/views/Employee/employee-manages-leaves.fxml"));
            Parent leavesPage = loader.load();
            LeavesEmployeeController leavecontroller = loader.getController();
            leavecontroller.initialize(connection,userId);

            mainBorderPane.setCenter(leavesPage);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void handleCoursesEventsButton(ActionEvent event) {
        // You don't have the file yet, so just leave a placeholder
        System.out.println("Courses & Events page is under construction.");
    }

    public void handleProfile(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/hrm/project/hrmproject/views/Employee/employee-profile.fxml"));
            Parent profilePage = loader.load();
            ProfileEmployeeController profilecontroller = loader.getController();
            profilecontroller.initialize(userId,connection);

            mainBorderPane.setCenter(profilePage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
