package com.mimiworld.mimiworld.client.screen;

import com.mimiworld.mimiworld.menu.GridCookingPotMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * 大煮锅 GUI：3x3 网格 + 右上输入 + 燃料 + 输出（176x196），三横箭头进度。
 */
public class LargeCookingPotScreen extends GridCookingPotScreen {

    public LargeCookingPotScreen(GridCookingPotMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title,
                "large_cooking_pot.png", 196, 102,
                28, 74,
                88, 22, 24);
    }
}
