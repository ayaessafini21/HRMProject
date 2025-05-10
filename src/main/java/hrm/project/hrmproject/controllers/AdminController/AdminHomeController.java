package hrm.project.hrmproject.controllers.AdminController;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import hrm.project.hrmproject.utils.DbConnection;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;

public class AdminHomeController {
    @FXML
    private BorderPane border1, border2;

    @FXML
    private PieChart statdepartment;

    @FXML
    private VBox attendanceContent;  // the VBox inside the ScrollPane

    @FXML
    private ScrollPane attendanceBox;

    @FXML
    public void initialize() {
        try {
            initialize_border1();
            initialize_border2();

            // Optional: refresh every minute
            Timeline timeline = new Timeline(new KeyFrame(Duration.minutes(1), e -> {
                try {
                    initialize_border2();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }));
            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.play();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void initialize_border1() throws SQLException {
        String sql = "SELECT P.NAME, COUNT(*) AS COUNT FROM USERS AS U JOIN DEPARTMENT AS P ON U.ID_DEPARTMENT = P.ID_DEPARTMENT GROUP BY P.NAME;";
        Connection result = DbConnection.getConnection();
        if (result != null) {
            try {
                Statement stm = result.createStatement();
                ResultSet rslt = stm.executeQuery(sql);
                ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

                while (rslt.next()) {
                    int count = rslt.getInt("COUNT");
                    String dep_name = rslt.getString("NAME");
                    PieChart.Data data = new PieChart.Data(dep_name, count);
                    pieChartData.add(data);
                    data.setName(dep_name);
                    String tooltipText = dep_name + ": " + count + " employees";
                    Tooltip tooltip = new Tooltip(tooltipText);

                    // Delay tooltip installation until the chart is rendered
                    data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                        if (newNode != null) {
                            Tooltip.install(newNode, tooltip);
                        }
                    });
                }

                statdepartment.setData(pieChartData);
                statdepartment.setLabelsVisible(true);
                statdepartment.setLabelLineLength(10);

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public void initialize_border2() throws SQLException {
        LocalDate today = LocalDate.now();
        int jour = today.getDayOfMonth();
        int mois = today.getMonthValue();
        int annee = today.getYear();

        try (Connection conn = DbConnection.getConnection()) {
            if (conn != null) {
                // Check or insert date
                String reqCheck = "SELECT id_start_date FROM date_dim WHERE day = ? AND month = ? AND year = ?";
                PreparedStatement checkStmt = conn.prepareStatement(reqCheck);
                checkStmt.setInt(1, jour);
                checkStmt.setInt(2, mois);
                checkStmt.setInt(3, annee);
                ResultSet rsCheck = checkStmt.executeQuery();

                int id_date = -1;
                if (rsCheck.next()) {
                    id_date = rsCheck.getInt("id_start_date");
                } else {
                    String reqInsert = "INSERT INTO date_dim (day, month, year) VALUES (?,?,?)";
                    PreparedStatement insertStmt = conn.prepareStatement(reqInsert, Statement.RETURN_GENERATED_KEYS);
                    insertStmt.setInt(1, jour);
                    insertStmt.setInt(2, mois);
                    insertStmt.setInt(3, annee);
                    insertStmt.executeUpdate();
                    ResultSet generatedKeys = insertStmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        id_date = generatedKeys.getInt(1);
                    }
                }

                if (LocalTime.now().isAfter(LocalTime.of(8, 59))) {
                    automate_insertion(id_date);
                    String query = """
                SELECT A.status, A.checkin_time, U.first_name, U.last_name
                FROM attendance A
                JOIN users U ON A.id_user = U.id_user
                WHERE A.id_start_date = ?
                """;
                    PreparedStatement pstmt = conn.prepareStatement(query);
                    pstmt.setInt(1, id_date);
                    ResultSet rs = pstmt.executeQuery();

                    ObservableList<String> latePeople = FXCollections.observableArrayList();
                    ObservableList<String> pendingPeople = FXCollections.observableArrayList();
                    ObservableList<String> absentPeople = FXCollections.observableArrayList();

                    while (rs.next()) {
                        String status = rs.getString("status");
                        Time checkinTime = rs.getTime("checkin_time");
                        String fullName = rs.getString("first_name") + " " + rs.getString("last_name");

                        if ("absent".equalsIgnoreCase(status)) {
                            absentPeople.add(fullName);
                        } else if (checkinTime != null) {
                            LocalTime checkin = checkinTime.toLocalTime();
                            if (checkin.isAfter(LocalTime.of(9, 0))) {
                                latePeople.add(fullName);
                            }
                        } else if ("pending".equalsIgnoreCase(status)) {
                            pendingPeople.add(fullName);
                        }
                    }

                    // ✅ Clear the VBox inside the ScrollPane
                    attendanceContent.getChildren().clear();

                    if (!absentPeople.isEmpty()) {
                        Label absentLabel = new Label("❌ Absent:");
                        absentLabel.setStyle("-fx-font-weight: bold;");
                        attendanceContent.getChildren().add(absentLabel);
                        for (String name : absentPeople) {
                            attendanceContent.getChildren().add(new Label("• " + name));
                        }
                    }

                    if (!pendingPeople.isEmpty()) {
                        Label pendingLabel = new Label("❌ Pending:");
                        pendingLabel.setStyle("-fx-font-weight: bold;");
                        attendanceContent.getChildren().add(pendingLabel);
                        for (String name : pendingPeople) {
                            attendanceContent.getChildren().add(new Label("• " + name));
                        }
                    }

                    if (!latePeople.isEmpty()) {
                        Label lateLabel = new Label("⌛ Late:");
                        lateLabel.setStyle("-fx-font-weight: bold;");
                        attendanceContent.getChildren().add(lateLabel);
                        for (String name : latePeople) {
                            attendanceContent.getChildren().add(new Label("• " + name));
                        }
                    }

                    if (absentPeople.isEmpty() && latePeople.isEmpty() && pendingPeople.isEmpty()) {
                        Label allPresentLabel = new Label("✅ Everyone is present");
                        allPresentLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: green;");
                        attendanceContent.getChildren().add(allPresentLabel);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void automate_insertion(int id_date) throws SQLException {
        try (Connection conn = DbConnection.getConnection()) {
            if (conn != null) {
                ObservableList<Integer> people = FXCollections.observableArrayList();
                String sql = "SELECT id_user FROM users";
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {
                        people.add(rs.getInt("id_user"));
                    }
                }

                String checksql = "SELECT id_user FROM attendance WHERE id_start_date = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(checksql)) {
                    pstmt.setInt(1, id_date);
                    try (ResultSet rslt = pstmt.executeQuery()) {
                        while (rslt.next()) {
                            int id1 = rslt.getInt("id_user");
                            people.remove(Integer.valueOf(id1));
                        }
                    }
                }

                for (Integer person : people) {
                    String insertQuery = "INSERT INTO attendance (status, remarks, checkin_time, checkout_time, id_user, id_start_date) VALUES (?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {
                        pstmt.setString(1, "pending");
                        pstmt.setNull(2, Types.VARCHAR);
                        pstmt.setNull(3, Types.TIME);
                        pstmt.setNull(4, Types.TIME);
                        pstmt.setInt(5, person);
                        pstmt.setInt(6, id_date);
                        pstmt.executeUpdate();
                    }
                }
            }
        }
    }

}
