package hrm.project.hrmproject.modules;
import java.sql.Date;
public class VacationLeave {
    private int idVacationLeave;
    private String statusOkNo;
    private Date startDate;
    private Date endDate;
    private User user; // Foreign key reference to User
    private LeaveType leaveType; // Foreign key reference to LeaveType

    // Getters and Setters
    public int getIdVacationLeave() {
        return idVacationLeave;
    }

    public void setIdVacationLeave(int idVacationLeave) {
        this.idVacationLeave = idVacationLeave;
    }

    public String getStatusOkNo() {
        return statusOkNo;
    }

    public void setStatusOkNo(String statusOkNo) {
        this.statusOkNo = statusOkNo;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LeaveType getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(LeaveType leaveType) {
        this.leaveType = leaveType;
    }
}

