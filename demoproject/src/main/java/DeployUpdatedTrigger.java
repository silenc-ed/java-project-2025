import java.sql.Connection;
import java.sql.Statement;

public class DeployUpdatedTrigger {
    public static void main(String[] args) {
        System.out.println("Deploying updated warranty trigger...");
        String triggerSql = 
            "CREATE OR REPLACE TRIGGER TRG_HD_TU_KICH_HOAT_BH\n" +
            "AFTER UPDATE OF TRANG_THAI ON HOA_DON\n" +
            "FOR EACH ROW\n" +
            "WHEN (\n" +
            "    (NEW.TRANG_THAI = N'Hoàn thành' OR NEW.TRANG_THAI = N'Đã thanh toán')\n" +
            "    AND OLD.TRANG_THAI NOT IN (N'Hoàn thành', N'Đã thanh toán')\n" +
            ")\n" +
            "DECLARE\n" +
            "    v_count NUMBER;\n" +
            "BEGIN\n" +
            "    FOR rec IN (\n" +
            "        SELECT CTHD.SERIAL_NUMBER, SP.THOI_GIAN_BH\n" +
            "        FROM CHI_TIET_HOA_DON CTHD\n" +
            "        JOIN SAN_PHAM SP ON CTHD.MA_SP = SP.MA_SP\n" +
            "        WHERE CTHD.MA_HD = :NEW.MA_HD\n" +
            "          AND CTHD.SERIAL_NUMBER IS NOT NULL\n" +
            "          AND SP.THOI_GIAN_BH > 0\n" +
            "    ) LOOP\n" +
            "        SELECT COUNT(*) INTO v_count FROM BAO_HANH WHERE SERIAL_NUMBER = rec.SERIAL_NUMBER;\n" +
            "        IF v_count = 0 THEN\n" +
            "            INSERT INTO BAO_HANH (SERIAL_NUMBER, MA_HD, NGAY_BAT_DAU, NGAY_KET_THUC, TRANG_THAI, GHI_CHU)\n" +
            "            VALUES (\n" +
            "                rec.SERIAL_NUMBER, \n" +
            "                :NEW.MA_HD, \n" +
            "                SYSDATE, \n" +
            "                ADD_MONTHS(SYSDATE, rec.THOI_GIAN_BH), \n" +
            "                N'Hiệu lực', \n" +
            "                N'Kích hoạt tự động khi hóa đơn ' || :NEW.MA_HD || N' chuyển sang ' || :NEW.TRANG_THAI\n" +
            "            );\n" +
            "        END IF;\n" +
            "    END LOOP;\n" +
            "EXCEPTION\n" +
            "    WHEN OTHERS THEN\n" +
            "        SP_GHI_LOG_LOI('TRG_HD_TU_KICH_HOAT_BH', SQLCODE, SQLERRM);\n" +
            "END;";

        try (Connection con = ConnectDB.ConnectionUtils.getMyConnection();
             Statement stmt = con.createStatement()) {
            stmt.execute(triggerSql);
            System.out.println("SUCCESS: Updated TRG_HD_TU_KICH_HOAT_BH created!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
