package hrm.project.hrmproject.controllers.EmployeeController;

import DAO.Employee;
import hrm.project.hrmproject.modules.Attendance;
import hrm.project.hrmproject.modules.Salary;
import hrm.project.hrmproject.modules.TrainingEnrollment;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HomeEmployeeController {

    @FXML private Button checkin;
    @FXML private Button checkout;
    @FXML private TextField brutSalaryField;
    @FXML private TextField bonusField;
    @FXML private TextField netSalaryField;
    @FXML private TextField taxesField;

    @FXML private Button downloadSalaryFileButton;

    @FXML private TableView<TrainingEnrollment> trainingTableView;
    @FXML private TableColumn<TrainingEnrollment, String> courseNameColumn;
    @FXML private TableColumn<TrainingEnrollment, java.util.Date> startDateColumn;
    @FXML private TableColumn<TrainingEnrollment, java.util.Date> endDateColumn;
    @FXML private TableColumn<TrainingEnrollment, String> statusColumn;

    @FXML private TableView<Attendance> attendanceTableView;
    @FXML private TableColumn<Attendance, String> dateColumn;
    @FXML private TableColumn<Attendance, String> checkInColumn;
    @FXML private TableColumn<Attendance, String> checkOutColumn;
    @FXML private TableColumn<Attendance, String> notesColumn;
    @FXML private TableColumn<Attendance, String> attendanceStatusColumn;

    private Connection conn;
    private int userId;
    private List<Object> salaryData;

    public void initializeData(Connection conn, int userId) {
        this.conn = conn;
        this.userId = userId;

        setupTrainingTable();
        setupAttendanceTable();
        loadTrainingData();
        salaryData = loadSalaryInfo();
        loadAttendanceInfo();
        startAttendanceRefresh();

        downloadSalaryFileButton.setOnAction(e -> downloadSalaryFile());
        checkin.setOnAction(e -> handleCheckIn());
        checkout.setOnAction(e -> handleCheckOut());
    }

    private void setupTrainingTable() {
        courseNameColumn.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Set custom cell factories to handle null values
        courseNameColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
            }
        });

        startDateColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(java.util.Date item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.toString());
            }
        });

        endDateColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(java.util.Date item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.toString());
            }
        });

        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
            }
        });
    }

    private void setupAttendanceTable() {
        dateColumn.setCellValueFactory(cell -> {
            var d = cell.getValue().getDateDim();
            if (d == null) return new javafx.beans.property.SimpleStringProperty("");
            return new javafx.beans.property.SimpleStringProperty(
                    d.getDay() + "/" + d.getMonth() + "/" + d.getYear()
            );
        });

        checkInColumn.setCellValueFactory(cell -> {
            var t = cell.getValue().getCheckinTime();
            return new javafx.beans.property.SimpleStringProperty(
                    t != null ? t.toLocalDateTime().toLocalTime().toString() : ""
            );
        });

        checkOutColumn.setCellValueFactory(cell -> {
            var t = cell.getValue().getCheckoutTime();
            return new javafx.beans.property.SimpleStringProperty(
                    t != null ? t.toLocalDateTime().toLocalTime().toString() : ""
            );
        });

        notesColumn.setCellValueFactory(cell -> {
            String remarks = cell.getValue().getRemarks();
            return new javafx.beans.property.SimpleStringProperty(remarks != null ? remarks : "");
        });

        attendanceStatusColumn.setCellValueFactory(cell -> {
            String status = cell.getValue().getStatus();
            return new javafx.beans.property.SimpleStringProperty(status != null ? status : "");
        });
    }

    private void loadTrainingData() {
        try {
            ObservableList<TrainingEnrollment> data = Employee.training(userId, conn);
            trainingTableView.setItems(data);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private List<Object> loadSalaryInfo() {
        try {
            List<Object> data = Employee.salary_info(userId, conn);
            if (data.size() >= 1 && data.get(0) instanceof Salary salary) {
                brutSalaryField.setText(salary.getBrutSalary() != null ? salary.getBrutSalary().toString() : "");
                bonusField.setText(salary.getBonus() != null ? salary.getBonus().toString() : "");
                netSalaryField.setText(salary.getNetSalary() != null ? salary.getNetSalary().toString() : "");
                taxesField.setText(salary.getTaxes() != null ? salary.getTaxes().toString() : "");
            }

            return data;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void loadAttendanceInfo() {
        try {
            List<Attendance> data = Employee.attendance_info(userId, conn);
            attendanceTableView.getItems().setAll(data);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void startAttendanceRefresh() {
        ScheduledService<Void> refresher = new ScheduledService<>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<>() {
                    @Override
                    protected Void call() {
                        try {
                            int id_date = getDateId(LocalDate.now());
                            Attendance a = Employee.current_attendance(userId, conn, id_date);
                            if (a != null && !"pending".equalsIgnoreCase(a.getStatus())) {
                                Platform.runLater(() -> loadAttendanceInfo());
                                cancel(); // stop refreshing
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                };
            }
        };
        refresher.setPeriod(Duration.seconds(20));
        refresher.start();
    }

    @FXML
    private void downloadSalaryFile() {
        if (salaryData == null || salaryData.isEmpty()) {
            showAlert("Error", "Salary data is not available.");
            return;
        }

        try {
            // Create a list to hold only Salary objects
            List<Salary> salaryList = new ArrayList<>();

            // Extract Salary objects from salaryData
            for (Object obj : salaryData) {
                if (obj instanceof Salary) {
                    salaryList.add((Salary) obj);
                }
            }

            // Call the download_salary method with the filtered list
            if (!salaryList.isEmpty()) {
                Employee.download_salary(salaryList);
                showAlert("Success", "Salary file downloaded successfully.");
            } else {
                showAlert("Error", "No salary information found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Could not download salary file: " + e.getMessage());
        }
    }

    private int getDateId(LocalDate date) throws SQLException {
        var stmt = conn.prepareStatement("SELECT id_start_date FROM date_dim WHERE day=? AND month=? AND year=?");
        stmt.setInt(1, date.getDayOfMonth());
        stmt.setInt(2, date.getMonthValue());
        stmt.setInt(3, date.getYear());
        var rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getInt("id_start_date");
        } else {
            throw new SQLException("Current date not found in date_dim table.");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    private void handleCheckIn() {
        try {
            int id_date = getDateId(LocalDate.now());
            // Insert the check-in logic here (e.g., insert a new attendance record with the check-in time)
            Employee.checkIn(userId, conn, id_date);
            // Optional: Show success message
            showAlert("Success", "Checked in successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to check in.");
        }
    }

    private void handleCheckOut() {
        try {
            int id_date = getDateId(LocalDate.now());
            // Insert the check-out logic here (e.g., update the attendance record with the check-out time)
            Employee.checkOut(userId, conn, id_date);
            // Optional: Show success message
            showAlert("Success", "Checked out successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to check out.");
        }
    }
}