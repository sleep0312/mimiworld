package com.mimiworld.mimiworld.client.screen;

import com.mimiworld.mimiworld.menu.GridCookingPotMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * 烤箱 GUI：贴图与参数与大煮锅完全相同（oven.png 176x196，3x3 网格 + 三横箭头进度）。
 */
public class OvenScreen extends GridCookingPotScreen {

    public OvenScreen(GridCookingPotMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title,
                "oven.png", 196, 102,
                28, 74,
                88, 22, 24);
    }
}
