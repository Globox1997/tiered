package draylar.tiered.reforge.widget;

import org.jetbrains.annotations.Nullable;

import draylar.tiered.network.TieredClientPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.libz.api.InventoryTab;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class ReforgeTab extends InventoryTab {

    public ReforgeTab(Component title, @Nullable Identifier texture, int preferedPos, Class<?>... screenClasses) {
        super(title, texture, preferedPos, screenClasses);
    }

    @Override
    public void onClick(Minecraft client) {
        TieredClientPacket.writeC2SScreenPacket((int) client.mouseHandler.xpos(), (int) client.mouseHandler.ypos(), true);
    }

}
