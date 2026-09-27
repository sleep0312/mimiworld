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
 * 超大煮锅菜单：4x4 网格（16 入）+ 右上输入 + 燃料 + 输出（extra_large_cooking_pot.png 176x214）。
 */
public class ExtraLargeCookingPotMenu extends GridCookingPotMenu {

    /** 客户端构造 */
    public ExtraLargeCookingPotMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory,
                new SimpleContainer(19),
                new SimpleContainerData(4), ContainerLevelAccess.NULL);
    }

    /** 服务端构造：4x4 网格 16 + 右上输入 1 + 燃料 1 + 输出 1 = 19 槽 */
    public ExtraLargeCookingPotMenu(int containerId, Inventory playerInventory,
                                    Container container, ContainerData data, ContainerLevelAccess access) {
        super(ModMenuTypes.EXTRA_LARGE_COOKING_POT_MENU.get(), containerId, playerInventory,
                container, data, access,
                4, 8, 17, 18,
                116, 44, 80,
                36, 110,
                132);
    }

    @Override
    protected Block validBlock() {
        return ModBlocks.EXTRA_LARGE_COOKING_POT.get();
    }
}
