package com.mycompany.demo.Common;

import org.mindrot.jbcrypt.BCrypt;

public class hashUtil {

//    /**
//     * Mã hóa mật khẩu (Dùng khi Đăng ký)
//     * BCrypt sẽ tự tạo Salt và trộn vào chuỗi trả về.
//     */
    
    public static String hashPassword(String password) {
        // gensalt() mặc định là 10 rounds, đủ mạnh cho các ứng dụng hiện nay
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

//    /**
//     * Kiểm tra mật khẩu (Dùng khi Đăng nhập)
//     * @param plainPassword Mật khẩu người dùng nhập vào
//     * @param hashedPassword Chuỗi hash lấy từ Database
//     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            // Trường hợp dữ liệu trong DB không phải định dạng BCrypt
            return false;
        }
    }

    // Test nhanh
    public static void main(String[] args) {
        String password = "123456";
        
        // Lần 1
        String hashed1 = hashPassword(password);
        System.out.println("Hash lần 1: " + hashed1);
        
        // Lần 2 (Bạn sẽ thấy nó khác hoàn toàn lần 1 dù cùng mật khẩu "123456")
        String hashed2 = hashPassword(password);
        System.out.println("Hash lần 2: " + hashed2);

        // Kiểm tra
        System.out.println("Kiểm tra lần 1: " + checkPassword("123456", hashed1));
        System.out.println("Kiểm tra lần 2: " + checkPassword("123456", hashed2));
    }
}