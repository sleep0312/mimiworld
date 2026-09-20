package com.mimiworld.mimiworld.client.screen;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.menu.IonizationTableMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * 电离台 GUI：贴图 ionization_table.png，两输入槽之间为倒三角 ▼（加工进度指示）。
 */
public class IonizationTableScreen extends DualInputProcessorScreen<IonizationTableMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MimiWorld.MODID, "textures/gui/ionization_table.png");

    public IonizationTableScreen(IonizationTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TEXTURE);
    }
}
