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
 * 大煮锅菜单：3x3 网格（9 入）+ 右上输入 + 燃料 + 输出（large_cooking_pot.png 176x196）。
 */
public class LargeCookingPotMenu extends GridCookingPotMenu {

    /** 客户端构造 */
    public LargeCookingPotMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory,
                new SimpleContainer(12),
                new SimpleContainerData(4), ContainerLevelAccess.NULL);
    }

    /** 服务端构造：3x3 网格 9 + 右上输入 1 + 燃料 1 + 输出 1 = 12 槽 */
    public LargeCookingPotMenu(int containerId, Inventory playerInventory,
                               Container container, ContainerData data, ContainerLevelAccess access) {
        super(ModMenuTypes.LARGE_COOKING_POT_MENU.get(), containerId, playerInventory,
                container, data, access,
                3, 8, 17, 18,
                116, 35, 71,
                27, 92,
                114);
    }

    @Override
    protected Block validBlock() {
        return ModBlocks.LARGE_COOKING_POT.get();
    }
}
