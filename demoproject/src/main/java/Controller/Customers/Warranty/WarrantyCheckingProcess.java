package Controller.Customers.Warranty;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Date;
import ConnectDB.ConnectionUtils;

public class WarrantyCheckingProcess {
    
    public String checking(String sdt, String imei, Date[] dates) throws Exception {
        return Controller.Admin.BaoHanh.BaoHanhDAO.checking(sdt, imei, dates);
    }
}
