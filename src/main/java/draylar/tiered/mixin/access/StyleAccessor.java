package draylar.tiered.mixin.access;

import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.TextColor;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Optional;

@Mixin(Style.class)
public interface StyleAccessor {

    @Invoker("of")
    static Style invokeOf(Optional<TextColor> color, Optional<Boolean> bold, Optional<Boolean> italic, Optional<Boolean> underlined, Optional<Boolean> strikethrough, Optional<Boolean> obfuscated,
            Optional<ClickEvent> optional, Optional<HoverEvent> optional2, Optional<String> optional3, Optional<Identifier> optional4) {
        throw new AssertionError("This shouldn't happen!");
    }

}
