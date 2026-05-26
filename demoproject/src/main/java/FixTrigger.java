import ConnectDB.ConnectionUtils;
import java.sql.Connection;
import java.sql.Statement;

public class FixTrigger {
    public static void main(String[] args) {
        try (Connection con = ConnectionUtils.getMyConnection();
             Statement st = con.createStatement()) {
            
            String triggerSQL = "CREATE OR REPLACE TRIGGER TRG_KM_TRANG_THAI\n" +
                                "BEFORE UPDATE ON KHUYEN_MAI\n" +
                                "FOR EACH ROW\n" +
                                "BEGIN\n" +
                                "    IF :NEW.NGAY_KET_THUC < SYSTIMESTAMP THEN\n" +
                                "        :NEW.TRANG_THAI := 'Hết hạn';\n" +
                                "    ELSIF :NEW.SO_LUONG_CL <= 0 THEN\n" +
                                "        :NEW.TRANG_THAI := 'Hết lượt';\n" +
                                "    END IF;\n" +
                                "END;";
            st.execute(triggerSQL);
            System.out.println("Trigger TRG_KM_TRANG_THAI created/recompiled successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
