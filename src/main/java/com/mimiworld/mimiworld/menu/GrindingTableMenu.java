package com.mimiworld.mimiworld.menu;

import com.mimiworld.mimiworld.block.ModBlocks;
import com.mimiworld.mimiworld.block.entity.MultiInputProcessorBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.level.block.Block;

/**
 * 碾磨台菜单：左右两列各三个输入槽（x=26 / x=130，y=15/36/57），
 * 底部中央输出槽 (79,81)。面板加高到 192，玩家背包起始 y=116。
 * 坐标与 grinding_table.png 槽框严格一致。
 */
public class GrindingTableMenu extends MultiInputProcessorMenu {

    public static final int COL_LEFT_X = 26;
    public static final int COL_RIGHT_X = 130;
    public static final int ROW_0_Y = 15;
    public static final int ROW_1_Y = 36;
    public static final int ROW_2_Y = 57;
    public static final int SLOT_OUTPUT_X = 79;
    public static final int SLOT_OUTPUT_Y = 81;

    private static final int PLAYER_INV_Y = 116;

    /** 客户端构造 */
    public GrindingTableMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory,
                new SimpleContainer(7),
                new SimpleContainerData(MultiInputProcessorBlockEntity.DATA_COUNT),
                ContainerLevelAccess.NULL);
    }

    /** 服务端构造：由 GrindingTableBlockEntity#createMenu 调用 */
    public GrindingTableMenu(int containerId, Inventory playerInventory,
                             Container container, ContainerData data, ContainerLevelAccess access) {
        super(ModMenuTypes.GRINDING_TABLE_MENU.get(), containerId, playerInventory, container, data, access);
    }

    @Override
    protected int playerInventoryY() {
        return PLAYER_INV_Y;
    }

    @Override
    protected Block validBlock() {
        return ModBlocks.GRINDING_TABLE.get();
    }

    @Override
    protected void addMachineSlots(Container container) {
        // 槽 0~2：左列上中下；槽 3~5：右列上中下；槽 6：底部中央输出
        addInputSlot(container, 0, COL_LEFT_X, ROW_0_Y);
        addInputSlot(container, 1, COL_LEFT_X, ROW_1_Y);
        addInputSlot(container, 2, COL_LEFT_X, ROW_2_Y);
        addInputSlot(container, 3, COL_RIGHT_X, ROW_0_Y);
        addInputSlot(container, 4, COL_RIGHT_X, ROW_1_Y);
        addInputSlot(container, 5, COL_RIGHT_X, ROW_2_Y);
        addResultSlot(container, 6, SLOT_OUTPUT_X, SLOT_OUTPUT_Y);
    }
}
