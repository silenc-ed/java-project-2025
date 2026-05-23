package Model;

public class SerialNumber {
    private String maSerial;
    private int maBienThe;
    private String trangThai;
    private java.sql.Date ngayNhap;

    public SerialNumber() {
    }

    public String getMaSerial() {
        return maSerial;
    }

    public void setMaSerial(String maSerial) {
        this.maSerial = maSerial;
    }

    public int getMaBienThe() {
        return maBienThe;
    }

    public void setMaBienThe(int maBienThe) {
        this.maBienThe = maBienThe;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public java.sql.Date getNgayNhap() {
        return ngayNhap;
    }

    public void setNgayNhap(java.sql.Date ngayNhap) {
        this.ngayNhap = ngayNhap;
    }
}
