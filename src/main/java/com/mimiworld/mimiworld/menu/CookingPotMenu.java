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
 * 煮锅菜单：2x2 网格（4 入）+ 右上输入 + 燃料 + 输出（cooking_pot.png 176x178）。
 */
public class CookingPotMenu extends GridCookingPotMenu {

    /** 客户端构造 */
    public CookingPotMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory,
                new SimpleContainer(7),
                new SimpleContainerData(4), ContainerLevelAccess.NULL);
    }

    /** 服务端构造：2x2 网格 4 + 右上输入 1 + 燃料 1 + 输出 1 = 7 槽 */
    public CookingPotMenu(int containerId, Inventory playerInventory,
                          Container container, ContainerData data, ContainerLevelAccess access) {
        super(ModMenuTypes.COOKING_POT_MENU.get(), containerId, playerInventory,
                container, data, access,
                2, 8, 17, 18,
                116, 17, 53,
                18, 74,
                96);
    }

    @Override
    protected Block validBlock() {
        return ModBlocks.COOKING_POT.get();
    }
}
