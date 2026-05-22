package Common;

import java.util.Properties;
import java.util.Random;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailService {

    // THAY THẾ BẰNG EMAIL VÀ MẬT KHẨU ỨNG DỤNG (APP PASSWORD) CỦA BẠN
    private static final String SMTP_USER = "chieuthuhanoi06@gmail.com"; 
    private static final String SMTP_PASSWORD = "xfzt vyof uadr kprh";

    /**
     * Gửi email chứa mã OTP đến địa chỉ nhận.
     * @param toEmail Email người nhận
     * @param otp Mã xác thực
     * @return true nếu gửi thành công, ngược lại false
     */
    public static boolean sendOTP(String toEmail, String otp) {
        // Cấu hình properties cho SMTP server của Google
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        // Khởi tạo phiên làm việc (Session) với xác thực
        Session session = Session.getInstance(props,
            new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(SMTP_USER, SMTP_PASSWORD);
                }
            });

        try {
            // Tạo thông điệp email
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SMTP_USER));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Mã xác thực đăng ký tài khoản (OTP)");
            
            // Nội dung email
            String emailContent = "Xin chào,\n\n"
                    + "Cảm ơn bạn đã đăng ký tài khoản. Đây là mã xác thực (OTP) của bạn:\n"
                    + "Mã OTP: " + otp + "\n\n"
                    + "Vui lòng không chia sẻ mã này cho bất kỳ ai.\n"
                    + "Trân trọng.";
            message.setText(emailContent);

            // Gửi email
            Transport.send(message);
            System.out.println("Gửi OTP thành công tới: " + toEmail);
            return true;

        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Tạo mã OTP ngẫu nhiên gồm 6 chữ số
     * @return Chuỗi mã OTP
     */
    public static String generateOTP() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // Đảm bảo luôn có 6 chữ số
        return String.valueOf(otp);
    }
}
