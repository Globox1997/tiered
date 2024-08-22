package draylar.tiered.api;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class BorderTemplate {

    private final int index;
    private final String texture;
    private final Identifier identifier;
    private final int startGradient;
    private final int endGradient;
    private final int backgroundGradient;
    private final List<String> decider;
    private List<ItemStack> stacks = new ArrayList<ItemStack>();

    public BorderTemplate(int index, String texture, int startGradient, int endGradient, int backgroundGradient, List<String> decider) {
        this.index = index;
        this.texture = texture;
        this.identifier = Identifier.of("tiered", "textures/gui/" + this.texture + ".png");
        this.startGradient = startGradient;
        this.endGradient = endGradient;
        this.backgroundGradient = backgroundGradient;
        this.decider = new ArrayList<String>();
        this.decider.addAll(decider);
    }

    public int getIndex() {
        return this.index;
    }

    public String getTexture() {
        return this.texture;
    }

    public Identifier getIdentifier() {
        return this.identifier;
    }

    public int getStartGradient() {
        return this.startGradient;
    }

    public int getEndGradient() {
        return this.endGradient;
    }

    public int getBackgroundGradient() {
        return this.backgroundGradient;
    }

    public List<String> getDecider() {
        return this.decider;
    }

    public boolean containsDecider(String string) {
        if (this.decider.contains(string)) {
            return true;
        }
        return false;
    }

    public void addStack(ItemStack itemStack) {
        if (!this.stacks.contains(itemStack)) {
            this.stacks.add(itemStack);
        }
    }

    public boolean containsStack(ItemStack itemStack) {
        return this.stacks.contains(itemStack);
    }

}
