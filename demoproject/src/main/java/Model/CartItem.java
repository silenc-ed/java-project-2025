package Model;

public class CartItem {
    private SanPham product;
    private BienTheSanPham variant; // Null if no variant selected
    private int quantity;

    public CartItem(SanPham product, BienTheSanPham variant, int quantity) {
        this.product = product;
        this.variant = variant;
        this.quantity = quantity;
    }

    public SanPham getProduct() { return product; }
    public void setProduct(SanPham product) { this.product = product; }

    public BienTheSanPham getVariant() { return variant; }
    public void setVariant(BienTheSanPham variant) { this.variant = variant; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    
    public double getTotalPrice() {
        double price = variant != null ? variant.getGiaBan() : product.getGiaBan();
        return price * quantity;
    }
}
