package com.mimiworld.mimiworld.client.screen;

import com.mimiworld.mimiworld.menu.GridCookingPotMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * 超大煮锅 GUI：4x4 网格 + 右上输入 + 燃料 + 输出（176x214），三横箭头进度。
 */
public class ExtraLargeCookingPotScreen extends GridCookingPotScreen {

    public ExtraLargeCookingPotScreen(GridCookingPotMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title,
                "extra_large_cooking_pot.png", 214, 120,
                37, 92,
                94, 31, 18);
    }
}
