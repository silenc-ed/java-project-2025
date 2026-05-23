import java.sql.*;

public class TestJDBC {
    public static void main(String[] args) throws Exception {
        System.out.println("Testing savePhieuNhap...");
        try {
            Controller.Admin.PhieuNhap.PhieuNhapDAO.savePhieuNhap(-1, "Nha cung cap test", 1, 1, 0, 0, 0, 1, "test", false);
            System.out.println("Success savePhieuNhap");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
