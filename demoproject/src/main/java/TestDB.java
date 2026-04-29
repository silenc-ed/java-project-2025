import ConnectDB.ConnectionUtils;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

public class TestDB {
    public static void main(String[] args) {
        try (Connection con = ConnectionUtils.getMyConnection()) {
            DatabaseMetaData metaData = con.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, "NHANVIEN", null);
            while (columns.next()) {
                System.out.println(columns.getString("COLUMN_NAME"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
