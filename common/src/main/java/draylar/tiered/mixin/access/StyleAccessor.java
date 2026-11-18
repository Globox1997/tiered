package draylar.tiered.mixin.access;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Optional;

@Mixin(Style.class)
public interface StyleAccessor {

    @Invoker("create")
    static Style invokeOf(Optional<TextColor> color, Optional<Integer> shadowColor, Optional<Boolean> bold, Optional<Boolean> italic, Optional<Boolean> underlined, Optional<Boolean> strikethrough,
            Optional<Boolean> obfuscated, Optional<ClickEvent> optional, Optional<HoverEvent> optional2, Optional<String> optional3, Optional<FontDescription> optional4) {
        throw new AssertionError("This shouldn't happen!");
    }

}
