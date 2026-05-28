/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ConnectDB;

import java.sql.*;

/**
 * Class Connection Oracle
 * @author nguyenminhnhut
 */
public class ConnectionOracle {
    
    /**
     * Function get Oracle Connection 
     * 
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException 
     */
    public static Connection getOracleConnection() throws ClassNotFoundException,
            SQLException {

        String connectionURL = Common.ConfigHelper.getProperty("db.url");
        String userName = Common.ConfigHelper.getProperty("db.username");
        String password = Common.ConfigHelper.getProperty("db.password");

        // Khai báo class Driver cho DB Oracle
        Class.forName("oracle.jdbc.driver.OracleDriver");

        //Tạo đối tượng connection
        Connection conn = DriverManager.getConnection(connectionURL, userName, password);
        
        return conn;
    }


}
