package ConnectDB;

import java.sql.*;

public class TestDB {
    public static void main(String[] args) {
        try (Connection conn = ConnectionOracle.getOracleConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            System.out.println("--- TABLES ---");
            ResultSet rs = meta.getTables(null, "DOAN", "%", new String[]{"TABLE"});
            while (rs.next()) {
                System.out.println(rs.getString("TABLE_NAME"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
