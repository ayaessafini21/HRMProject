package hrm.project.hrmproject.modules;
import java.sql.Timestamp;
public class Attendance {
    private int idAttendance;
    private String status;
    private String remarks;
    private Timestamp checkinTime;
    private Timestamp checkoutTime;
    private User user; // Foreign key reference to User
    private DateDim dateDim; // Foreign key reference to DateDim

    // Getters and Setters
    public int getIdAttendance() {
        return idAttendance;
    }

    public void setIdAttendance(int idAttendance) {
        this.idAttendance = idAttendance;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Timestamp getCheckinTime() {
        return checkinTime;
    }

    public void setCheckinTime(Timestamp checkinTime) {
        this.checkinTime = checkinTime;
    }

    public Timestamp getCheckoutTime() {
        return checkoutTime;
    }

    public void setCheckoutTime(Timestamp checkoutTime) {
        this.checkoutTime = checkoutTime;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public DateDim getDateDim() {
        return dateDim;
    }

    public void setDateDim(DateDim dateDim) {
        this.dateDim = dateDim;
    }
}
