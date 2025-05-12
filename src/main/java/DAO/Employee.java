package DAO;

import hrm.project.hrmproject.modules.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Employee {
    public static Object SignIn (Connection conn, String email, String pass) throws SQLException {
        String req = "SELECT id_user FROM compte WHERE login= ? AND password= ?";
        PreparedStatement stm = conn.prepareStatement(req);
        stm.setString(1,email);
        stm.setString(2,pass);
        ResultSet rslt = stm.executeQuery();
        if (rslt.next()){
            int id = rslt.getInt("id_user");
            return id;
        }
        return null;
    }
    public static ObservableList<TrainingEnrollment> training(int id_user, Connection conn) throws SQLException {
        if (conn != null) {
            String req = "SELECT * FROM training_enrollment WHERE id_user = ?";
            PreparedStatement stm = conn.prepareStatement(req);
            stm.setInt(1, id_user);
            ResultSet rslt = stm.executeQuery();

            ObservableList<TrainingEnrollment> training_list = FXCollections.observableArrayList();
            while (rslt.next()) {
                TrainingEnrollment t = new TrainingEnrollment();
                t.setIdEnrollment(rslt.getInt("id_enrollment"));
                t.setCourseName(rslt.getString("course_name"));
                t.setStartDate(rslt.getDate("start_date"));
                t.setEndDate(rslt.getDate("end_date"));
                t.setUser(null);
                t.setStatus(rslt.getString("status"));
                training_list.add(t);
            }
            return training_list;
        }
        return FXCollections.observableArrayList();
    }

    public static List<Object> salary_info(int id_user, Connection conn) throws SQLException {
        if (conn != null) {
            List<Object> salaire_info = new ArrayList<>();

            // Salary
            String req = "SELECT * FROM Salary WHERE id_user = ?";
            PreparedStatement stm = conn.prepareStatement(req);
            stm.setInt(1, id_user);
            ResultSet rslt = stm.executeQuery();
            if (rslt.next()) {
                Salary salaire = new Salary();
                int id_s = rslt.getInt("id_salary");
                BigDecimal bs = rslt.getBigDecimal("brut_salary");
                BigDecimal bonus = rslt.getBigDecimal("bonus");
                BigDecimal ns = rslt.getBigDecimal("net_salary");
                BigDecimal taxes = rslt.getBigDecimal("taxes");
                salaire.setIdSalary(id_s);
                salaire.setBrutSalary(bs);
                salaire.setBonus(bonus);
                salaire.setNetSalary(ns);
                salaire.setTaxes(taxes);
                salaire_info.add(salaire);
            }
            return salaire_info;
        }
        return new ArrayList<>();
    }
    public static void download_salary(List<Salary> salaire){
        if (salaire !=null){
            String userHome = System.getProperty("user.home");
            String filePath = userHome + "/Downloads/salary_info.csv";
            try(BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))){
                writer.write("Brut Salary,Bonus,Net Salary,Taxes");
                writer.newLine();
                for (Object obj : salaire) {
                    if (obj instanceof Salary) {
                        Salary s = (Salary) obj;
                        List<String> fields = new ArrayList<>();
                        if (s.getBrutSalary() != null) fields.add(s.getBrutSalary().toString());
                        if (s.getBonus() != null) fields.add(s.getBonus().toString());
                        if (s.getNetSalary() != null) fields.add(s.getNetSalary().toString());
                        if (s.getTaxes() != null) fields.add(s.getTaxes().toString());
                        writer.write(String.join("|", fields));
                        writer.newLine();
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public static List<Attendance> attendance_info(int id_user, Connection conn) throws SQLException {
        if (conn != null) {
            LocalDate today = LocalDate.now();
            LocalDate fourDaysAgo = today.minusDays(4);

            String query = "SELECT a.*, d.day, d.month, d.year " +
                    "FROM attendance a " +
                    "JOIN date_dim d ON a.id_start_date = d.id_start_date " +
                    "WHERE a.id_user = ? AND " +
                    "STR_TO_DATE(CONCAT(d.year, '-', d.month, '-', d.day), '%Y-%m-%d') BETWEEN ? AND ?";

            PreparedStatement stm = conn.prepareStatement(query);
            stm.setInt(1, id_user);
            stm.setDate(2, Date.valueOf(fourDaysAgo));
            stm.setDate(3, Date.valueOf(today));
            ResultSet rslt = stm.executeQuery();

            List<Attendance> attendanceList = new ArrayList<>();
            while (rslt.next()) {
                Attendance a = new Attendance();
                a.setIdAttendance(rslt.getInt("id_attendance"));
                a.setStatus(rslt.getString("status"));
                a.setRemarks(rslt.getString("remarks"));
                a.setCheckinTime(rslt.getTimestamp("checkin_time"));
                a.setCheckoutTime(rslt.getTimestamp("checkout_time"));
                a.setUser(null);

                DateDim datee = new DateDim();
                datee.setDay(rslt.getInt("day"));
                datee.setMonth(rslt.getInt("month"));
                datee.setYear(rslt.getInt("year"));
                a.setDateDim(datee);

                attendanceList.add(a);
            }
            return attendanceList;
        }
        return new ArrayList<>();
    }

    public static Attendance current_attendance(int id_user, Connection conn, int id_date) throws SQLException {
        if (conn != null) {
            String query = "SELECT a.*, d.day, d.month, d.year " +
                    "FROM attendance a " +
                    "JOIN date_dim d ON a.id_start_date = d.id_start_date " +
                    "WHERE a.id_user = ? AND a.id_start_date = ?";

            PreparedStatement stm = conn.prepareStatement(query);
            stm.setInt(1, id_user);
            stm.setInt(2, id_date);
            ResultSet rslt = stm.executeQuery();

            if (rslt.next()) {
                Attendance att_today = new Attendance();
                att_today.setIdAttendance(rslt.getInt("id_attendance"));
                att_today.setStatus(rslt.getString("status"));
                att_today.setRemarks(rslt.getString("remarks"));
                att_today.setCheckinTime(rslt.getTimestamp("checkin_time"));
                att_today.setCheckoutTime(rslt.getTimestamp("checkout_time"));
                att_today.setUser(null);

                DateDim datee = new DateDim();
                datee.setDay(rslt.getInt("day"));
                datee.setMonth(rslt.getInt("month"));
                datee.setYear(rslt.getInt("year"));
                att_today.setDateDim(datee);

                return att_today;
            }
        }
        return null;
    }
    public static void checkIn(int id_user, Connection conn, int id_date) throws SQLException {
        // Get the current time
        LocalTime now = LocalTime.now();
        LocalTime fourPm = LocalTime.of(16, 0);  // 4:00 PM

        // Check if the user already has an attendance record for the given date
        String checkQuery = "SELECT * FROM attendance WHERE id_user = ? AND id_start_date = ?";
        PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
        checkStmt.setInt(1, id_user);
        checkStmt.setInt(2, id_date);
        ResultSet rs = checkStmt.executeQuery();

        if (rs.next()) {
            // If the user exists and the status is "pending", update the status to "present"
            String status = rs.getString("status");
            if ("pending".equals(status)) {
                String updateQuery = "UPDATE attendance SET status = ?, checkin_time = ? WHERE id_user = ? AND id_start_date = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                updateStmt.setString(1, "present");
                updateStmt.setTimestamp(2, Timestamp.valueOf(LocalDate.now().atTime(now)));
                updateStmt.setInt(3, id_user);
                updateStmt.setInt(4, id_date);
                updateStmt.executeUpdate();
            } else if (now.isAfter(fourPm) && "pending".equals(status)) {
                // If time has passed 4 PM and button hasn't been clicked, update status to "absent"
                String updateQuery = "UPDATE attendance SET status = ? WHERE id_user = ? AND id_start_date = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                updateStmt.setString(1, "absent");
                updateStmt.setInt(2, id_user);
                updateStmt.setInt(3, id_date);
                updateStmt.executeUpdate();
            }
        } else {
            // If no record exists for the user on that date, create a new one with status "present"
            String insertQuery = "INSERT INTO attendance (id_user, id_start_date, status, checkin_time) VALUES (?, ?, ?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
            insertStmt.setInt(1, id_user);
            insertStmt.setInt(2, id_date);
            insertStmt.setString(3, "present");
            insertStmt.setTimestamp(4, Timestamp.valueOf(LocalDate.now().atTime(now)));
            insertStmt.executeUpdate();
        }
    }

    public static void checkOut(int id_user, Connection conn, int id_date) throws SQLException {
        // Get the current time
        LocalTime now = LocalTime.now();

        // Check if the user has a record for check-in
        String checkQuery = "SELECT * FROM attendance WHERE id_user = ? AND id_start_date = ?";
        PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
        checkStmt.setInt(1, id_user);
        checkStmt.setInt(2, id_date);
        ResultSet rs = checkStmt.executeQuery();

        if (rs.next()) {
            // If a record exists, update the checkout time
            String updateQuery = "UPDATE attendance SET checkout_time = ? WHERE id_user = ? AND id_start_date = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
            updateStmt.setTimestamp(1, Timestamp.valueOf(LocalDate.now().atTime(now)));
            updateStmt.setInt(2, id_user);
            updateStmt.setInt(3, id_date);
            updateStmt.executeUpdate();
        }
    }
    public static ArrayList<Object> getUserAndCompteInfo(int idUser, Connection conn) throws SQLException {
        ArrayList<Object> userAndCompteList = new ArrayList<>();

        // Query to get user info
        String userQuery = "SELECT * FROM users WHERE id_user = ?";
        PreparedStatement userStmt = conn.prepareStatement(userQuery);
        userStmt.setInt(1, idUser);
        ResultSet userResult = userStmt.executeQuery();

        if (userResult.next()) {
            // Create User object and set properties from result set
            User user = new User();
            user.setIdUser(userResult.getInt("id_user"));
            user.setRoleAdminOrEmployee(userResult.getString("role_admin_or_employee"));
            user.setFirstName(userResult.getString("first_name"));
            user.setLastName(userResult.getString("last_name"));
            user.setEmailAddress(userResult.getString("email_address"));
            user.setPhoneNumber(userResult.getString("phone_number"));
            user.setAddress(userResult.getString("address"));
            user.setDateOfBirth(userResult.getDate("date_of_birth"));
            user.setHireDate(userResult.getDate("hire_date"));
            user.setJobTitle(userResult.getString("job_title"));
            int id_dep = userResult.getInt("id_department");
            String query = "SELECT name FROM department WHERE id_department = ?";
            PreparedStatement s = conn.prepareStatement(query);
            s.setInt(1,id_dep);
            ResultSet r = s.executeQuery();
            Department dep = new Department();
            if (r.next()) {
                String name = r.getString("name");
                dep.setIdDepartment(id_dep);
                dep.setName(name);
            }
            user.setDepartment(dep);
            // Query to get account info (Compte)
            String compteQuery = "SELECT * FROM compte WHERE id_user = ?";
            PreparedStatement compteStmt = conn.prepareStatement(compteQuery);
            compteStmt.setInt(1, idUser);
            ResultSet compteResult = compteStmt.executeQuery();

            if (compteResult.next()) {
                // Create Compte object and set properties from result set
                Compte compte = new Compte();
                compte.setIdCompte(compteResult.getInt("id_compte"));
                compte.setLogin(compteResult.getString("login"));
                compte.setPassword(compteResult.getString("password"));

                // Set the user object inside the compte (Foreign key relationship)
                compte.setUser(user);

                // Add both User and Compte objects to the list
                userAndCompteList.add(user);
                userAndCompteList.add(compte);
            }
        }

        return userAndCompteList;
    }
    public static ObservableList<VacationLeave> getLeaves(int idUser, Connection conn) throws Exception {
        ObservableList<VacationLeave> leave_list = FXCollections.observableArrayList();
        String query = "SELECT * FROM vacation_leave WHERE id_user = ?";
        var stm = conn.prepareStatement(query);
        stm.setInt(1, idUser);
        var rs = stm.executeQuery();

        while (rs.next()) {
            java.time.LocalDate today = java.time.LocalDate.now();
            java.sql.Date start_date = rs.getDate("start_date");

            if (!start_date.toLocalDate().isBefore(today)) {
                VacationLeave v = new VacationLeave();
                v.setIdVacationLeave(rs.getInt("id_vacation_leave"));
                v.setStartDate(start_date);
                v.setEndDate(rs.getDate("end_date"));
                v.setStatusOkNo(rs.getString("status_ok_no"));

                int id_type = rs.getInt("id_leave_type");
                var typeStm = conn.prepareStatement("SELECT * FROM leave_type WHERE id_leave_type = ?");
                typeStm.setInt(1, id_type);
                var rs1 = typeStm.executeQuery();
                if (rs1.next()) {
                    LeaveType lt = new LeaveType();
                    lt.setIdLeaveType(rs1.getInt("id_leave_type"));
                    lt.setVacationType(rs1.getString("vacation_type"));
                    v.setLeaveType(lt);
                }
                leave_list.add(v);
            }
        }
        return leave_list;
    }
    public static ObservableList<LeaveType> getLeaveTypes(Connection conn) throws SQLException {
        PreparedStatement stm = conn.prepareStatement("SELECT * FROM leave_type ");
        ResultSet rs = stm.executeQuery();
        ObservableList<LeaveType> liste = FXCollections.observableArrayList() ;
        while(rs.next()){
            LeaveType t = new LeaveType();
            t.setIdLeaveType(rs.getInt("id_leave_type"));
            t.setVacationType(rs.getString("vacation_type"));
            liste.add(t);
        }
        return liste;
    }
    public static boolean addleaves(int idUser, Connection conn, LeaveType type, LocalDate s_date, LocalDate e_date) throws SQLException {
        // Ensure start date is not after end date
        if (s_date.isAfter(e_date)) {
            System.out.println("Start date cannot be after end date.");
            return false;
        }

        String checkQuery = "SELECT COUNT(*) FROM vacation_leave " +
                "WHERE id_user = ? AND NOT (end_date < ? OR start_date > ?)";

        try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
            checkStmt.setInt(1, idUser);
            checkStmt.setDate(2, java.sql.Date.valueOf(s_date));
            checkStmt.setDate(3, java.sql.Date.valueOf(e_date));

            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            int count = rs.getInt(1);

            if (count > 0) {
                System.out.println("Leave request conflicts with existing leave.");
                return false;
            }
        }

        // If no conflict, insert the leave request
        String insertQuery = "INSERT INTO vacation_leave (status_ok_no, start_date, end_date, id_user, id_leave_type) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
            insertStmt.setString(1, ""); // Better than empty string
            insertStmt.setDate(2, java.sql.Date.valueOf(s_date));
            insertStmt.setDate(3, java.sql.Date.valueOf(e_date));
            insertStmt.setInt(4, idUser);
            insertStmt.setInt(5, type.getIdLeaveType());

            insertStmt.executeUpdate();
            System.out.println("Leave request submitted successfully.");
            return true;
        }
    }


}
