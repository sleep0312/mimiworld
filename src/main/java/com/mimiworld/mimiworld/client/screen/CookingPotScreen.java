package com.mimiworld.mimiworld.client.screen;

import com.mimiworld.mimiworld.menu.GridCookingPotMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * 煮锅 GUI：2x2 网格 + 右上输入 + 燃料 + 输出（176x178），三横箭头进度。
 */
public class CookingPotScreen extends GridCookingPotScreen {

    public CookingPotScreen(GridCookingPotMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title,
                "cooking_pot.png", 178, 84,
                19, 56,
                82, 13, 30);
    }
}
