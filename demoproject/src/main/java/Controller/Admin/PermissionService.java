package Controller.Admin;

import ConnectDB.ConnectionUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * PermissionService - Quản lý quyền hạn của người dùng đang đăng nhập.
 * Load quyền từ DB dựa trên token hiện tại và cache lại trong phiên làm việc.
 *
 * Quyền được tổng hợp từ 2 nguồn (UNION/OR):
 *   1. Qua ROLE_GROUP: ACCOUNT_ASSIGN_ROLEGROUP -> ROLE_GROUP_ASSIGN_ROLE -> VAI_TRO
 *   2. Trực tiếp:      ACCOUNT_ASSIGN_ROLE -> VAI_TRO
 */
public class PermissionService {

    /**
     * Model lưu quyền hạn cho 1 chức năng.
     */
    public static class Permission {
        public final String tenChucNang;
        public final boolean duocXem;
        public final boolean duocThem;
        public final boolean duocSua;
        public final boolean duocXoa;

        public Permission(String tenChucNang,
                          boolean duocXem, boolean duocThem,
                          boolean duocSua, boolean duocXoa) {
            this.tenChucNang = tenChucNang;
            this.duocXem   = duocXem;
            this.duocThem  = duocThem;
            this.duocSua   = duocSua;
            this.duocXoa   = duocXoa;
        }
    }

    // Cache: TEN_HIEN_THI (lowercase) -> Permission
    private static Map<String, Permission> permissionCache = null;

    // Trạng thái: có phải Admin toàn quyền không (ví dụ khi load thất bại)
    private static boolean isFullAccess = false;

    // ─── Public API ────────────────────────────────────────────────────────────

    /**
     * Load quyền từ DB và lưu vào cache.
     * Nên được gọi ngay sau khi đăng nhập thành công.
     */
    public static void loadPermissions() {
        permissionCache = new LinkedHashMap<>();
        isFullAccess = false;

        String tokenValue = Common.TokenManager.getLocalToken();
        if (tokenValue == null || tokenValue.isEmpty()) {
            System.out.println("[PermissionService] Không tìm thấy token, cấp toàn quyền mặc định.");
            isFullAccess = true;
            return;
        }

        String sql =
            "SELECT cn.TEN_HIEN_THI, " +
            "       MAX(vt.DUOC_XEM)  AS DUOC_XEM, " +
            "       MAX(vt.DUOC_THEM) AS DUOC_THEM, " +
            "       MAX(vt.DUOC_SUA)  AS DUOC_SUA, " +
            "       MAX(vt.DUOC_XOA)  AS DUOC_XOA " +
            "FROM ( " +
            "    SELECT vt.MA_CHUC_NANG, vt.DUOC_XEM, vt.DUOC_THEM, vt.DUOC_SUA, vt.DUOC_XOA " +
            "    FROM ACCOUNT_TOKEN atok " +
            "    JOIN TAI_KHOAN tk ON atok.MA_TK = tk.MA_TK " +
            "    JOIN ACCOUNT_ASSIGN_ROLEGROUP aarg ON tk.MA_TK = aarg.MA_TK " +
            "    JOIN ROLE_GROUP_ASSIGN_ROLE rgar ON aarg.MA_ROLEGRP = rgar.MA_ROLEGRP " +
            "    JOIN VAI_TRO vt ON rgar.MA_ROLE = vt.MA_ROLE " +
            "    WHERE atok.TOKEN_VALUE = ? AND atok.TRANG_THAI = 'Y' " +
            "      AND atok.THOI_GIAN_HET_HAN > CURRENT_TIMESTAMP " +
            "    UNION ALL " +
            "    SELECT vt.MA_CHUC_NANG, vt.DUOC_XEM, vt.DUOC_THEM, vt.DUOC_SUA, vt.DUOC_XOA " +
            "    FROM ACCOUNT_TOKEN atok " +
            "    JOIN TAI_KHOAN tk ON atok.MA_TK = tk.MA_TK " +
            "    JOIN ACCOUNT_ASSIGN_ROLE aar ON tk.MA_TK = aar.MA_TK " +
            "    JOIN VAI_TRO vt ON aar.MA_ROLE = vt.MA_ROLE " +
            "    WHERE atok.TOKEN_VALUE = ? AND atok.TRANG_THAI = 'Y' " +
            "      AND atok.THOI_GIAN_HET_HAN > CURRENT_TIMESTAMP " +
            ") vt " +
            "JOIN CHUC_NANG cn ON vt.MA_CHUC_NANG = cn.MA_CHUC_NANG " +
            "GROUP BY cn.TEN_HIEN_THI";

        try (Connection con = ConnectionUtils.getMyConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, tokenValue);
            ps.setString(2, tokenValue);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String ten = rs.getString("TEN_HIEN_THI");
                    boolean xem  = rs.getInt("DUOC_XEM")  == 1;
                    boolean them = rs.getInt("DUOC_THEM") == 1;
                    boolean sua  = rs.getInt("DUOC_SUA")  == 1;
                    boolean xoa  = rs.getInt("DUOC_XOA")  == 1;

                    permissionCache.put(ten.toLowerCase(), new Permission(ten, xem, them, sua, xoa));
                }
            }

            System.out.println("[PermissionService] Đã load " + permissionCache.size() + " quyền.");

            // Nếu không có quyền nào -> có thể là Admin hệ thống hoặc chưa cấu hình
            if (permissionCache.isEmpty()) {
                System.out.println("[PermissionService] Không tìm thấy quyền trong DB, cấp toàn quyền.");
                isFullAccess = true;
            }

        } catch (Exception ex) {
            System.err.println("[PermissionService] Lỗi load quyền: " + ex.getMessage());
            // Fallback: cấp toàn quyền để tránh chặn người dùng
            isFullAccess = true;
        }
    }

    /**
     * Xóa cache khi đăng xuất.
     */
    public static void clearCache() {
        permissionCache = null;
        isFullAccess = false;
    }

    // ─── Kiểm tra quyền cụ thể ────────────────────────────────────────────────

    /** @param tenChucNang là TEN_HIEN_THI trong bảng CHUC_NANG */
    public static boolean canView(String tenChucNang) {
        if (isFullAccess || permissionCache == null) return true;
        Permission p = permissionCache.get(tenChucNang.toLowerCase());
        return p != null && p.duocXem;
    }

    public static boolean canAdd(String tenChucNang) {
        if (isFullAccess || permissionCache == null) return true;
        Permission p = permissionCache.get(tenChucNang.toLowerCase());
        return p != null && p.duocThem;
    }

    public static boolean canEdit(String tenChucNang) {
        if (isFullAccess || permissionCache == null) return true;
        Permission p = permissionCache.get(tenChucNang.toLowerCase());
        return p != null && p.duocSua;
    }

    public static boolean canDelete(String tenChucNang) {
        if (isFullAccess || permissionCache == null) return true;
        Permission p = permissionCache.get(tenChucNang.toLowerCase());
        return p != null && p.duocXoa;
    }

    /**
     * Trả về danh sách TEN_HIEN_THI mà người dùng được phép xem.
     * Dùng để lọc menu.
     */
    public static List<String> getViewableFeatures() {
        List<String> result = new ArrayList<>();
        if (isFullAccess || permissionCache == null) return null; // null = toàn quyền

        for (Permission p : permissionCache.values()) {
            if (p.duocXem) {
                result.add(p.tenChucNang);
            }
        }
        return result;
    }

    /**
     * Kiểm tra xem người dùng hiện tại có phải là Quản lý/Admin không
     * (bằng cách kiểm tra xem có đầy đủ 4 quyền XEM, THÊM, SỬA, XÓA đối với mục Chấm công)
     */
    public static boolean isAdminOrManager() {
        if (isFullAccess || permissionCache == null) return true;
        return canView("Cham cong") && canAdd("Cham cong") && canEdit("Cham cong") && canDelete("Cham cong");
    }

    /**
     * Kiểm tra xem đã load quyền chưa.
     */
    public static boolean isLoaded() {
        return permissionCache != null || isFullAccess;
    }
}
