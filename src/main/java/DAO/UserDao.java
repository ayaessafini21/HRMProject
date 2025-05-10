package DAO;

import hrm.project.hrmproject.modules.*;
import hrm.project.hrmproject.utils.DbConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UserDao {

  /*  @Override
    public void create(User user) {
        String query = "INSERT INTO User (id_user, role_admin_or_employee, first_name, last_name, email_address, phone_number, address, date_of_birth, hire_date, job_title, id_department) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = DbConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setInt(1, user.getIdUser());
                stmt.setString(2, user.getRoleAdminOrEmployee());
                stmt.setString(3, user.getFirstName());
                stmt.setString(4, user.getLastName());
                stmt.setString(5, user.getEmailAddress());
                stmt.setString(6, user.getPhoneNumber());
                stmt.setString(7, user.getAddress());
                stmt.setDate(8, user.getDateOfBirth());
                stmt.setDate(9, user.getHireDate());
                stmt.setString(10, user.getJobTitle());

                stmt.setObject(11, user.getDepartment());
                stmt.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }*/
}
