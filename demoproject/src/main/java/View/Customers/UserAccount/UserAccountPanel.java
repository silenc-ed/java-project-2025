package View.Customers.UserAccount;

import Controller.ProfileDAO;
import Common.TokenManager;
import Common.EmailService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.Calendar;

public class UserAccountPanel extends javax.swing.JPanel {

    // Premium Color Palette
    private static final Color BG_PRIMARY = new Color(245, 246, 250);
    private static final Color BG_WHITE = Color.WHITE;
    private static final Color ACCENT_PRIMARY = new Color(79, 70, 229);    // Indigo
    private static final Color ACCENT_SECONDARY = new Color(99, 102, 241); // Light Indigo
    private static final Color TEXT_PRIMARY = new Color(30, 41, 59);
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);
    private static final Color BORDER_LIGHT = new Color(226, 232, 240);
    private static final Color HOVER_BG = new Color(241, 245, 249);
    
    // Standard Colors for buttons
    private static final Color COLOR_GREEN = new Color(40, 167, 69);
    private static final Color COLOR_GREEN_HOVER = new Color(33, 136, 56);
    private static final Color COLOR_BLUE = new Color(0, 123, 255);
    private static final Color COLOR_BLUE_HOVER = new Color(0, 105, 217);
    private static final Color COLOR_RED = new Color(220, 53, 69);
    private static final Color COLOR_RED_HOVER = new Color(200, 35, 51);

    private ProfileDAO.Profile profile;
    private CardLayout cardLayout;
    private JPanel rightContentPanel;
    
    // Form fields - Profile
    private JLabel lblUsernameValue;
    private JTextField tfHoTen;
    private JTextField tfSdt;
    private JTextField tfEmail;
    private JRadioButton rbNam, rbNu, rbKhac;
    private JComboBox<Integer> cbNgay, cbThang, cbNamSinh;

    // Form fields - Address
    private JTextArea taDiaChi;

    // Form fields - Password
    private JPasswordField pfMatKhauCu;
    private JPasswordField pfMatKhauMoi;
    private JPasswordField pfXacNhanMatKhau;

    // Reward display fields
    private JLabel lblPointsCardValue;
    private JLabel lblTierName;
    private JLabel lblUserPointsName;
    private JProgressBar pbTierProgress;
    private JLabel lblNextTierPromo;

    // Sidebar navigation buttons
    private JPanel pnlNavHoSo, pnlNavDiaChi, pnlNavMatKhau, pnlNavDiem;

    public UserAccountPanel() {
        initComponents();
        loadProfileData();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(BG_PRIMARY);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Create Split Layout (Left: Sidebar, Right: Main Area)
        JPanel mainSplitPanel = new JPanel(new BorderLayout(15, 0));
        mainSplitPanel.setOpaque(false);

        // ================== SIDEBAR PANEL (LEFT) ==================
        JPanel sidebarPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_WHITE);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 16, 16));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        sidebarPanel.setOpaque(false);
        sidebarPanel.setPreferredSize(new Dimension(240, 0));
        sidebarPanel.setBorder(new EmptyBorder(20, 15, 20, 15));
        sidebarPanel.setLayout(new BorderLayout(0, 20));

        // Sidebar Header (Avatar + Username)
        JPanel sidebarHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 5));
        sidebarHeader.setOpaque(false);

        // Circular Avatar
        JLabel avatarLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Draw circle background
                g2.setColor(new Color(238, 242, 255));
                g2.fillOval(0, 0, getWidth(), getHeight());
                // Draw initial letter
                g2.setColor(ACCENT_PRIMARY);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 22));
                FontMetrics fm = g2.getFontMetrics();
                String initial = "U";
                if (profile != null && profile.hoTen != null && !profile.hoTen.isEmpty()) {
                    initial = profile.hoTen.substring(0, 1).toUpperCase();
                }
                int x = (getWidth() - fm.stringWidth(initial)) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(initial, x, y);
                g2.dispose();
            }
        };
        avatarLabel.setPreferredSize(new Dimension(50, 50));
        sidebarHeader.add(avatarLabel);

        // User info text panel
        JPanel userInfoTextPanel = new JPanel();
        userInfoTextPanel.setLayout(new BoxLayout(userInfoTextPanel, BoxLayout.Y_AXIS));
        userInfoTextPanel.setOpaque(false);

        lblUsernameValue = new JLabel("Đang tải...");
        lblUsernameValue.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblUsernameValue.setForeground(TEXT_PRIMARY);

        JLabel lblEditProfile = new JLabel("✎ Sửa hồ sơ");
        lblEditProfile.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblEditProfile.setForeground(TEXT_SECONDARY);
        lblEditProfile.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblEditProfile.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                switchCard("HOSO", pnlNavHoSo);
            }
        });

        userInfoTextPanel.add(lblUsernameValue);
        userInfoTextPanel.add(Box.createVerticalStrut(3));
        userInfoTextPanel.add(lblEditProfile);
        sidebarHeader.add(userInfoTextPanel);

        sidebarPanel.add(sidebarHeader, BorderLayout.NORTH);

        // Sidebar Navigation Menu
        JPanel sidebarMenuPanel = new JPanel();
        sidebarMenuPanel.setLayout(new BoxLayout(sidebarMenuPanel, BoxLayout.Y_AXIS));
        sidebarMenuPanel.setOpaque(false);

        // Separator
        JSeparator sepSidebar = new JSeparator();
        sepSidebar.setForeground(BORDER_LIGHT);
        sepSidebar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sepSidebar.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebarMenuPanel.add(sepSidebar);
        sidebarMenuPanel.add(Box.createVerticalStrut(15));

        // Section Title: Tài khoản của tôi
        JLabel lblSectionAccount = new JLabel("Tài khoản của tôi");
        lblSectionAccount.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSectionAccount.setForeground(TEXT_PRIMARY);
        lblSectionAccount.setBorder(new EmptyBorder(5, 10, 5, 10));
        lblSectionAccount.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebarMenuPanel.add(lblSectionAccount);

        pnlNavHoSo = createSidebarItem("  • Hồ sơ cá nhân");
        pnlNavDiaChi = createSidebarItem("  • Địa chỉ nhận hàng");
        pnlNavMatKhau = createSidebarItem("  • Đổi mật khẩu");
        
        pnlNavHoSo.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlNavDiaChi.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlNavMatKhau.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        sidebarMenuPanel.add(pnlNavHoSo);
        sidebarMenuPanel.add(pnlNavDiaChi);
        sidebarMenuPanel.add(pnlNavMatKhau);
        sidebarMenuPanel.add(Box.createVerticalStrut(10));

        // Section Title: Ưu đãi & Tích lũy
        JLabel lblSectionLoyalty = new JLabel("Ưu đãi thành viên");
        lblSectionLoyalty.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSectionLoyalty.setForeground(TEXT_PRIMARY);
        lblSectionLoyalty.setBorder(new EmptyBorder(5, 10, 5, 10));
        lblSectionLoyalty.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebarMenuPanel.add(lblSectionLoyalty);

        pnlNavDiem = createSidebarItem("  🌟 Điểm tích lũy");
        pnlNavDiem.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebarMenuPanel.add(pnlNavDiem);

        // Events for sidebar items
        pnlNavHoSo.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { switchCard("HOSO", pnlNavHoSo); }
        });
        pnlNavDiaChi.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { switchCard("DIACHI", pnlNavDiaChi); }
        });
        pnlNavMatKhau.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { switchCard("MATKHAU", pnlNavMatKhau); }
        });
        pnlNavDiem.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { switchCard("DIEM", pnlNavDiem); }
        });

        sidebarPanel.add(sidebarMenuPanel, BorderLayout.CENTER);
        mainSplitPanel.add(sidebarPanel, BorderLayout.WEST);

        // ================== RIGHT CONTENT PANEL (CARD LAYOUT) ==================
        cardLayout = new CardLayout();
        rightContentPanel = new JPanel(cardLayout) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_WHITE);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 16, 16));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        rightContentPanel.setOpaque(false);
        rightContentPanel.setBorder(new EmptyBorder(25, 30, 25, 30));

        // Create the cards
        JPanel cardHoSo = createProfileCard();
        JPanel cardDiaChi = createAddressCard();
        JPanel cardMatKhau = createPasswordCard();
        JPanel cardDiem = createPointsCard();

        rightContentPanel.add(cardHoSo, "HOSO");
        rightContentPanel.add(cardDiaChi, "DIACHI");
        rightContentPanel.add(cardMatKhau, "MATKHAU");
        rightContentPanel.add(cardDiem, "DIEM");

        mainSplitPanel.add(rightContentPanel, BorderLayout.CENTER);
        add(mainSplitPanel, BorderLayout.CENTER);

        // Default active tab
        setActiveSidebarItem(pnlNavHoSo);
    }

    private JPanel createSidebarItem(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(210, 35));
        panel.setMinimumSize(new Dimension(210, 35));
        panel.setMaximumSize(new Dimension(210, 35));
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panel.setBorder(new EmptyBorder(6, 10, 6, 10));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel(title);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(TEXT_SECONDARY);
        panel.add(label, BorderLayout.CENTER);

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (panel.getBackground() != HOVER_BG) {
                    label.setForeground(ACCENT_PRIMARY);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (panel.isOpaque()) {
                    label.setForeground(ACCENT_PRIMARY);
                } else {
                    label.setForeground(TEXT_SECONDARY);
                }
            }
        });
        return panel;
    }

    private void setActiveSidebarItem(JPanel activePanel) {
        // Reset all
        JPanel[] items = {pnlNavHoSo, pnlNavDiaChi, pnlNavMatKhau, pnlNavDiem};
        for (JPanel item : items) {
            item.setOpaque(false);
            item.setBackground(null);
            JLabel lbl = (JLabel) item.getComponent(0);
            lbl.setForeground(TEXT_SECONDARY);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        }

        // Set active
        activePanel.setOpaque(true);
        activePanel.setBackground(HOVER_BG);
        JLabel lblActive = (JLabel) activePanel.getComponent(0);
        lblActive.setForeground(ACCENT_PRIMARY);
        lblActive.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        repaint();
    }

    private void switchCard(String cardName, JPanel activeNavItem) {
        cardLayout.show(rightContentPanel, cardName);
        setActiveSidebarItem(activeNavItem);
    }

    // ================== CARD 1: PROFILE FORM ==================
    private JPanel createProfileCard() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setOpaque(false);

        // Header Section
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_LIGHT));

        JLabel titleLabel = new JLabel("Hồ sơ của tôi");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(TEXT_PRIMARY);

        JLabel subLabel = new JLabel("Quản lý thông tin hồ sơ để bảo mật tài khoản");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLabel.setForeground(TEXT_SECONDARY);
        subLabel.setBorder(new EmptyBorder(5, 0, 10, 0));

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subLabel, BorderLayout.SOUTH);
        panel.add(headerPanel, BorderLayout.NORTH);

        // Form fields Section
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        // 1. Username
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.25;
        JLabel lblUser = new JLabel("Tên đăng nhập:");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblUser.setForeground(TEXT_SECONDARY);
        formPanel.add(lblUser, gbc);

        gbc.gridx = 1; gbc.weightx = 0.75;
        lblUsernameValue = new JLabel("Loading...");
        lblUsernameValue.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblUsernameValue.setForeground(TEXT_PRIMARY);
        formPanel.add(lblUsernameValue, gbc);

        // 2. Họ tên
        gbc.gridx = 0; gbc.gridy++; gbc.weightx = 0.25;
        JLabel lblName = new JLabel("Họ và tên:");
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblName.setForeground(TEXT_SECONDARY);
        formPanel.add(lblName, gbc);

        gbc.gridx = 1; gbc.weightx = 0.75;
        tfHoTen = createStyledTextField();
        formPanel.add(tfHoTen, gbc);

        // 3. Email
        gbc.gridx = 0; gbc.gridy++; gbc.weightx = 0.25;
        JLabel lblMail = new JLabel("Địa chỉ Email:");
        lblMail.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblMail.setForeground(TEXT_SECONDARY);
        formPanel.add(lblMail, gbc);

        gbc.gridx = 1; gbc.weightx = 0.75;
        tfEmail = createStyledTextField();
        formPanel.add(tfEmail, gbc);

        // 4. Số điện thoại
        gbc.gridx = 0; gbc.gridy++; gbc.weightx = 0.25;
        JLabel lblPhone = new JLabel("Số điện thoại:");
        lblPhone.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPhone.setForeground(TEXT_SECONDARY);
        formPanel.add(lblPhone, gbc);

        gbc.gridx = 1; gbc.weightx = 0.75;
        tfSdt = createStyledTextField();
        formPanel.add(tfSdt, gbc);

        // 5. Giới tính
        gbc.gridx = 0; gbc.gridy++; gbc.weightx = 0.25;
        JLabel lblGender = new JLabel("Giới tính:");
        lblGender.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblGender.setForeground(TEXT_SECONDARY);
        formPanel.add(lblGender, gbc);

        gbc.gridx = 1; gbc.weightx = 0.75;
        JPanel pnlGender = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        pnlGender.setOpaque(false);
        rbNam = new JRadioButton("Nam"); rbNu = new JRadioButton("Nữ"); rbKhac = new JRadioButton("Khác");
        rbNam.setOpaque(false); rbNu.setOpaque(false); rbKhac.setOpaque(false);
        ButtonGroup bgGender = new ButtonGroup();
        bgGender.add(rbNam); bgGender.add(rbNu); bgGender.add(rbKhac);
        pnlGender.add(rbNam); pnlGender.add(rbNu); pnlGender.add(rbKhac);
        formPanel.add(pnlGender, gbc);

        // 6. Ngày sinh
        gbc.gridx = 0; gbc.gridy++; gbc.weightx = 0.25;
        JLabel lblDob = new JLabel("Ngày sinh:");
        lblDob.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblDob.setForeground(TEXT_SECONDARY);
        formPanel.add(lblDob, gbc);

        gbc.gridx = 1; gbc.weightx = 0.75;
        JPanel pnlDob = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlDob.setOpaque(false);

        cbNgay = new JComboBox<>(); for (int i = 1; i <= 31; i++) cbNgay.addItem(i);
        cbThang = new JComboBox<>(); for (int i = 1; i <= 12; i++) cbThang.addItem(i);
        cbNamSinh = new JComboBox<>(); for (int i = 2026; i >= 1940; i--) cbNamSinh.addItem(i);

        cbNgay.setPreferredSize(new Dimension(65, 30));
        cbThang.setPreferredSize(new Dimension(80, 30));
        cbNamSinh.setPreferredSize(new Dimension(90, 30));

        pnlDob.add(new JLabel("Ngày")); pnlDob.add(cbNgay);
        pnlDob.add(new JLabel(" Tháng")); pnlDob.add(cbThang);
        pnlDob.add(new JLabel(" Năm")); pnlDob.add(cbNamSinh);
        formPanel.add(pnlDob, gbc);

        // Action Buttons Row
        gbc.gridx = 1; gbc.gridy++;
        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        pnlActions.setOpaque(false);

        JButton btnSave = createStyledButton("Lưu thay đổi", COLOR_GREEN, COLOR_GREEN_HOVER);
        btnSave.addActionListener(e -> handleSaveChanges());
        
        JButton btnReset = createStyledButton("Nhập lại", COLOR_RED, COLOR_RED_HOVER);
        btnReset.addActionListener(e -> loadProfileData());

        pnlActions.add(btnSave);
        pnlActions.add(btnReset);
        formPanel.add(pnlActions, gbc);

        panel.add(formPanel, BorderLayout.CENTER);
        return panel;
    }

    // ================== CARD 2: ADDRESS MANAGEMENT ==================
    private JPanel createAddressCard() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setOpaque(false);

        // Header Section
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_LIGHT));

        JLabel titleLabel = new JLabel("Địa chỉ của tôi");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(TEXT_PRIMARY);

        JLabel subLabel = new JLabel("Quản lý thông tin địa chỉ giao hàng và nhận hàng");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLabel.setForeground(TEXT_SECONDARY);
        subLabel.setBorder(new EmptyBorder(5, 0, 10, 0));

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subLabel, BorderLayout.SOUTH);
        panel.add(headerPanel, BorderLayout.NORTH);

        // Content
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.25;
        JLabel lblAddr = new JLabel("Địa chỉ nhận hàng:");
        lblAddr.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblAddr.setForeground(TEXT_SECONDARY);
        contentPanel.add(lblAddr, gbc);

        gbc.gridx = 1; gbc.weightx = 0.75;
        taDiaChi = new JTextArea(4, 30);
        taDiaChi.setLineWrap(true);
        taDiaChi.setWrapStyleWord(true);
        taDiaChi.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        taDiaChi.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_LIGHT, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        
        JScrollPane spDiaChi = new JScrollPane(taDiaChi);
        spDiaChi.setBorder(null);
        contentPanel.add(spDiaChi, gbc);

        gbc.gridx = 1; gbc.gridy++;
        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        pnlActions.setOpaque(false);

        JButton btnSaveAddr = createStyledButton("Lưu địa chỉ", COLOR_GREEN, COLOR_GREEN_HOVER);
        btnSaveAddr.addActionListener(e -> handleSaveAddress());
        
        JButton btnReset = createStyledButton("Làm mới", COLOR_RED, COLOR_RED_HOVER);
        btnReset.addActionListener(e -> loadProfileData());

        pnlActions.add(btnSaveAddr);
        pnlActions.add(btnReset);
        contentPanel.add(pnlActions, gbc);

        // Graphic Placeholder
        gbc.gridx = 1; gbc.gridy++; gbc.insets = new Insets(30, 10, 10, 10);
        JLabel lblTruck = new JLabel("🚚 Hàng hóa sẽ được vận chuyển trực tiếp đến địa chỉ mặc định này cúa bạn!");
        lblTruck.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        lblTruck.setForeground(TEXT_SECONDARY);
        contentPanel.add(lblTruck, gbc);

        panel.add(contentPanel, BorderLayout.CENTER);
        return panel;
    }

    // ================== CARD 3: SECURE PASSWORD ==================
    private JPanel createPasswordCard() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setOpaque(false);

        // Header Section
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_LIGHT));

        JLabel titleLabel = new JLabel("Đổi mật khẩu");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(TEXT_PRIMARY);

        JLabel subLabel = new JLabel("Để bảo mật tài khoản, vui lòng không chia sẻ mật khẩu cho người khác");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLabel.setForeground(TEXT_SECONDARY);
        subLabel.setBorder(new EmptyBorder(5, 0, 10, 0));

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subLabel, BorderLayout.SOUTH);
        panel.add(headerPanel, BorderLayout.NORTH);

        // Content
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        // 1. Current Pass
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.25;
        JLabel lblOldPass = new JLabel("Mật khẩu cũ:");
        lblOldPass.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblOldPass.setForeground(TEXT_SECONDARY);
        contentPanel.add(lblOldPass, gbc);

        gbc.gridx = 1; gbc.weightx = 0.75;
        pfMatKhauCu = createStyledPasswordField();
        contentPanel.add(pfMatKhauCu, gbc);

        // 2. New Pass
        gbc.gridx = 0; gbc.gridy++; gbc.weightx = 0.25;
        JLabel lblNewPass = new JLabel("Mật khẩu mới:");
        lblNewPass.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblNewPass.setForeground(TEXT_SECONDARY);
        contentPanel.add(lblNewPass, gbc);

        gbc.gridx = 1; gbc.weightx = 0.75;
        pfMatKhauMoi = createStyledPasswordField();
        contentPanel.add(pfMatKhauMoi, gbc);

        // 3. Confirm Pass
        gbc.gridx = 0; gbc.gridy++; gbc.weightx = 0.25;
        JLabel lblConfPass = new JLabel("Xác nhận mật khẩu:");
        lblConfPass.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblConfPass.setForeground(TEXT_SECONDARY);
        contentPanel.add(lblConfPass, gbc);

        gbc.gridx = 1; gbc.weightx = 0.75;
        pfXacNhanMatKhau = createStyledPasswordField();
        contentPanel.add(pfXacNhanMatKhau, gbc);

        // Buttons
        gbc.gridx = 1; gbc.gridy++;
        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        pnlActions.setOpaque(false);

        JButton btnChange = createStyledButton("Đổi mật khẩu", COLOR_BLUE, COLOR_BLUE_HOVER);
        btnChange.addActionListener(e -> handleChangePassword());
        
        JButton btnReset = createStyledButton("Làm sạch", COLOR_RED, COLOR_RED_HOVER);
        btnReset.addActionListener(e -> {
            pfMatKhauCu.setText("");
            pfMatKhauMoi.setText("");
            pfXacNhanMatKhau.setText("");
        });

        pnlActions.add(btnChange);
        pnlActions.add(btnReset);
        contentPanel.add(pnlActions, gbc);

        panel.add(contentPanel, BorderLayout.CENTER);
        return panel;
    }

    // ================== CARD 4: LOYALTY CARD ==================
    private JPanel createPointsCard() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setOpaque(false);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_LIGHT));

        JLabel titleLabel = new JLabel("Điểm tích lũy & Hạng thành viên");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(TEXT_PRIMARY);

        JLabel subLabel = new JLabel("Tích lũy điểm khi mua hàng tại hệ thống để đổi ưu đãi cực lớn!");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLabel.setForeground(TEXT_SECONDARY);
        subLabel.setBorder(new EmptyBorder(5, 0, 10, 0));

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subLabel, BorderLayout.SOUTH);
        panel.add(headerPanel, BorderLayout.NORTH);

        // Membership Gold Card
        JPanel cardPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Gold gradient paint
                Color c1 = new Color(241, 196, 15);
                Color c2 = new Color(243, 156, 18);
                GradientPaint gp = new GradientPaint(0, 0, c1, getWidth(), getHeight(), c2);
                g2d.setPaint(gp);
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));
                
                // Add soft patterns
                g2d.setColor(new Color(255, 255, 255, 30));
                g2d.fillOval(getWidth() - 100, -50, 150, 150);
                g2d.fillOval(getWidth() - 150, getHeight() - 100, 200, 200);
                
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        cardPanel.setOpaque(false);
        cardPanel.setPreferredSize(new Dimension(0, 160));
        cardPanel.setLayout(new BorderLayout());
        cardPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

        JPanel pnlCardTop = new JPanel(new BorderLayout());
        pnlCardTop.setOpaque(false);
        
        lblTierName = new JLabel("MEMBER");
        lblTierName.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTierName.setForeground(Color.WHITE);
        pnlCardTop.add(lblTierName, BorderLayout.WEST);

        JLabel lblLogo = new JLabel("👑");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblLogo.setForeground(Color.WHITE);
        pnlCardTop.add(lblLogo, BorderLayout.EAST);

        cardPanel.add(pnlCardTop, BorderLayout.NORTH);

        JPanel pnlCardCenter = new JPanel(new BorderLayout());
        pnlCardCenter.setOpaque(false);
        
        lblUserPointsName = new JLabel("Khách hàng: Loading...");
        lblUserPointsName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblUserPointsName.setForeground(new Color(255, 255, 255, 220));
        pnlCardCenter.add(lblUserPointsName, BorderLayout.WEST);

        lblPointsCardValue = new JLabel("0 Điểm");
        lblPointsCardValue.setFont(new Font("Segoe UI", Font.BOLD, 30));
        lblPointsCardValue.setForeground(Color.WHITE);
        pnlCardCenter.add(lblPointsCardValue, BorderLayout.EAST);

        cardPanel.add(pnlCardCenter, BorderLayout.SOUTH);

        // Progress panel
        JPanel pnlPointsDetail = new JPanel();
        pnlPointsDetail.setLayout(new BoxLayout(pnlPointsDetail, BoxLayout.Y_AXIS));
        pnlPointsDetail.setOpaque(false);

        JLabel lblProgTitle = new JLabel("Tiến trình thăng hạng của bạn:");
        lblProgTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblProgTitle.setForeground(TEXT_PRIMARY);
        pnlPointsDetail.add(lblProgTitle);
        pnlPointsDetail.add(Box.createVerticalStrut(8));

        pbTierProgress = new JProgressBar(0, 10000);
        pbTierProgress.setMaximumSize(new Dimension(Integer.MAX_VALUE, 12));
        pbTierProgress.setPreferredSize(new Dimension(0, 12));
        pbTierProgress.setForeground(new Color(243, 156, 18));
        pbTierProgress.setBackground(BORDER_LIGHT);
        pbTierProgress.setBorderPainted(false);
        pnlPointsDetail.add(pbTierProgress);
        pnlPointsDetail.add(Box.createVerticalStrut(8));

        lblNextTierPromo = new JLabel("Tích lũy thêm điểm để nhận hạng tiếp theo.");
        lblNextTierPromo.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblNextTierPromo.setForeground(TEXT_SECONDARY);
        pnlPointsDetail.add(lblNextTierPromo);

        // Main body layout of reward points card
        JPanel pnlRewardBody = new JPanel(new BorderLayout(0, 15));
        pnlRewardBody.setOpaque(false);
        pnlRewardBody.add(cardPanel, BorderLayout.NORTH);
        pnlRewardBody.add(pnlPointsDetail, BorderLayout.CENTER);

        panel.add(pnlRewardBody, BorderLayout.CENTER);
        return panel;
    }

    private JTextField createStyledTextField() {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(ACCENT_PRIMARY);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_LIGHT, 1),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        
        tf.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ACCENT_SECONDARY, 1),
                    BorderFactory.createEmptyBorder(6, 12, 6, 12)
                ));
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                    BorderFactory.createEmptyBorder(6, 12, 6, 12)
                ));
            }
        });
        return tf;
    }

    private JPasswordField createStyledPasswordField() {
        JPasswordField pf = new JPasswordField();
        pf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        pf.setForeground(TEXT_PRIMARY);
        pf.setCaretColor(ACCENT_PRIMARY);
        pf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_LIGHT, 1),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        
        pf.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                pf.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ACCENT_SECONDARY, 1),
                    BorderFactory.createEmptyBorder(6, 12, 6, 12)
                ));
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                pf.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_LIGHT, 1),
                    BorderFactory.createEmptyBorder(6, 12, 6, 12)
                ));
            }
        });
        return pf;
    }

    private JButton createStyledButton(String text, Color baseColor, Color hoverColor) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(hoverColor.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(hoverColor);
                } else {
                    g2.setColor(baseColor);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        return btn;
    }

    private void loadProfileData() {
        String token = TokenManager.getLocalToken();
        if (token == null) {
            JOptionPane.showMessageDialog(this, "Không có token đăng nhập hợp lệ. Vui lòng đăng nhập lại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        profile = ProfileDAO.getProfileByToken(token);
        if (profile != null) {
            // Update Username
            String displayUser = (profile.username != null && !profile.username.isEmpty()) ? profile.username : (profile.role.equals("ADMIN") ? "ADMIN-" + profile.id : "CUSTOMER-" + profile.id);
            lblUsernameValue.setText(displayUser);

            // Update Fields
            tfHoTen.setText(profile.hoTen);
            tfSdt.setText(profile.sdt);
            tfEmail.setText(profile.email);
            taDiaChi.setText(profile.diaChi != null ? profile.diaChi : "");

            // Setup radio selections (simulate defaults if null)
            rbNam.setSelected(true); // default Nam
            cbNgay.setSelectedIndex(0);
            cbThang.setSelectedIndex(0);
            cbNamSinh.setSelectedItem(2000);

            // Set Loyalty status
            lblPointsCardValue.setText(String.format("%,d Điểm", profile.diemTichLuy));
            lblUserPointsName.setText("Khách hàng: " + profile.hoTen);

            // Calculate Tier and progress
            String tier = "ĐỒNG";
            Color cardColor = new Color(243, 156, 18);
            int nextTierPoints = 1000;
            String nextTierName = "BẠC";
            
            if (profile.diemTichLuy >= 10000) {
                tier = "BẠCH KIM";
                nextTierPoints = 10000;
                nextTierName = "";
            } else if (profile.diemTichLuy >= 5000) {
                tier = "VÀNG";
                nextTierPoints = 10000;
                nextTierName = "BẠCH KIM";
            } else if (profile.diemTichLuy >= 1000) {
                tier = "BẠC";
                nextTierPoints = 5000;
                nextTierName = "VÀNG";
            }

            lblTierName.setText(tier + " MEMBER");
            pbTierProgress.setMaximum(nextTierPoints);
            pbTierProgress.setValue((int)profile.diemTichLuy);
            
            if (!nextTierName.isEmpty()) {
                long needed = nextTierPoints - profile.diemTichLuy;
                lblNextTierPromo.setText(String.format("🌟 Bạn cần thêm %,d điểm nữa để thăng hạng %s!", needed, nextTierName));
            } else {
                lblNextTierPromo.setText("🎉 Chúc mừng! Bạn đang sở hữu thứ hạng cao nhất (BẠCH KIM)!");
            }

            repaint();
        } else {
            JOptionPane.showMessageDialog(this, "Không thể tải thông tin tài khoản từ máy chủ.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleSaveChanges() {
        final String newName = tfHoTen.getText().trim();
        final String newPhone = tfSdt.getText().trim();
        final String newEmail = tfEmail.getText().trim();

        if (newName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Họ và tên không được để trống.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (newPhone.isEmpty() || !newPhone.matches("^\\d{10}$")) {
            JOptionPane.showMessageDialog(this, "Số điện thoại không hợp lệ (yêu cầu 10 chữ số).", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (newEmail.isEmpty() || !newEmail.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            JOptionPane.showMessageDialog(this, "Địa chỉ email không hợp lệ.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (newName.equals(profile.hoTen) && newPhone.equals(profile.sdt) && newEmail.equals(profile.email)) {
            JOptionPane.showMessageDialog(this, "Không có thay đổi nào để lưu.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        final String token = TokenManager.getLocalToken();

        if (!newEmail.equalsIgnoreCase(profile.email)) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Bạn đang thay đổi địa chỉ email. Hệ thống sẽ gửi một mã xác thực (OTP) đến email mới.\nBạn có muốn tiếp tục không?",
                    "Xác nhận thay đổi email", JOptionPane.YES_NO_OPTION);

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            final String otpCode = EmailService.generateOTP();
            
            final JDialog progressDlg = new JDialog((java.awt.Frame) SwingUtilities.getWindowAncestor(this), "Gửi OTP", true);
            progressDlg.setLayout(new BorderLayout());
            JLabel lblStatus = new JLabel("Đang gửi mã xác thực tới email " + newEmail + "...", JLabel.CENTER);
            lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            lblStatus.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            progressDlg.add(lblStatus, BorderLayout.CENTER);
            progressDlg.setSize(400, 120);
            progressDlg.setLocationRelativeTo(this);

            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return EmailService.sendOTP(newEmail, otpCode);
                }

                @Override
                protected void done() {
                    progressDlg.dispose();
                    try {
                        boolean success = get();
                        if (success) {
                            String otpInput = JOptionPane.showInputDialog(UserAccountPanel.this,
                                    "Mã xác thực OTP gồm 6 chữ số đã được gửi thành công.\nVui lòng nhập mã OTP để xác nhận thay đổi email:",
                                    "Xác minh OTP", JOptionPane.PLAIN_MESSAGE);

                            if (otpInput != null) {
                                if (otpInput.trim().equals(otpCode)) {
                                    saveProfile(token, newName, newPhone, newEmail, profile.diaChi);
                                } else {
                                    JOptionPane.showMessageDialog(UserAccountPanel.this,
                                            "Mã OTP không chính xác. Hủy thay đổi thông tin.",
                                            "Xác minh thất bại", JOptionPane.ERROR_MESSAGE);
                                }
                            }
                        } else {
                            JOptionPane.showMessageDialog(UserAccountPanel.this,
                                    "Gửi mã OTP thất bại. Vui lòng kiểm tra lại địa chỉ email hoặc kết nối mạng.",
                                    "Lỗi gửi email", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(UserAccountPanel.this,
                                "Đã xảy ra lỗi khi gửi OTP: " + ex.getMessage(),
                                "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };

            worker.execute();
            progressDlg.setVisible(true);

        } else {
            saveProfile(token, newName, newPhone, newEmail, profile.diaChi);
        }
    }

    private void handleSaveAddress() {
        String newAddr = taDiaChi.getText().trim();
        String token = TokenManager.getLocalToken();
        if (profile == null || token == null) return;

        saveProfile(token, profile.hoTen, profile.sdt, profile.email, newAddr);
    }

    private void saveProfile(String token, String hoTen, String sdt, String email, String diaChi) {
        boolean ok = ProfileDAO.updateProfileByToken(token, hoTen, sdt, email, diaChi);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Cập nhật thông tin hồ sơ thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            
            // Refresh parent Main Frame header if applicable
            java.awt.Window window = SwingUtilities.getWindowAncestor(this);
            if (window instanceof View.Admin.Main) {
                ((View.Admin.Main) window).setFullName(hoTen);
            } else if (window instanceof View.Customers.Main) {
                ((View.Customers.Main) window).setFullName(hoTen);
            } else if (window instanceof View.Employees.Main) {
                ((View.Employees.Main) window).setFullName(hoTen);
            }
            
            loadProfileData();
        } else {
            JOptionPane.showMessageDialog(this, "Không thể cập nhật hồ sơ trong cơ sở dữ liệu.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleChangePassword() {
        String currentPass = new String(pfMatKhauCu.getPassword()).trim();
        String newPass = new String(pfMatKhauMoi.getPassword()).trim();
        String confPass = new String(pfXacNhanMatKhau.getPassword()).trim();

        if (currentPass.isEmpty() || newPass.isEmpty() || confPass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng điền đầy đủ các thông tin mật khẩu.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!newPass.equals(confPass)) {
            JOptionPane.showMessageDialog(this, "Mật khẩu xác nhận không trùng khớp.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (newPass.length() < 6) {
            JOptionPane.showMessageDialog(this, "Mật khẩu mới phải từ 6 ký tự trở lên.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String token = TokenManager.getLocalToken();
        if (token == null) return;

        boolean success = ProfileDAO.changePasswordByToken(token, currentPass, newPass);
        if (success) {
            JOptionPane.showMessageDialog(this, "Đổi mật khẩu thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            pfMatKhauCu.setText("");
            pfMatKhauMoi.setText("");
            pfXacNhanMatKhau.setText("");
        } else {
            JOptionPane.showMessageDialog(this, "Mật khẩu cũ không chính xác hoặc không thể thay đổi.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
