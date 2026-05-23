package Model;

public class LoaiSanPham {
    private int maLsp;
    private String tenLsp;
    private String hinhAnh;
    private String trangThai;
    private String moTa;

    public LoaiSanPham() {
    }

    public LoaiSanPham(int maLsp, String tenLsp, String hinhAnh, String trangThai, String moTa) {
        this.maLsp = maLsp;
        this.tenLsp = tenLsp;
        this.hinhAnh = hinhAnh;
        this.trangThai = trangThai;
        this.moTa = moTa;
    }

    public int getMaLsp() {
        return maLsp;
    }

    public void setMaLsp(int maLsp) {
        this.maLsp = maLsp;
    }

    public String getTenLsp() {
        return tenLsp;
    }

    public void setTenLsp(String tenLsp) {
        this.tenLsp = tenLsp;
    }

    public String getHinhAnh() {
        return hinhAnh;
    }

    public void setHinhAnh(String hinhAnh) {
        this.hinhAnh = hinhAnh;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
    
    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    private int tongSoMatHang;

    public int getTongSoMatHang() {
        return tongSoMatHang;
    }

    public void setTongSoMatHang(int tongSoMatHang) {
        this.tongSoMatHang = tongSoMatHang;
    }
}
