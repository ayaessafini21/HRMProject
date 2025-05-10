module hrm.project.hrmproject {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.j;
    requires java.desktop;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome5;
    opens hrm.project.hrmproject to javafx.fxml;
    opens hrm.project.hrmproject.controllers to javafx.fxml;
    opens hrm.project.hrmproject.controllers.AdminController to javafx.fxml;
    exports hrm.project.hrmproject;
    exports hrm.project.hrmproject.controllers to javafx.fxml;
    exports hrm.project.hrmproject.controllers.AdminController to javafx.fxml;

}
