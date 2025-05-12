package hrm.project.hrmproject.controllers.EmployeeController;

import hrm.project.hrmproject.modules.LeaveType;
import hrm.project.hrmproject.modules.VacationLeave;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import DAO.Employee;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Timer;
import java.util.TimerTask;

public class LeavesEmployeeController {

    @FXML private Button askLeaveButton;
    @FXML private DatePicker end_datee;
    @FXML private DatePicker start_datee;
    @FXML private ComboBox leaveType;
    @FXML private TableView<VacationLeave> leaveTable;
    @FXML private TableColumn<VacationLeave, Integer> colId;
    @FXML private TableColumn<VacationLeave, String> colType;
    @FXML private TableColumn<VacationLeave, String> colStart;
    @FXML private TableColumn<VacationLeave, String> colEnd;
    @FXML private TableColumn<VacationLeave, String> colStatus;

    private Connection conn;
    private int idUser;
    private boolean result = false;

    @FXML
    public void initialize(Connection conn,int userId) throws Exception {
        this.conn = conn;
        this.idUser = userId;
        setupColumns();
        loadLeaveData();
        loadLeaveTypes();
        scheduleAutoRefresh();
    }

    private void setupColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idVacationLeave"));
        colType.setCellValueFactory(data ->
                javafx.beans.binding.Bindings.createStringBinding(() ->
                        data.getValue().getLeaveType().getVacationType()
                )
        );
        colStart.setCellValueFactory(data ->
                javafx.beans.binding.Bindings.createStringBinding(() ->
                        data.getValue().getStartDate().toString()
                )
        );
        colEnd.setCellValueFactory(data ->
                javafx.beans.binding.Bindings.createStringBinding(() ->
                        data.getValue().getEndDate().toString()
                )
        );
        colStatus.setCellValueFactory(new PropertyValueFactory<>("statusOkNo"));
    }

    private void loadLeaveData() throws Exception {
        ObservableList<VacationLeave> data = Employee.getLeaves(idUser, conn);
        System.out.println("Loaded leave records: " + data.size());
        leaveTable.setItems(data);
    }
    private void loadLeaveTypes() throws SQLException {
        ObservableList<LeaveType> types = Employee.getLeaveTypes(conn);
        leaveType.setItems(types);
    }
    // Schedule refresh every 12 hours
    private void scheduleAutoRefresh() {
        Timer timer = new Timer(true);
        long delay = Duration.ofHours(12).toMillis();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                Platform.runLater(() -> {
                    try {
                        loadLeaveData();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }, delay, delay);
    }

    public void AskLeave(ActionEvent actionEvent) throws Exception {
        LeaveType type = (LeaveType) leaveType.getValue();
        LocalDate start_date_leave = start_datee.getValue();
        LocalDate end_date_leave = end_datee.getValue();
        result = Employee.addleaves(idUser, conn, type, start_date_leave, end_date_leave);

        if (result) {
            try {
                loadLeaveData();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            scheduleAutoRefresh();
        }
    }

}
