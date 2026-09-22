package com.mimiworld.mimiworld.datagen;

import com.mimiworld.mimiworld.ModItems;
import com.mimiworld.mimiworld.entity.ModEntities;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * 实体战利品表数据生成（用户实体编号 1-30）。
 *
 * <p>每条掉落「概率数量x物品编码」对应一个独立的掉落池（rolls=1），
 * 数量区间 {@code min~max} 用 set_count + UniformGenerator 表达，min=0 即可能不掉；
 * 生成路径 data/mimiworld/loot_table/entities/&lt;实体注册名&gt;.json，
 * 与 EntityType.Builder 自动推导的默认战利品路径一致。</p>
 */
public class ModLootTableProvider extends LootTableProvider {

    public ModLootTableProvider(PackOutput output) {
        super(output, Set.of(), List.of(
                new SubProviderEntry(EntityDrops::new, LootContextParamSets.ENTITY)));
    }

    /** 单条掉落：物品 + 数量闭区间（min 可为 0 表示概率掉落）。 */
    private record Drop(ItemLike item, int min, int max) {
    }

    /** DROPS[1..30]：index 即用户实体编号。 */
    private static final Drop[][] DROPS = new Drop[31][];

    static {
        var rice = ModItems.byCode(1);
        DROPS[1] = new Drop[]{d(rice, 1, 1), d(ModItems.byCode(246), 1, 1), d(ModItems.byCode(201), 0, 2)};
        DROPS[2] = new Drop[]{d(rice, 4, 5), d(ModItems.byCode(193), 2, 3), d(ModItems.byCode(202), 2, 3)};
        DROPS[3] = new Drop[]{d(rice, 2, 3), d(ModItems.byCode(195), 2, 3), d(ModItems.byCode(203), 1, 2)};
        DROPS[4] = new Drop[]{d(rice, 4, 5), d(ModItems.byCode(197), 2, 3), d(ModItems.byCode(204), 2, 3)};
        DROPS[5] = new Drop[]{d(rice, 1, 1), d(ModItems.byCode(199), 1, 1)};
        DROPS[6] = new Drop[]{d(rice, 1, 1), d(ModItems.byCode(205), 1, 1)};
        DROPS[7] = new Drop[]{d(rice, 10, 20)};
        DROPS[8] = new Drop[]{d(rice, 5, 6), d(ModItems.byCode(206), 0, 1)};
        DROPS[9] = new Drop[]{d(rice, 6, 7), d(ModItems.byCode(207), 3, 4), d(ModItems.byCode(208), 2, 3)};
        DROPS[10] = new Drop[]{d(rice, 8, 9), d(ModItems.byCode(215), 1, 2), d(ModItems.byCode(16), 0, 1)};
        DROPS[11] = new Drop[]{d(rice, 3, 4), d(ModItems.byCode(209), 1, 2)};
        DROPS[12] = new Drop[]{d(rice, 8, 9), d(ModItems.byCode(71), 0, 1), d(Items.GLOWSTONE_DUST, 0, 1)};
        DROPS[13] = new Drop[]{d(rice, 19, 20), d(ModItems.byCode(210), 0, 2)};
        DROPS[14] = new Drop[]{d(ModItems.byCode(211), 0, 2), d(rice, 15, 16)};
        DROPS[15] = new Drop[]{d(ModItems.byCode(212), 0, 2), d(rice, 15, 16)};
        DROPS[16] = new Drop[]{d(rice, 3, 4), d(ModItems.byCode(213), 1, 2)};
        DROPS[17] = new Drop[]{d(rice, 6, 7), d(ModItems.byCode(214), 1, 2)};
        DROPS[18] = new Drop[]{d(rice, 8, 9), d(ModItems.byCode(197), 1, 2)};
        DROPS[19] = new Drop[]{d(ModItems.byCode(256), 0, 1), d(rice, 188, 189)};
        DROPS[20] = new Drop[]{d(rice, 1, 2), d(ModItems.byCode(258), 0, 2)};
        // 界天米：第二条掉落在用户要求下由 260（占位）改为 184 堕落之心
        DROPS[21] = new Drop[]{d(ModItems.byCode(259), 1, 2), d(ModItems.byCode(184), 1, 1),
                d(ModItems.byCode(265), 1, 1), d(ModItems.byCode(296), 0, 1)};
        DROPS[22] = new Drop[]{d(ModItems.byCode(261), 4, 4), d(ModItems.byCode(265), 1, 1), d(ModItems.byCode(298), 3, 4)};
        DROPS[23] = new Drop[]{d(ModItems.byCode(262), 5, 5), d(ModItems.byCode(226), 2, 2),
                d(ModItems.byCode(265), 1, 1), d(ModItems.byCode(300), 2, 3)};
        DROPS[24] = new Drop[]{d(ModItems.byCode(263), 1, 1), d(ModItems.byCode(99), 3, 5),
                d(ModItems.byCode(101), 3, 5), d(ModItems.byCode(100), 3, 5),
                d(ModItems.byCode(265), 1, 1), d(ModItems.byCode(302), 1, 2)};
        DROPS[25] = new Drop[]{d(ModItems.byCode(264), 16, 16), d(ModItems.byCode(265), 1, 1), d(ModItems.byCode(304), 10, 20)};
        DROPS[26] = new Drop[]{d(ModItems.byCode(266), 3, 5), d(ModItems.byCode(80), 1, 1),
                d(ModItems.byCode(265), 1, 1), d(ModItems.byCode(306), 1, 1),
                d(ModItems.byCode(331), 1, 2)};
        DROPS[27] = new Drop[]{d(ModItems.byCode(267), 1, 4), d(ModItems.byCode(185), 1, 1),
                d(ModItems.byCode(265), 1, 1), d(ModItems.byCode(308), 1, 4)};
        DROPS[28] = new Drop[]{d(ModItems.byCode(268), 1, 1), d(ModItems.byCode(84), 3, 3),
                d(ModItems.byCode(265), 1, 1), d(ModItems.byCode(310), 5, 6),
                d(ModItems.byCode(330), 5, 6)};
        DROPS[29] = new Drop[]{d(ModItems.byCode(269), 1, 1), d(ModItems.byCode(60), 3, 5),
                d(ModItems.byCode(265), 1, 1), d(ModItems.byCode(312), 0, 1), d(ModItems.byCode(313), 16, 24)};
        DROPS[30] = new Drop[]{d(ModItems.byCode(270), 2, 2), d(Items.NETHER_STAR, 3, 3),
                d(ModItems.byCode(265), 1, 1), d(ModItems.byCode(314), 1, 2)};
    }

    private static Drop d(ItemLike item, int min, int max) {
        return new Drop(item, min, max);
    }

    private static class EntityDrops implements LootTableSubProvider {
        @Override
        public void generate(BiConsumer<ResourceLocation, LootTable.Builder> consumer) {
            for (int code = 1; code <= 30; code++) {
                LootTable.Builder table = LootTable.lootTable();
                for (Drop drop : DROPS[code]) {
                    table.withPool(LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1.0F))
                            .add(LootItem.lootTableItem(drop.item())
                                    .apply(SetItemCountFunction.setCount(
                                            UniformGenerator.between(drop.min(), drop.max())))));
                }
                consumer.accept(ModEntities.SPECS[code].type().get().getDefaultLootTable(), table);
            }
        }
    }
}
