package hrm.project.hrmproject.controllers.EmployeeController;

import DAO.Employee;
import hrm.project.hrmproject.modules.Attendance;
import hrm.project.hrmproject.modules.Salary;
import hrm.project.hrmproject.modules.TrainingEnrollment;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
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
import java.util.List;

public class HomeEmployeeController {

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
    private String salaryFilePath;

    public void initializeData(Connection conn, int userId) {
        this.conn = conn;
        this.userId = userId;

        setupTrainingTable();
        setupAttendanceTable();
        loadTrainingData();
        loadSalaryInfo();
        loadAttendanceInfo();
        startAttendanceRefresh();

        downloadSalaryFileButton.setOnAction(e -> downloadSalaryFile());
    }

    private void setupTrainingTable() {
        courseNameColumn.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void setupAttendanceTable() {
        dateColumn.setCellValueFactory(cell -> {
            var d = cell.getValue().getDateDim();
            return new javafx.beans.property.SimpleStringProperty(
                    d.getDay() + "/" + d.getMonth() + "/" + d.getYear()
            );
        });

        checkInColumn.setCellValueFactory(cell -> {
            var t = cell.getValue().getCheckinTime();
            return new javafx.beans.property.SimpleStringProperty(
                    t != null ? t.toLocalDateTime().toLocalTime().toString() : "N/A"
            );
        });

        checkOutColumn.setCellValueFactory(cell -> {
            var t = cell.getValue().getCheckoutTime();
            return new javafx.beans.property.SimpleStringProperty(
                    t != null ? t.toLocalDateTime().toLocalTime().toString() : "N/A"
            );
        });


        notesColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getRemarks()));
        attendanceStatusColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getStatus()));
    }

    private void loadTrainingData() {
        try {
            ObservableList<TrainingEnrollment> data = Employee.training(userId, conn);
            trainingTableView.setItems(data);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadSalaryInfo() {
        try {
            List<Object> data = Employee.salary_info(userId, conn);
            if (data.size() >= 2 && data.get(0) instanceof Salary salary) {
                brutSalaryField.setText(salary.getBrutSalary().toString());
                bonusField.setText(salary.getBonus().toString());
                netSalaryField.setText(salary.getNetSalary().toString());
                taxesField.setText(salary.getTaxes().toString());
                salaryFilePath = (String) data.get(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
        if (salaryFilePath == null) {
            showAlert("Error", "Salary file path is not available.");
            return;
        }

        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Salary File");
            fileChooser.setInitialFileName(Paths.get(salaryFilePath).getFileName().toString());
            File dest = fileChooser.showSaveDialog(null);

            if (dest != null) {
                try (FileInputStream fis = new FileInputStream(salaryFilePath);
                     FileOutputStream fos = new FileOutputStream(dest)) {
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = fis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    showAlert("Success", "Salary file downloaded successfully.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Could not download salary file.");
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
}
