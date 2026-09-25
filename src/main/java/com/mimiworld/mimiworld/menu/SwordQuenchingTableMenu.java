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
 * 淬剑台菜单：上排两个输入槽 (35,15)/(121,15)、中间输入槽 (79,38)、
 * 最下方输出槽 (79,61)，右侧为开始按钮（不是槽位；点击经 clickMenuButton 通道处理）。
 * 坐标与 sword_quenching_table.png 严格一致。
 */
public class SwordQuenchingTableMenu extends MultiInputProcessorMenu {

    public static final int SLOT_TOP_LEFT_X = 35;
    public static final int SLOT_TOP_LEFT_Y = 15;
    public static final int SLOT_TOP_RIGHT_X = 121;
    public static final int SLOT_TOP_RIGHT_Y = 15;
    public static final int SLOT_CENTER_X = 79;
    public static final int SLOT_CENTER_Y = 38;
    public static final int SLOT_OUTPUT_X = 79;
    public static final int SLOT_OUTPUT_Y = 61;

    /** 客户端构造 */
    public SwordQuenchingTableMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory,
                new SimpleContainer(4),
                new SimpleContainerData(MultiInputProcessorBlockEntity.DATA_COUNT),
                ContainerLevelAccess.NULL);
    }

    /** 服务端构造：由 SwordQuenchingTableBlockEntity#createMenu 调用 */
    public SwordQuenchingTableMenu(int containerId, Inventory playerInventory,
                                   Container container, ContainerData data, ContainerLevelAccess access) {
        super(ModMenuTypes.SWORD_QUENCHING_TABLE_MENU.get(), containerId, playerInventory, container, data, access);
    }

    @Override
    protected Block validBlock() {
        return ModBlocks.SWORD_QUENCHING_TABLE.get();
    }

    @Override
    protected void addMachineSlots(Container container) {
        addInputSlot(container, 0, SLOT_TOP_LEFT_X, SLOT_TOP_LEFT_Y);
        addInputSlot(container, 1, SLOT_TOP_RIGHT_X, SLOT_TOP_RIGHT_Y);
        addInputSlot(container, 2, SLOT_CENTER_X, SLOT_CENTER_Y);
        addResultSlot(container, 3, SLOT_OUTPUT_X, SLOT_OUTPUT_Y);
    }
}
