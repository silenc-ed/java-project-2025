package View.Admin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class UIUtils {

    /**
     * Styles a JButton based on its text, using a flat design standard.
     */
    public static void styleButton(JButton btn) {
        // Flat Look & Feel settings
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        
        // Default flat border for spacing
        btn.setBorder(new EmptyBorder(8, 15, 8, 15));

        String text = btn.getText() != null ? btn.getText().toLowerCase() : "";

        // Assign colors based on functional keywords
        if (text.contains("xóa") || text.contains("✕") || text.contains("✖")) {
            btn.setBackground(new Color(220, 53, 69)); // Red (Delete)
        } else if (text.contains("thêm") || text.contains("lưu") || text.contains("➕")) {
            btn.setBackground(new Color(40, 167, 69)); // Green (Add/Save)
        } else if (text.contains("sửa") || text.contains("lọc") || text.contains("🔍") || text.contains("✏")) {
            btn.setBackground(new Color(0, 123, 255)); // Blue (Edit/Filter/Search)
        } else if (text.contains("cập nhật") || text.contains("đổi") || text.contains("chuyển") || text.contains("loại sản phẩm") || text.contains("sản phẩm") || text.contains("↻") || text.contains("chi tiết")) {
            btn.setBackground(new Color(111, 66, 193)); // Purple (Update/Switch/Details)
        } else if (text.contains("hủy") || text.contains("quay lại") || text.contains("hủy bỏ")) {
            btn.setBackground(new Color(108, 117, 125)); // Gray (Cancel/Back)
        } else {
            btn.setBackground(new Color(23, 162, 184)); // Cyan (Default info)
        }
    }
}
