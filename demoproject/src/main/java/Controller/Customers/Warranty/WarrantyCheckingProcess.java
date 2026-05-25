package Controller.Customers.Warranty;

import java.util.Map;

public class WarrantyCheckingProcess {
    
    public Map<String, Object> getWarrantyDetails(String imei) throws Exception {
        return Controller.Admin.BaoHanh.BaoHanhDAO.getWarrantyDetails(imei);
    }
}
