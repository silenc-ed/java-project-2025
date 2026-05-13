package View.Customers.ProductPanel;

import Model.SanPham;
import java.util.List;

public interface ProductFilterListener {
    void onProductsFiltered(List<SanPham> products, String sectionTitle);
}
