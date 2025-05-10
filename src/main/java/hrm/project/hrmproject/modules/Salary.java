package hrm.project.hrmproject.modules;
import java.math.BigDecimal;
public class Salary {
    private int idSalary;
    private BigDecimal brutSalary;
    private BigDecimal bonus;
    private BigDecimal taxes;
    private BigDecimal netSalary;
    private User user; // Foreign key reference to User

    // Getters and Setters
    public int getIdSalary() {
        return idSalary;
    }

    public void setIdSalary(int idSalary) {
        this.idSalary = idSalary;
    }

    public BigDecimal getBrutSalary() {
        return brutSalary;
    }

    public void setBrutSalary(BigDecimal brutSalary) {
        this.brutSalary = brutSalary;
    }

    public BigDecimal getBonus() {
        return bonus;
    }

    public void setBonus(BigDecimal bonus) {
        this.bonus = bonus;
    }

    public BigDecimal getTaxes() {
        return taxes;
    }

    public void setTaxes(BigDecimal taxes) {
        this.taxes = taxes;
    }

    public BigDecimal getNetSalary() {
        return netSalary;
    }

    public void setNetSalary(BigDecimal netSalary) {
        this.netSalary = netSalary;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
