package hrm.project.hrmproject.utils;
import java.sql.*;

public class DbConnection {
   public static Connection getConnection(){
       try{
           return DriverManager.getConnection("jdbc:mysql://localhost:3308/rhm","root","");
       }
       catch(Exception e){
           System.out.println(e);
           return null;       }
   }
}