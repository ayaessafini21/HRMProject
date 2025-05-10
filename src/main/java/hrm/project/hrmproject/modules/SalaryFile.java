package hrm.project.hrmproject.modules;
import java.sql.Date;
public class SalaryFile {
    private int idSalaryFile;
    private String filePath;
    private Date downloadDate;
    private User user; // Foreign key reference to User

    // Getters and Setters
    public int getIdSalaryFile() {
        return idSalaryFile;
    }

    public void setIdSalaryFile(int idSalaryFile) {
        this.idSalaryFile = idSalaryFile;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Date getDownloadDate() {
        return downloadDate;
    }

    public void setDownloadDate(Date downloadDate) {
        this.downloadDate = downloadDate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}

