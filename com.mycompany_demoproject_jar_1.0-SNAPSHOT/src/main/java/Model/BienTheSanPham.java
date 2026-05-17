package Model;

public class BienTheSanPham {
    private int maBienThe;
    private int maSp;
    private String tenBienThe;
    private double giaBan;
    private String trangThai;

    public BienTheSanPham() {
    }

    public BienTheSanPham(int maBienThe, int maSp, String tenBienThe, double giaBan, String trangThai) {
        this.maBienThe = maBienThe;
        this.maSp = maSp;
        this.tenBienThe = tenBienThe;
        this.giaBan = giaBan;
        this.trangThai = trangThai;
    }

    public int getMaBienThe() { return maBienThe; }
    public void setMaBienThe(int maBienThe) { this.maBienThe = maBienThe; }

    public int getMaSp() { return maSp; }
    public void setMaSp(int maSp) { this.maSp = maSp; }

    public String getTenBienThe() { return tenBienThe; }
    public void setTenBienThe(String tenBienThe) { this.tenBienThe = tenBienThe; }

    public double getGiaBan() { return giaBan; }
    public void setGiaBan(double giaBan) { this.giaBan = giaBan; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}
