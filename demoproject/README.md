# 📖 DemoProject — Tài Liệu Kiến Trúc Dự Án

> **Loại ứng dụng:** Java Desktop App (Swing + FlatLaf)  
> **CSDL:** Oracle Database (JDBC)  
> **Build Tool:** Apache Maven  
> **Java Version:** 21  
> **Main Class:** `com.mycompany.demoproject.Demoproject`

---

## 📁 1. Cây Thư Mục (Directory Tree)

```
demoproject/
├── pom.xml                          # Cấu hình Maven (dependencies, plugins)
├── README.md                        # ← Bạn đang đọc file này
│
└── src/main/
    ├── java/
    │   ├── com/mycompany/demoproject/
    │   │   └── Demoproject.java     # ★ ENTRY POINT — Hàm main(), khởi tạo FlatLaf & điều hướng
    │   │
    │   ├── ConnectDB/               # 🔌 Kết nối CSDL Oracle
    │   │   ├── ConnectionOracle.java    # Cấu hình host, SID, user, password Oracle
    │   │   └── ConnectionUtils.java     # Factory method getMyConnection() dùng chung
    │   │
    │   ├── Model/                   # 📦 Các POJO (Plain Old Java Object) — Lớp dữ liệu
    │   │   ├── SanPham.java             # Sản phẩm (mã SP, tên, giá, hình ảnh, trạng thái…)
    │   │   ├── LoaiSanPham.java         # Loại sản phẩm (danh mục)
    │   │   ├── BienTheSanPham.java      # Biến thể sản phẩm (màu sắc, dung lượng…)
    │   │   ├── CartItem.java            # Mặt hàng trong giỏ hàng
    │   │   └── CartManager.java         # Singleton quản lý giỏ hàng (thêm, xoá, làm rỗng)
    │   │
    │   ├── Controller/              # 🧠 Xử lý nghiệp vụ (Business Logic) & DAO
    │   │   ├── SanPhamDAO.java          # CRUD + tìm kiếm sản phẩm
    │   │   ├── LoaiSanPhamDAO.java      # Lấy danh sách loại sản phẩm
    │   │   │
    │   │   ├── SignIn/                  # Xử lý Đăng nhập
    │   │   │   ├── LoginProcess.java        # Xác thực username/password → tạo token
    │   │   │   └── AuthProcess.java         # Quản lý token (tạo, xác thực, thu hồi, lấy tên)
    │   │   │
    │   │   ├── CreateAccount/           # Xử lý Đăng ký
    │   │   │   ├── CreateAccountProcess.java  # Tạo KHACHHANG + TAIKHOAN (transaction)
    │   │   │   └── CreateUserProcess.java     # Kiểm tra trùng SDT/Email
    │   │   │
    │   │   ├── ForgottedPassword/       # Xử lý Quên mật khẩu
    │   │   │   └── ForgottedPasswordProcess.java  # Tìm tài khoản & đặt lại mật khẩu
    │   │   │
    │   │   └── Customers/
    │   │       └── Warranty/
    │   │           └── WarrantyCheckingProcess.java  # Tra cứu bảo hành qua IMEI
    │   │
    │   ├── View/                    # 🖥️ Giao diện Swing (JFrame, JPanel, .form)
    │   │   ├── SignIn/                  # Màn hình Đăng nhập
    │   │   │   └── SignInView.java/.form
    │   │   │
    │   │   ├── CreateAccount/           # Màn hình Đăng ký tài khoản
    │   │   │   ├── CreateAccountView.java/.form
    │   │   │   ├── CreateUserView.java/.form
    │   │   │   └── GetRequestAccount.java/.form
    │   │   │
    │   │   ├── ForgottedPassword/       # Màn hình Quên mật khẩu
    │   │   │   ├── ForgottedPasswordView.java/.form
    │   │   │   ├── GetRequestAccount.java/.form
    │   │   │   └── ResetPasswordView.java/.form
    │   │   │
    │   │   ├── Admin/                   # ★ Dashboard quản trị (Admin)
    │   │   │   ├── Main.java/.form          # JFrame chính của Admin
    │   │   │   ├── Menu.java/.form          # Menu sidebar
    │   │   │   ├── ListMenu.java            # Danh sách menu item
    │   │   │   ├── MainMenuPanel.java/.form # Panel menu chính
    │   │   │   ├── DashboardPanel.java      # Tổng quan
    │   │   │   ├── ProductPanel.java        # Quản lý sản phẩm
    │   │   │   ├── CustomerPanel.java       # Quản lý khách hàng
    │   │   │   ├── EmployeePanel.java       # Quản lý nhân viên
    │   │   │   ├── BillPanel.java           # Quản lý hoá đơn
    │   │   │   ├── CreateInvoicePanel.java  # Tạo hoá đơn mới
    │   │   │   ├── WarehousePanel.java      # Quản lý kho
    │   │   │   ├── ProcurementPanel.java    # Quản lý nhập hàng
    │   │   │   ├── VoucherPanel.java        # Quản lý voucher/khuyến mãi
    │   │   │   └── WarrantyPanel.java       # Quản lý bảo hành
    │   │   │
    │   │   ├── Customers/               # ★ Giao diện Khách hàng
    │   │   │   ├── Main.java/.form          # JFrame chính của Customer
    │   │   │   ├── Menu.java/.form          # Menu sidebar
    │   │   │   ├── ListMenu.java            # Danh sách menu item
    │   │   │   ├── UserAccountPanel.java    # Thông tin tài khoản
    │   │   │   ├── UserOptionPanel.java     # Tuỳ chọn người dùng
    │   │   │   ├── VoucherPanel.java        # Xem voucher
    │   │   │   ├── WarrantyPanel.java       # Tra cứu bảo hành
    │   │   │   ├── CartDrawer.java          # Drawer giỏ hàng
    │   │   │   ├── ProductPanel/            # Module hiển thị sản phẩm
    │   │   │   │   ├── ProductPanel.java        # Panel chính hiển thị grid sản phẩm
    │   │   │   │   ├── ProductCard.java         # Card hiển thị 1 sản phẩm
    │   │   │   │   ├── InfoProductPanel.java    # Chi tiết sản phẩm
    │   │   │   │   ├── BannerSlideshow.java     # Banner quảng cáo (slide)
    │   │   │   │   ├── CategoryPopupDialog.java # Popup lọc theo danh mục
    │   │   │   │   ├── WrapLayout.java          # Custom LayoutManager
    │   │   │   │   └── ProductFilterListener.java # Interface lọc sản phẩm
    │   │   │   └── StockPanel/
    │   │   │       └── PacketPanel.java     # Panel kho hàng
    │   │   │
    │   │   └── Employees/               # Giao diện Nhân viên
    │   │       ├── Main.java/.form
    │   │       ├── Menu.java/.form
    │   │       └── PanelBorder.java/.form
    │   │
    │   ├── Common/                  # 🔧 Tiện ích dùng chung
    │   │   ├── EmailService.java        # Gửi email OTP qua Gmail SMTP
    │   │   ├── HashUtil.java            # Mã hoá/kiểm tra mật khẩu (BCrypt)
    │   │   └── TokenManager.java        # Lưu/đọc/xoá token xác thực cục bộ (Preferences)
    │   │
    │   └── TestDB.java              # File test kết nối DB (dev only)
    │
    └── resources/
        ├── Default/                 # Tài nguyên giao diện chính
        │   ├── Icon/                    # Icon menu, nút bấm (1.png – 10.png, logo.png…)
        │   ├── Product/                 # Hình ảnh sản phẩm mẫu (laptop, phone, tablet…)
        │   └── Theme/wallpaper/         # Hình nền giao diện
        │
        └── Login/                   # Tài nguyên màn hình đăng nhập
            ├── Icon/                    # Icon Google login, v.v.
            └── Theme/                   # Hình nền & theme đăng nhập (loginImage.gif…)
```

---

## 🏗️ 2. Giải Thích Vai Trò Từng Thư Mục

| Thư mục | Vai trò |
|---------|---------|
| **`ConnectDB/`** | Chứa cấu hình kết nối tới Oracle Database. `ConnectionOracle` khai báo host, SID, user, password. `ConnectionUtils` cung cấp method `getMyConnection()` để các class khác gọi lấy `Connection`. |
| **`Model/`** | Chứa các POJO đại diện cho bảng trong CSDL (SanPham, LoaiSanPham, BienTheSanPham) và các đối tượng nghiệp vụ (CartItem, CartManager). Không chứa logic xử lý DB. |
| **`Controller/`** | Chứa toàn bộ logic nghiệp vụ và DAO (Data Access Object). Mỗi sub-package tương ứng với một chức năng: SignIn, CreateAccount, ForgottedPassword, Customers/Warranty. Các DAO như `SanPhamDAO`, `LoaiSanPhamDAO` nằm trực tiếp trong package gốc. |
| **`View/`** | Chứa toàn bộ giao diện Swing (JFrame, JPanel). Phân chia theo vai trò người dùng: `Admin/` (quản trị), `Customers/` (khách hàng), `Employees/` (nhân viên), `SignIn/`, `CreateAccount/`, `ForgottedPassword/`. |
| **`Common/`** | Chứa các class tiện ích dùng chung: gửi email OTP (`EmailService`), mã hoá mật khẩu BCrypt (`HashUtil`), quản lý token xác thực (`TokenManager`). |
| **`resources/`** | Chứa tài nguyên tĩnh: icon, hình ảnh sản phẩm, theme/wallpaper cho giao diện. Chia thành `Default/` (giao diện chính) và `Login/` (màn hình đăng nhập). |
| **`com/mycompany/demoproject/`** | Chứa class `Demoproject.java` — **điểm khởi chạy** (entry point) của ứng dụng. |

---

## 🔄 3. Luồng Hoạt Động (Data Flow)

### 3.1. Luồng Đăng nhập

```
┌─────────────────┐     ┌──────────────────┐     ┌────────────────────┐     ┌──────────┐
│  Demoproject     │────▶│  AuthProcess     │────▶│  ConnectionUtils   │────▶│  Oracle  │
│  (Entry Point)   │     │  .validateToken()│     │  .getMyConnection()│     │    DB    │
└─────────────────┘     └──────────────────┘     └────────────────────┘     └──────────┘
        │                        │
        │ Token hợp lệ?         │ Trả về Role
        ▼                        ▼
  ┌─────────────┐         ┌─────────────┐
  │ ADMIN       │         │ CUSTOMER    │         ┌──────────────┐
  │ → Admin/Main│         │ → Cust/Main │         │ INVALID      │
  └─────────────┘         └─────────────┘         │ → SignInView │
                                                   └──────────────┘
```

### 3.2. Luồng khi User bấm nút "Đăng nhập" trên `SignInView`

```
 ① User nhập username/password trên SignInView (View)
            │
            ▼
 ② SignInView gọi → LoginProcess.loginProcess(user, pass)  (Controller)
            │
            ▼
 ③ LoginProcess gọi → ConnectionUtils.getMyConnection()    (ConnectDB)
            │
            ▼
 ④ Query bảng TAIKHOAN, so sánh password bằng HashUtil.checkPassword()  (Common)
            │
            ▼
 ⑤ Nếu đúng → AuthProcess.generateAndSaveToken(maTk)      (Controller)
    ├── INSERT token vào bảng ACCOUNT_TOKEN                 (DB)
    └── TokenManager.saveLocalToken(token)                  (Common — Preferences)
            │
            ▼
 ⑥ Trả về Role ("ADMIN" / "CUSTOMER") → SignInView mở màn hình tương ứng
```

### 3.3. Luồng xem Sản phẩm (Customer)

```
 View.Customers.ProductPanel
        │
        ├── Gọi SanPhamDAO.getAllSanPham()          (Controller)
        │       └── ConnectionUtils → Oracle DB
        │
        ├── Gọi LoaiSanPhamDAO.getAllLoaiSanPham()  (Controller)
        │       └── ConnectionUtils → Oracle DB
        │
        └── Hiển thị danh sách ProductCard trên grid
                │
                └── Click vào 1 card → Mở InfoProductPanel
                        └── Gọi SanPhamDAO.getBienTheByMaSp() → Hiển thị biến thể
```

### 3.4. Tổng quát — Quy tắc luồng dữ liệu

```
  [ View (Swing) ]  →  [ Controller (DAO/Process) ]  →  [ ConnectDB ]  →  [ Oracle DB ]
       ↑                         │
       └─── Model (POJO) ────────┘   (Controller trả về List<Model> cho View render)
```

> **Quy tắc:** View **KHÔNG BAO GIỜ** truy vấn DB trực tiếp. Mọi thao tác DB đều phải đi qua Controller.

---

## 📋 4. Danh Sách File Cốt Lõi (Key Files)

### 4.1. Entry Point & Cấu hình

| File | Chức năng |
|------|-----------|
| `Demoproject.java` | Hàm `main()` — Khởi tạo FlatLaf theme, kiểm tra token, điều hướng tới Admin/Customer/SignIn |
| `pom.xml` | Cấu hình Maven: dependencies (FlatLaf, ojdbc8, jBCrypt, JavaMail, Swing DateTime Picker) |
| `ConnectionOracle.java` | Khai báo chuỗi kết nối Oracle (host, SID, user, pass) |
| `ConnectionUtils.java` | Cung cấp `getMyConnection()` — factory method lấy Connection |

### 4.2. Xác thực & Bảo mật

| File | Chức năng |
|------|-----------|
| `LoginProcess.java` | Xác thực đăng nhập: query TAIKHOAN, check BCrypt hash, tạo token |
| `AuthProcess.java` | Quản lý vòng đời token: tạo (`generateAndSaveToken`), xác thực (`validateToken`), thu hồi (`revokeToken`), lấy tên user (`getFullNameFromToken`) |
| `HashUtil.java` | Mã hoá mật khẩu BCrypt (`hashPassword`, `checkPassword`) |
| `TokenManager.java` | Lưu trữ token cục bộ qua `java.util.prefs.Preferences` |
| `EmailService.java` | Gửi email OTP qua Gmail SMTP (dùng cho đăng ký & quên mật khẩu) |

### 4.3. Đăng ký & Quên mật khẩu

| File | Chức năng |
|------|-----------|
| `CreateAccountProcess.java` | Tạo tài khoản mới: INSERT KHACHHANG + TAIKHOAN (có transaction rollback) |
| `CreateUserProcess.java` | Kiểm tra trùng lặp SDT/Email trước khi đăng ký |
| `ForgottedPasswordProcess.java` | Tìm tài khoản qua username/email, đặt lại mật khẩu |

### 4.4. Nghiệp vụ Sản phẩm & Giỏ hàng

| File | Chức năng |
|------|-----------|
| `SanPhamDAO.java` | CRUD sản phẩm: `getAllSanPham`, `searchByName`, `searchByPriceRange`, `searchAdvanced`, `getProductsByCategory`, `getBienTheByMaSp` |
| `LoaiSanPhamDAO.java` | Lấy danh sách loại sản phẩm (danh mục) |
| `CartManager.java` | Singleton quản lý giỏ hàng: thêm item (gộp nếu trùng), xoá, làm rỗng |
| `WarrantyCheckingProcess.java` | Tra cứu bảo hành sản phẩm qua IMEI/Serial |

### 4.5. Giao diện chính

| File | Chức năng |
|------|-----------|
| `View/SignIn/SignInView.java` | Màn hình đăng nhập |
| `View/Admin/Main.java` | JFrame chính của Admin, chứa sidebar menu + content panel |
| `View/Customers/Main.java` | JFrame chính của Customer |
| `View/Customers/ProductPanel/ProductPanel.java` | Grid hiển thị sản phẩm, tìm kiếm, lọc |
| `View/Customers/ProductPanel/ProductCard.java` | Card UI cho mỗi sản phẩm |
| `View/Customers/CartDrawer.java` | Drawer panel giỏ hàng |

---

## 🧰 5. Dependencies (Thư viện bên thứ 3)

| Thư viện | Version | Mục đích |
|----------|---------|----------|
| **FlatLaf** | 3.4.1 | Modern Look & Feel cho Swing (thay thế giao diện mặc định) |
| **FlatLaf Roboto Fonts** | 2.137 | Font Roboto cho FlatLaf |
| **ojdbc8** | 21.1.0.0 | JDBC Driver kết nối Oracle Database |
| **jBCrypt** | 0.4 | Mã hoá mật khẩu bằng thuật toán BCrypt |
| **JavaMail** | 1.6.2 | Gửi email OTP (Gmail SMTP) |
| **Swing DateTime Picker** | 2.1.3 | Component chọn ngày tháng cho Swing |
| **Modal Dialog** | 2.6.1-SNAPSHOT | Component dialog kiểu modal |

---

## 🚀 6. Hướng Dẫn Mở Rộng (How to Extend)

### Khi bạn cần thêm một màn hình chức năng mới (VD: "Quản lý Nhà cung cấp")

#### Bước 1 — Tạo Model (nếu cần bảng mới)

Tạo file `Model/NhaCungCap.java`:
```java
package Model;

public class NhaCungCap {
    private int maNcc;
    private String tenNcc;
    private String sdt;
    // ... constructor, getter, setter
}
```

#### Bước 2 — Tạo Controller (DAO)

Tạo file `Controller/NhaCungCapDAO.java`:
```java
package Controller;

import ConnectDB.ConnectionUtils;
import Model.NhaCungCap;
import java.sql.*;
import java.util.*;

public class NhaCungCapDAO {
    public static List<NhaCungCap> getAll() {
        // Gọi ConnectionUtils.getMyConnection() → query → trả List<NhaCungCap>
    }

    public static boolean insert(NhaCungCap ncc) {
        // INSERT INTO ...
    }
}
```

#### Bước 3 — Tạo View (Panel giao diện)

Tạo file trong thư mục View tương ứng với vai trò:
- **Admin:** `View/Admin/NhaCungCapPanel.java` (và `.form` nếu dùng NetBeans GUI Builder)
- **Customer:** `View/Customers/XxxPanel.java`

```java
package View.Admin;

public class NhaCungCapPanel extends javax.swing.JPanel {
    public NhaCungCapPanel() {
        initComponents();
        loadData();
    }

    private void loadData() {
        List<NhaCungCap> list = NhaCungCapDAO.getAll();
        // Hiển thị lên JTable hoặc custom panel
    }
}
```

#### Bước 4 — Đăng ký vào Menu

Mở file `View/Admin/Main.java` (hoặc `ListMenu.java`), thêm menu item mới và xử lý sự kiện để hiển thị `NhaCungCapPanel` khi click.

#### Tóm tắt checklist

| # | Hành động | Thư mục |
|---|-----------|---------|
| 1 | Tạo POJO mới | `Model/` |
| 2 | Tạo DAO mới | `Controller/` (hoặc sub-package) |
| 3 | Tạo Panel/View mới | `View/Admin/` hoặc `View/Customers/` |
| 4 | Đăng ký menu item | `View/Admin/Main.java` hoặc `ListMenu.java` |
| 5 | Thêm icon (nếu cần) | `resources/Default/Icon/` |

---

## ⚠️ 7. Lưu Ý Quan Trọng

1. **Kết nối DB:** Thông tin kết nối Oracle đang hardcode trong `ConnectionOracle.java`. Khi deploy cần đổi `hostName`, `sid`, `userName`, `password` cho phù hợp.
2. **File `.class`:** Trong source tree hiện có một số file `.class` bị commit nhầm (trong `ConnectDB/`, `Common/`, `View/`). Nên thêm `*.class` vào `.gitignore` và xoá chúng khỏi Git.
3. **Credentials:** File `EmailService.java` chứa App Password Gmail. **Không nên** commit thông tin nhạy cảm lên Git. Nên chuyển sang biến môi trường hoặc file config riêng.
4. **Kiến trúc:** Dự án đang theo mô hình **MVC biến thể** (Model – View – Controller/DAO). Controller đóng vai trò vừa là Business Logic vừa là DAO.

---

*Tài liệu được tạo tự động bởi Tech Lead Review — Cập nhật lần cuối: 2026-05-16*
