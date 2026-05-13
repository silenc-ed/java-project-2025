package Model;

public class SanPham {
    private int maSp;
    private int maLsp;
    private String tenSp;
    private String donViTinh;
    private double giaBan;
    private int thoiGianBh;
    private String hinhAnh;
    private int coQuanLySerial;
    private String trangThai;
    private int soLuongDaBan;
    private String moTa;

    public SanPham() {
    }

    public SanPham(int maSp, int maLsp, String tenSp, String donViTinh, double giaBan, int thoiGianBh, String hinhAnh, int coQuanLySerial, String trangThai, int soLuongDaBan, String moTa) {
        this.maSp = maSp;
        this.maLsp = maLsp;
        this.tenSp = tenSp;
        this.donViTinh = donViTinh;
        this.giaBan = giaBan;
        this.thoiGianBh = thoiGianBh;
        this.hinhAnh = hinhAnh;
        this.coQuanLySerial = coQuanLySerial;
        this.trangThai = trangThai;
        this.soLuongDaBan = soLuongDaBan;
        this.moTa = moTa;
    }

    public int getMaSp() { return maSp; }
    public void setMaSp(int maSp) { this.maSp = maSp; }

    public int getMaLsp() { return maLsp; }
    public void setMaLsp(int maLsp) { this.maLsp = maLsp; }

    public String getTenSp() { return tenSp; }
    public void setTenSp(String tenSp) { this.tenSp = tenSp; }

    public String getDonViTinh() { return donViTinh; }
    public void setDonViTinh(String donViTinh) { this.donViTinh = donViTinh; }

    public double getGiaBan() { return giaBan; }
    public void setGiaBan(double giaBan) { this.giaBan = giaBan; }

    public int getThoiGianBh() { return thoiGianBh; }
    public void setThoiGianBh(int thoiGianBh) { this.thoiGianBh = thoiGianBh; }

    public String getHinhAnh() { return hinhAnh; }
    public void setHinhAnh(String hinhAnh) { this.hinhAnh = hinhAnh; }

    public int getCoQuanLySerial() { return coQuanLySerial; }
    public void setCoQuanLySerial(int coQuanLySerial) { this.coQuanLySerial = coQuanLySerial; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public int getSoLuongDaBan() { return soLuongDaBan; }
    public void setSoLuongDaBan(int soLuongDaBan) { this.soLuongDaBan = soLuongDaBan; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
}
