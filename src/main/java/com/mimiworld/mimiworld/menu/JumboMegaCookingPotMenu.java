package com.mimiworld.mimiworld.menu;

import com.mimiworld.mimiworld.block.ModBlocks;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.level.block.Block;

/**
 * 巨无霸大大大煮锅菜单：5x5 网格（25 入）+ 右上输入 + 燃料 + 输出（jumbo_mega_cooking_pot.png 176x232）。
 */
public class JumboMegaCookingPotMenu extends GridCookingPotMenu {

    /** 客户端构造 */
    public JumboMegaCookingPotMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory,
                new SimpleContainer(28),
                new SimpleContainerData(4), ContainerLevelAccess.NULL);
    }

    /** 服务端构造：5x5 网格 25 + 右上输入 1 + 燃料 1 + 输出 1 = 28 槽 */
    public JumboMegaCookingPotMenu(int containerId, Inventory playerInventory,
                                   Container container, ContainerData data, ContainerLevelAccess access) {
        super(ModMenuTypes.JUMBO_MEGA_COOKING_POT_MENU.get(), containerId, playerInventory,
                container, data, access,
                5, 8, 17, 18,
                116, 53, 89,
                45, 128,
                150);
    }

    @Override
    protected Block validBlock() {
        return ModBlocks.JUMBO_MEGA_COOKING_POT.get();
    }
}
