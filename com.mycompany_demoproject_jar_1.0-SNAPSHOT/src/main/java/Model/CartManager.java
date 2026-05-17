package Model;

import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static final CartManager instance = new CartManager();
    private final List<CartItem> items = new ArrayList<>();

    private CartManager() {}

    public static CartManager getInstance() {
        return instance;
    }

    public void addItem(CartItem item) {
        // Check if item already exists
        for (CartItem existing : items) {
            boolean sameProduct = existing.getProduct().getMaSp() == item.getProduct().getMaSp();
            boolean sameVariant = (existing.getVariant() == null && item.getVariant() == null) ||
                                  (existing.getVariant() != null && item.getVariant() != null && 
                                   existing.getVariant().getMaBienThe() == item.getVariant().getMaBienThe());
            if (sameProduct && sameVariant) {
                existing.setQuantity(existing.getQuantity() + item.getQuantity());
                return;
            }
        }
        items.add(item);
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void clear() {
        items.clear();
    }
}
