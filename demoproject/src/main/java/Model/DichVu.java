package Model;

public class DichVu {
    private int maDv;
    private String tenDv;
    private String moTa;
    private double giaCuoc;
    private int trangThai; // 1: Hoạt động, 0: Ngừng

    public DichVu() {
    }

    public DichVu(int maDv, String tenDv, String moTa, double giaCuoc, int trangThai) {
        this.maDv = maDv;
        this.tenDv = tenDv;
        this.moTa = moTa;
        this.giaCuoc = giaCuoc;
        this.trangThai = trangThai;
    }

    public int getMaDv() { return maDv; }
    public void setMaDv(int maDv) { this.maDv = maDv; }

    public String getTenDv() { return tenDv; }
    public void setTenDv(String tenDv) { this.tenDv = tenDv; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    public double getGiaCuoc() { return giaCuoc; }
    public void setGiaCuoc(double giaCuoc) { this.giaCuoc = giaCuoc; }

    public int getTrangThai() { return trangThai; }
    public void setTrangThai(int trangThai) { this.trangThai = trangThai; }
}
