package com.mimiworld.mimiworld.client.screen;

import com.mimiworld.mimiworld.menu.GridCookingPotMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * 巨无霸大大大煮锅 GUI：5x5 网格 + 右上输入 + 燃料 + 输出（176x232），三横箭头进度。
 */
public class JumboMegaCookingPotScreen extends GridCookingPotScreen {

    public JumboMegaCookingPotScreen(GridCookingPotMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title,
                "jumbo_mega_cooking_pot.png", 232, 138,
                46, 110,
                102, 40, 10);
    }
}
