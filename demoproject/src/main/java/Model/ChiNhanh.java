package Model;

public class ChiNhanh {
    private int maCn;
    private String tenCn;
    private String diaChi;
    private String sdtHotline;
    private String trangThai;

    public ChiNhanh() {}

    public ChiNhanh(int maCn, String tenCn) {
        this.maCn = maCn;
        this.tenCn = tenCn;
    }

    public int getMaCn() { return maCn; }
    public void setMaCn(int maCn) { this.maCn = maCn; }

    public String getTenCn() { return tenCn; }
    public void setTenCn(String tenCn) { this.tenCn = tenCn; }

    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }

    public String getSdtHotline() { return sdtHotline; }
    public void setSdtHotline(String sdtHotline) { this.sdtHotline = sdtHotline; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    @Override
    public String toString() {
        return tenCn; // Hữu ích khi đưa vào JComboBox
    }
}
