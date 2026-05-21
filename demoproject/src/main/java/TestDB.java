import ConnectDB.ConnectionUtils;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

public class TestDB {
    public static void main(String[] args) {
        try {
            Connection conn = ConnectionUtils.getMyConnection();
            DatabaseMetaData metaData = conn.getMetaData();
            String[] sqlFileTables = {
                "CA_LAMVIEC", "CHINHANH", "NHACUNGCAP", "KHACHHANG", "LOAI_SANPHAM", 
                "LOAI_KHUYENMAI", "CHUCNANG", "ROLE_GROUP", "DICHVU", "SANPHAM", 
                "KHUYENMAI", "VAITRO", "NHANVIEN", "TAIKHOAN", "ROLEGROUP_ASSIGN_ROLE", 
                "ACCOUNT_ASSIGN_ROLEGROUP", "ACCOUNT_ASSIGN_ROLE", "LICH_LAMVIEC", 
                "TONKHO", "PHIEU_NHAP", "BIENTHE_SANPHAM", "CHITIET_PHIEUNHAP", 
                "KHO_SERIAL", "HOADON", "CHITIET_HOADON", "PHIEU_DICH_VU", 
                "CHITIET_SUDUNG_DICHVU", "PHIEU_SUA_CHUA", "CHITIET_SUDUNG_LINHKIEN", 
                "BAOHANH", "ACCOUNT_TOKEN", "CHAMCONG", "VI_KHUYENMAI"
            };
            
            System.out.println("Scanning tables:");
            for (String tbl : sqlFileTables) {
                // Check original
                boolean origExists = false;
                try (ResultSet rs = metaData.getTables(null, "ADMINTESTINGVN", tbl, new String[]{"TABLE"})) {
                    if (rs.next()) origExists = true;
                }
                
                // Check underscore variation (e.g. CA_LAM_VIEC)
                String varTbl = tbl.replace("LAMVIEC", "LAM_VIEC")
                                   .replace("CHINHANH", "CHI_NHANH")
                                   .replace("NHACUNGCAP", "NHA_CUNG_CAP")
                                   .replace("KHACHHANG", "KHACH_HANG")
                                   .replace("SANPHAM", "SAN_PHAM")
                                   .replace("KHUYENMAI", "KHUYEN_MAI")
                                   .replace("VAITRO", "VAI_TRO")
                                   .replace("NHANVIEN", "NHAN_VIEN")
                                   .replace("TAIKHOAN", "TAI_KHOAN")
                                   .replace("TONKHO", "TON_KHO")
                                   .replace("BIENTHE", "BIEN_THE")
                                   .replace("CHITIET", "CHI_TIET")
                                   .replace("HOADON", "HOA_DON")
                                   .replace("DICHVU", "DICH_VU")
                                   .replace("CHUCNANG", "CHUC_NANG")
                                   .replace("BAOHANH", "BAO_HANH")
                                   .replace("CHAMCONG", "CHAM_CONG")
                                   .replace("SUDUNG", "SU_DUNG")
                                   .replace("LINHKIEN", "LINH_KIEN")
                                   .replace("PHIEUNHAP", "PHIEU_NHAP");
                                   
                boolean varExists = false;
                if (!tbl.equals(varTbl)) {
                    try (ResultSet rs = metaData.getTables(null, "ADMINTESTINGVN", varTbl, new String[]{"TABLE"})) {
                        if (rs.next()) varExists = true;
                    }
                }
                
                System.out.printf("  %s: %s | %s: %s\n", 
                    tbl, origExists ? "YES" : "NO", 
                    varTbl, varExists ? "YES" : "NO");
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
