import ConnectDB.ConnectionUtils;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestDB {
    public static void main(String[] args) {
        try {
            Connection conn = ConnectionUtils.getMyConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT column_name FROM all_tab_columns WHERE table_name = 'BIENTHE_SANPHAM'");
            System.out.println("Columns in BIENTHE_SANPHAM:");
            while (rs.next()) {
                System.out.println(rs.getString("column_name"));
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
