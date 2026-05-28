import ConnectDB.ConnectionUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CheckDb {
    public static void main(String[] args) {
        try (Connection con = ConnectionUtils.getMyConnection()) {
            String sql = "SELECT CONSTRAINT_NAME, SEARCH_CONDITION FROM USER_CONSTRAINTS WHERE TABLE_NAME = 'PHIEU_NHAP' AND CONSTRAINT_TYPE = 'C'";
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.println("Constraint: " + rs.getString("CONSTRAINT_NAME") + " | Condition: " + rs.getString("SEARCH_CONDITION"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
