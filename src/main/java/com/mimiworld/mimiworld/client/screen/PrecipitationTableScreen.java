package com.mimiworld.mimiworld.client.screen;

import com.mimiworld.mimiworld.MimiWorld;
import com.mimiworld.mimiworld.menu.PrecipitationTableMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * 沉淀台 GUI：贴图 precipitation_table.png，两输入槽之间为正三角 ▲（加工进度指示）。
 */
public class PrecipitationTableScreen extends DualInputProcessorScreen<PrecipitationTableMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MimiWorld.MODID, "textures/gui/precipitation_table.png");

    public PrecipitationTableScreen(PrecipitationTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TEXTURE);
    }
}
