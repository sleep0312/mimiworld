package com.mimiworld.mimiworld.client.screen;

import com.mimiworld.mimiworld.menu.PestleRealmTableMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** 杵界台 GUI：2x2 输入网格 + 右下结果槽，标题为"杵界台"。 */
public class PestleRealmTableScreen extends GridCraftingTableScreen<PestleRealmTableMenu> {

    public PestleRealmTableScreen(PestleRealmTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title,
                "pestle_realm_table.png",
                166,  // 面板高度同原版工作台
                72);  // 背包标题 Y
    }
}
