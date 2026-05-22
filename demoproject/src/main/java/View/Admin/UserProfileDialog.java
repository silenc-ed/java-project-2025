package View.Admin;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * Hộp thoại "Hồ sơ cá nhân" (User Profile Dialog).
 * Hiển thị dưới dạng modal popup với bố cục 2 cột: Avatar trái + Thông tin phải.
 *
 * @author Senior Java Desktop Developer
 */
public class UserProfileDialog extends JDialog {

    // ==================== Color Palette ====================
    private static final Color PRIMARY_PURPLE = new Color(101, 78, 163);       // #654EA3
    private static final Color LIGHT_PURPLE_BG = new Color(243, 239, 255);     // Nền tím nhạt cho phần thông tin quan trọng
    private static final Color LIGHT_PURPLE_BORDER = new Color(209, 196, 245); // Viền tím nhạt
    private static final Color TEXT_PRIMARY = new Color(33, 37, 41);           // Chữ đậm chính
    private static final Color TEXT_SECONDARY = new Color(108, 117, 125);      // Chữ phụ
    private static final Color LINK_COLOR = new Color(59, 130, 246);           // Xanh dương cho link
    private static final Color BG_WHITE = Color.WHITE;
    private static final Color FIELD_BG = new Color(248, 249, 250);            // Nền TextField readonly
    private static final Color FIELD_BORDER = new Color(222, 226, 230);        // Viền TextField
    private static final Color AVATAR_BG = new Color(101, 78, 163);            // Nền avatar

    // ==================== Font Definitions ====================
    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 20);
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_LABEL_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_FIELD = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_LINK = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_AVATAR = new Font("Segoe UI", Font.BOLD, 36);
    private static final Font FONT_INFO_LABEL = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_INFO_VALUE = new Font("Segoe UI", Font.BOLD, 13);

    // ==================== Data Fields ====================
    private String fullName = "Nguyễn Văn A";
    private String username = "nguyenvana";
    private String email = "nguyenvana@email.com";
    private String phone = "0901234567";
    private String role = "Quản lý";
    private String branchName = "Chi nhánh Quận 1";
    private String branchHotline = "1900 1234";

    // ==================== UI Components ====================
    private JTextField tfFullName;
    private JTextField tfUsername;
    private JTextField tfEmail;
    private JTextField tfPhone;
    private JLabel lblRole;
    private JLabel lblBranch;
    private JLabel lblHotline;
    private JLabel avatarLabel;
    private JLabel uploadLink;

    /**
     * Constructor tạo UserProfileDialog.
     *
     * @param parent Frame cha để center dialog
     */
    public UserProfileDialog(Frame parent) {
        super(parent, "Hồ sơ cá nhân", true);
        loadUserData();
        initUI();
        setSize(720, 470);
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    /**
     * Tải dữ liệu người dùng từ token/DB.
     * Hiện tại dùng mock data, có thể thay bằng query thực tế.
     */
    private void loadUserData() {
        // Lấy tên từ AuthProcess nếu có
        try {
            String name = Controller.SignIn.AuthProcess.getFullNameFromToken();
            if (name != null && !name.isEmpty()) {
                this.fullName = name;
            }
        } catch (Exception e) {
            // Giữ giá trị mặc định nếu không kết nối được DB
        }
    }

    /**
     * Khởi tạo toàn bộ giao diện.
     */
    private void initUI() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_WHITE);

        // ===== PANEL CHÍNH: Chia trái/phải =====
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // --- Panel Trái: Avatar ---
        JPanel leftPanel = createLeftPanel();
        leftPanel.setPreferredSize(new Dimension(245, 0)); // ~35% của 720

        // --- Panel Phải: Thông tin ---
        JPanel rightPanel = createRightPanel();

        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(rightPanel, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);
    }

    // ================================================================
    //  PANEL TRÁI (AVATAR)
    // ================================================================
    private JPanel createLeftPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(BG_WHITE);
        panel.setLayout(new GridBagLayout());
        panel.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 0, 1, new Color(233, 236, 239)),
                BorderFactory.createEmptyBorder(30, 20, 30, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 0, 0, 0);

        // --- Avatar Circle ---
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 16, 0);
        avatarLabel = createAvatarLabel();
        panel.add(avatarLabel, gbc);

        // --- Link "Tải ảnh lên" ---
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 24, 0);
        uploadLink = new JLabel("Tải ảnh lên");
        uploadLink.setFont(FONT_LINK);
        uploadLink.setForeground(LINK_COLOR);
        uploadLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        uploadLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                uploadLink.setText("<html><u>Tải ảnh lên</u></html>");
            }

            @Override
            public void mouseExited(MouseEvent e) {
                uploadLink.setText("Tải ảnh lên");
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                handleUploadAvatar();
            }
        });
        panel.add(uploadLink, gbc);

        // --- Tiêu đề "Hồ sơ cá nhân" ---
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 8, 0);
        JLabel titleLabel = new JLabel("Hồ sơ cá nhân");
        titleLabel.setFont(FONT_TITLE);
        titleLabel.setForeground(TEXT_PRIMARY);
        panel.add(titleLabel, gbc);

        // --- Sub-text ---
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 0, 0);
        JLabel subLabel = new JLabel("Thông tin tài khoản của bạn");
        subLabel.setFont(FONT_INFO_LABEL);
        subLabel.setForeground(TEXT_SECONDARY);
        panel.add(subLabel, gbc);

        return panel;
    }

    /**
     * Tạo avatar hình tròn với chữ cái đầu tiên của tên.
     */
    private JLabel createAvatarLabel() {
        String initial = fullName != null && !fullName.isEmpty()
                ? String.valueOf(fullName.charAt(0)).toUpperCase()
                : "U";

        JLabel label = new JLabel(initial) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int size = Math.min(getWidth(), getHeight());
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;

                // Vẽ shadow nhẹ
                g2.setColor(new Color(101, 78, 163, 30));
                g2.fill(new Ellipse2D.Double(x + 2, y + 2, size, size));

                // Gradient nền avatar
                GradientPaint gradient = new GradientPaint(
                        x, y, PRIMARY_PURPLE,
                        x + size, y + size, new Color(170, 143, 232)
                );
                g2.setPaint(gradient);
                g2.fill(new Ellipse2D.Double(x, y, size, size));

                // Vẽ chữ cái
                g2.setColor(Color.WHITE);
                g2.setFont(FONT_AVATAR);
                FontMetrics fm = g2.getFontMetrics();
                String text = getText();
                int textX = x + (size - fm.stringWidth(text)) / 2;
                int textY = y + (size - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(text, textX, textY);

                g2.dispose();
            }
        };

        label.setPreferredSize(new Dimension(100, 100));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);
        return label;
    }

    // ================================================================
    //  PANEL PHẢI (THÔNG TIN)
    // ================================================================
    private JPanel createRightPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(BG_WHITE);
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        // Container dọc cho 3 phần
        JPanel contentPanel = new JPanel();
        contentPanel.setBackground(BG_WHITE);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        // PHẦN 1: Thông tin cơ bản
        JPanel basicInfoPanel = createBasicInfoPanel();
        contentPanel.add(basicInfoPanel);
        contentPanel.add(Box.createVerticalStrut(18));

        // PHẦN 2: Thông tin quan trọng (highlight)
        JPanel importantInfoPanel = createImportantInfoPanel();
        contentPanel.add(importantInfoPanel);
        contentPanel.add(Box.createVerticalStrut(20));

        // PHẦN 3: Nút hành động
        JPanel actionPanel = createActionPanel();
        contentPanel.add(actionPanel);

        panel.add(contentPanel, BorderLayout.NORTH);
        return panel;
    }

    // ---------- PHẦN 1: Thông tin cơ bản ----------
    private JPanel createBasicInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_WHITE);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 0, 4, 12);

        // Row 0: Họ và Tên
        addFormRow(panel, gbc, 0, "Họ và Tên", fullName, true);
        // Row 1: Tên đăng nhập
        addFormRow(panel, gbc, 1, "Tên đăng nhập", username, false);
        // Row 2: Email
        addFormRow(panel, gbc, 2, "Email", email, false);
        // Row 3: Số điện thoại
        addFormRow(panel, gbc, 3, "Số điện thoại", phone, false);

        return panel;
    }

    /**
     * Thêm một hàng label + textfield vào GridBagLayout.
     */
    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row,
                            String labelText, String value, boolean isFirst) {
        // Label
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0;
        gbc.insets = new Insets(isFirst ? 0 : 6, 0, 6, 16);
        JLabel label = new JLabel(labelText);
        label.setFont(FONT_LABEL_BOLD);
        label.setForeground(TEXT_PRIMARY);
        label.setPreferredSize(new Dimension(110, 28));
        panel.add(label, gbc);

        // TextField (readonly)
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(isFirst ? 0 : 6, 0, 6, 0);
        JTextField tf = createStyledTextField(value);
        panel.add(tf, gbc);

        // Lưu reference
        switch (row) {
            case 0: tfFullName = tf; break;
            case 1: tfUsername = tf; break;
            case 2: tfEmail = tf; break;
            case 3: tfPhone = tf; break;
        }
    }

    /**
     * Tạo JTextField readonly với style hiện đại.
     */
    private JTextField createStyledTextField(String value) {
        JTextField tf = new JTextField(value);
        tf.setEditable(false);
        tf.setFont(FONT_FIELD);
        tf.setForeground(TEXT_PRIMARY);
        tf.setBackground(FIELD_BG);
        tf.setBorder(new CompoundBorder(
                new LineBorder(FIELD_BORDER, 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        tf.setPreferredSize(new Dimension(250, 34));
        return tf;
    }

    // ---------- PHẦN 2: Thông tin quan trọng ----------
    private JPanel createImportantInfoPanel() {
        // Panel bo góc với nền tím nhạt
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Nền tím nhạt bo góc
                g2.setColor(LIGHT_PURPLE_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);

                // Viền tím nhạt
                g2.setColor(LIGHT_PURPLE_BORDER);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);

                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Vai trò
        addInfoRow(panel, gbc, 0, "Vai trò", role);
        // Tên chi nhánh
        addInfoRow(panel, gbc, 1, "Tên chi nhánh", branchName);
        // Hotline chi nhánh
        addInfoRow(panel, gbc, 2, "Hotline chi nhánh", branchHotline);

        return panel;
    }

    /**
     * Thêm một hàng thông tin (chỉ JLabel) vào panel quan trọng.
     */
    private void addInfoRow(JPanel panel, GridBagConstraints gbc, int row,
                            String labelText, String value) {
        // Label (tên trường)
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0;
        gbc.insets = new Insets(row == 0 ? 0 : 4, 0, 4, 20);
        JLabel label = new JLabel(labelText + ":");
        label.setFont(FONT_INFO_LABEL);
        label.setForeground(TEXT_SECONDARY);
        label.setPreferredSize(new Dimension(120, 22));
        panel.add(label, gbc);

        // Value (giá trị)
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(row == 0 ? 0 : 4, 0, 4, 0);
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(FONT_INFO_VALUE);
        valueLabel.setForeground(PRIMARY_PURPLE);
        panel.add(valueLabel, gbc);

        // Lưu reference
        switch (row) {
            case 0: lblRole = valueLabel; break;
            case 1: lblBranch = valueLabel; break;
            case 2: lblHotline = valueLabel; break;
        }
    }

    // ---------- PHẦN 3: Nút hành động ----------
    private JPanel createActionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panel.setBackground(BG_WHITE);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Nút "Đổi mật khẩu" (Nền trắng, viền tím, chữ tím)
        JButton btnChangePassword = createOutlinedButton("Đổi mật khẩu");
        btnChangePassword.addActionListener(e -> handleChangePassword());

        // Nút "Chỉnh sửa hồ sơ" (Nền tím, chữ trắng)
        JButton btnEditProfile = createFilledButton("Chỉnh sửa hồ sơ");
        btnEditProfile.addActionListener(e -> handleEditProfile());

        panel.add(btnChangePassword);
        panel.add(btnEditProfile);

        return panel;
    }

    /**
     * Tạo nút với nền tím, chữ trắng (Primary button).
     */
    private JButton createFilledButton(String text) {
        JButton btn = new JButton(text) {
            private boolean hovered = false;

            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hovered = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        hovered = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color bgColor = hovered
                        ? new Color(85, 62, 147)   // Darker purple on hover
                        : PRIMARY_PURPLE;

                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(getText(), x, y);

                g2.dispose();
            }
        };

        btn.setFont(FONT_BUTTON);
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(155, 38));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return btn;
    }

    /**
     * Tạo nút với nền trắng, viền tím, chữ tím (Outlined button).
     */
    private JButton createOutlinedButton(String text) {
        JButton btn = new JButton(text) {
            private boolean hovered = false;

            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hovered = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        hovered = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Background
                Color bgColor = hovered
                        ? new Color(243, 239, 255)  // Light purple hover
                        : Color.WHITE;
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                // Border
                g2.setColor(PRIMARY_PURPLE);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);

                // Text
                g2.setColor(PRIMARY_PURPLE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(getText(), x, y);

                g2.dispose();
            }
        };

        btn.setFont(FONT_BUTTON);
        btn.setForeground(PRIMARY_PURPLE);
        btn.setPreferredSize(new Dimension(145, 38));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return btn;
    }

    // ================================================================
    //  EVENT HANDLERS
    // ================================================================

    /**
     * Xử lý tải ảnh lên (placeholder).
     */
    private void handleUploadAvatar() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn ảnh đại diện");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Ảnh (*.jpg, *.png, *.gif)", "jpg", "jpeg", "png", "gif"
        ));
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            JOptionPane.showMessageDialog(this,
                    "Đã chọn: " + fileChooser.getSelectedFile().getName(),
                    "Tải ảnh lên", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Xử lý nút "Chỉnh sửa hồ sơ".
     */
    private void handleEditProfile() {
        JOptionPane.showMessageDialog(this,
                "Chức năng chỉnh sửa hồ sơ đang phát triển.",
                "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Xử lý nút "Đổi mật khẩu".
     */
    private void handleChangePassword() {
        JOptionPane.showMessageDialog(this,
                "Chức năng đổi mật khẩu đang phát triển.",
                "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    // ================================================================
    //  PUBLIC SETTERS (Để nạp dữ liệu từ bên ngoài)
    // ================================================================

    public void setFullName(String fullName) {
        this.fullName = fullName;
        if (tfFullName != null) tfFullName.setText(fullName);
        if (avatarLabel != null) {
            String initial = fullName != null && !fullName.isEmpty()
                    ? String.valueOf(fullName.charAt(0)).toUpperCase() : "U";
            avatarLabel.setText(initial);
            avatarLabel.repaint();
        }
    }

    public void setUsername(String username) {
        this.username = username;
        if (tfUsername != null) tfUsername.setText(username);
    }

    public void setEmail(String email) {
        this.email = email;
        if (tfEmail != null) tfEmail.setText(email);
    }

    public void setPhone(String phone) {
        this.phone = phone;
        if (tfPhone != null) tfPhone.setText(phone);
    }

    public void setRole(String role) {
        this.role = role;
        if (lblRole != null) lblRole.setText(role);
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
        if (lblBranch != null) lblBranch.setText(branchName);
    }

    public void setBranchHotline(String branchHotline) {
        this.branchHotline = branchHotline;
        if (lblHotline != null) lblHotline.setText(branchHotline);
    }
}
