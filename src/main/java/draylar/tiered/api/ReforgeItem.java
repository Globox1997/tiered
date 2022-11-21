package draylar.tiered.api;

import java.util.List;

public class ReforgeItem {
    private final ItemVerifier product;
    private final List<ItemVerifier> base;
    private final boolean cover;

    public ReforgeItem(ItemVerifier product, List<ItemVerifier> base, boolean cover) {
        this.product = product;
        this.base = base;
        this.cover = cover;
    }

    public ItemVerifier getProduct() {
        return product;
    }

    public List<ItemVerifier> getBase() {
        return base;
    }

    public boolean isCover() {
        return cover;
    }
}
