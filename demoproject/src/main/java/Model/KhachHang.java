/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

/**
 *
 * @author user
 */
public class KhachHang {
    private long maKH;
    private String hoTen;
    private String sDT;
    private String diaChi;
    private String email;
    private int diemTichLuy;

    // Hàm khởi tạo không tham số
    public KhachHang() {
    }

    // Hàm khởi tạo có tham số
    public KhachHang(long maKH, String hoTen, String sDT,
                     String diaChi, String email, int diemTichLuy) {
        this.maKH = maKH;
        this.hoTen = hoTen;
        this.sDT = sDT;
        this.diaChi = diaChi;
        this.email = email;
        this.diemTichLuy = diemTichLuy;
    }

    // Getter và Setter cho maKH
    public long getMaKH() {
        return maKH;
    }

    public void setMaKH(long maKH) {
        this.maKH = maKH;
    }

    // Getter và Setter cho hoTen
    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    // Getter và Setter cho sDT
    public String getsDT() {
        return sDT;
    }

    public void setsDT(String sDT) {
        this.sDT = sDT;
    }

    // Getter và Setter cho diaChi
    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    // Getter và Setter cho email
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // Getter và Setter cho diemTichLuy
    public int getDiemTichLuy() {
        return diemTichLuy;
    }

    public void setDiemTichLuy(int diemTichLuy) {
        this.diemTichLuy = diemTichLuy;
    }
}
