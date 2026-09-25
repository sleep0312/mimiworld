package com.mimiworld.mimiworld.client.screen;

import com.mimiworld.mimiworld.menu.BowForgingTableMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** 铸弓台 GUI：原版工作台贴图，标题为"铸弓台"。 */
public class BowForgingTableScreen extends ForgingTableScreen<BowForgingTableMenu> {

    public BowForgingTableScreen(BowForgingTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }
}
