import java.sql.*;

public class TestJDBC {
    public static void main(String[] args) throws Exception {
        System.out.println("Installing Trigger...");
        String triggerSql = 
            "CREATE OR REPLACE TRIGGER TRG_CTPN_CAPNHAT_KHO_TRUCTIEP\n" +
            "AFTER INSERT OR UPDATE OR DELETE ON CHI_TIET_PHIEU_NHAP\n" +
            "FOR EACH ROW\n" +
            "DECLARE\n" +
            "    v_trang_thai NUMBER;\n" +
            "    v_ma_cn NUMBER;\n" +
            "BEGIN\n" +
            "    IF INSERTING OR UPDATING THEN\n" +
            "        SELECT TRANG_THAI, MA_CN INTO v_trang_thai, v_ma_cn FROM PHIEU_NHAP WHERE MA_PN = :NEW.MA_PN;\n" +
            "        IF v_trang_thai = 1 THEN\n" +
            "            IF UPDATING THEN\n" +
            "                MERGE INTO TON_KHO tk\n" +
            "                USING DUAL ON (tk.MA_BIENTHE = :OLD.MA_BIENTHE AND tk.MA_CN = v_ma_cn)\n" +
            "                WHEN MATCHED THEN UPDATE SET SO_LUONG_TON = SO_LUONG_TON - :OLD.SO_LUONG;\n" +
            "            END IF;\n" +
            "            MERGE INTO TON_KHO tk\n" +
            "            USING DUAL ON (tk.MA_BIENTHE = :NEW.MA_BIENTHE AND tk.MA_CN = v_ma_cn)\n" +
            "            WHEN MATCHED THEN \n" +
            "                UPDATE SET SO_LUONG_TON = SO_LUONG_TON + :NEW.SO_LUONG\n" +
            "            WHEN NOT MATCHED THEN \n" +
            "                INSERT (MA_BIENTHE, MA_CN, SO_LUONG_TON) VALUES (:NEW.MA_BIENTHE, v_ma_cn, :NEW.SO_LUONG);\n" +
            "        END IF;\n" +
            "    END IF;\n" +
            "    IF DELETING THEN\n" +
            "        SELECT TRANG_THAI, MA_CN INTO v_trang_thai, v_ma_cn FROM PHIEU_NHAP WHERE MA_PN = :OLD.MA_PN;\n" +
            "        IF v_trang_thai = 1 THEN\n" +
            "            MERGE INTO TON_KHO tk\n" +
            "            USING DUAL ON (tk.MA_BIENTHE = :OLD.MA_BIENTHE AND tk.MA_CN = v_ma_cn)\n" +
            "            WHEN MATCHED THEN UPDATE SET SO_LUONG_TON = SO_LUONG_TON - :OLD.SO_LUONG;\n" +
            "        END IF;\n" +
            "    END IF;\n" +
            "END;";
        try (Connection con = ConnectDB.ConnectionUtils.getMyConnection();
             Statement stmt = con.createStatement()) {
            stmt.execute(triggerSql);
            System.out.println("Success creating Trigger!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

