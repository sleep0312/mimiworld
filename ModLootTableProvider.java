package com.mimiworld.mimiworld.datagen;

import com.mimiworld.mimiworld.ModItems;
import com.mimiworld.mimiworld.entity.ModEntities;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * 实体战利品表数据生成。
 *
 * <p>每条掉落「物品 + 数量区间」对应一个独立的掉落池（rolls=1），
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

    /** 每个实体类型对应的掉落池列表。 */
    private static final Map<EntityType<?>, Drop[]> DROPS = new HashMap<>();

    static {
        var rice = ModItems.RICE;
        DROPS.put(ModEntities.ZHUOMI_CHICKEN.get(), new Drop[]{d(rice, 1, 1), d(ModItems.LIEMI_CHICKEN, 1, 1), d(ModItems.ZHUOMI_FEATHER, 0, 2)});
        DROPS.put(ModEntities.MIMOO_COW.get(), new Drop[]{d(rice, 4, 5), d(ModItems.RAW_MIMOO_BEEF, 2, 3), d(ModItems.MIMOO_LEATHER, 2, 3)});
        DROPS.put(ModEntities.MIMI_SHEEP.get(), new Drop[]{d(rice, 2, 3), d(ModItems.RAW_MIMI_MUTTON, 2, 3), d(ModItems.MIMI_WOOL, 1, 2)});
        DROPS.put(ModEntities.MIHENG_PIG.get(), new Drop[]{d(rice, 4, 5), d(ModItems.RAW_MIHENG_PORK, 2, 3), d(ModItems.MIHENG_HIDE, 2, 3)});
        DROPS.put(ModEntities.TANMI_FISH.get(), new Drop[]{d(rice, 1, 1), d(ModItems.RAW_TANMI_FISH, 1, 1)});
        DROPS.put(ModEntities.PENMI_FISH.get(), new Drop[]{d(rice, 1, 1), d(ModItems.PENMI_FISH_SAC, 1, 1)});
        DROPS.put(ModEntities.RICE_GOLEM.get(), new Drop[]{d(rice, 10, 20)});
        DROPS.put(ModEntities.HAOMI_WOLF.get(), new Drop[]{d(rice, 5, 6), d(ModItems.HAOMI_WOLF_HEART, 0, 1)});
        DROPS.put(ModEntities.MIMI_SKELETON.get(), new Drop[]{d(rice, 6, 7), d(ModItems.RICE_BONE, 3, 4), d(ModItems.RICE_ARROW, 2, 3)});
        DROPS.put(ModEntities.HUMI_CORPSE.get(), new Drop[]{d(rice, 8, 9), d(ModItems.ROTTEN_RICE_FLESH, 1, 2), d(ModItems.CRYSTALLIZED_RICE, 0, 1)});
        DROPS.put(ModEntities.PAMI_CRAWLER.get(), new Drop[]{d(rice, 3, 4), d(ModItems.HUOMI_POWDER, 1, 2)});
        DROPS.put(ModEntities.RICE_WITCH.get(), new Drop[]{d(rice, 8, 9), d(ModItems.RICE_FRUIT_CLEANSING_SOLUTION, 0, 1), d(Items.GLOWSTONE_DUST, 0, 1)});
        DROPS.put(ModEntities.MOMI_ENTRANT.get(), new Drop[]{d(rice, 19, 20), d(ModItems.YANMI_PEARL, 0, 2)});
        DROPS.put(ModEntities.EXPLOSIVE_RICE_SPIRIT.get(), new Drop[]{d(ModItems.BAOLIEMI_ROD, 0, 2), d(rice, 15, 16)});
        DROPS.put(ModEntities.CARVED_RICE_SKELETON.get(), new Drop[]{d(ModItems.CARVED_RICE_BONE, 0, 2), d(rice, 15, 16)});
        DROPS.put(ModEntities.MILAI_HORSE.get(), new Drop[]{d(rice, 3, 4), d(ModItems.MILAIMA_BALL, 1, 2)});
        DROPS.put(ModEntities.STICKY_RICE_SPIDER.get(), new Drop[]{d(rice, 6, 7), d(ModItems.NIANMI_SILK_THREAD, 1, 2)});
        DROPS.put(ModEntities.MIHENG_PIG_CORPSE.get(), new Drop[]{d(rice, 8, 9), d(ModItems.RAW_MIHENG_PORK, 1, 2)});
        DROPS.put(ModEntities.RICE_STEALER.get(), new Drop[]{d(ModItems.RICE_STEALER_HEART, 0, 1), d(rice, 188, 189)});
        DROPS.put(ModEntities.RICE_SOUL.get(), new Drop[]{d(rice, 1, 2), d(ModItems.RICE_SOUL_TEAR, 0, 2)});
        // 界天米：第二条掉落为堕落之心
        DROPS.put(ModEntities.JIETIAN_RICE.get(), new Drop[]{d(ModItems.REMNANT_HEAVEN_WING, 1, 2), d(ModItems.FALLEN_HEART, 1, 1),
                d(ModItems.REMNANT_FALLEN_INGOT, 1, 1), d(ModItems.REALM_HEAVEN_BRAIN_FLOWER, 0, 1)});
        DROPS.put(ModEntities.CHAOTIC_VOID_TOUCHER.get(), new Drop[]{d(ModItems.VOID_TOUCH, 4, 4), d(ModItems.REMNANT_FALLEN_INGOT, 1, 1), d(ModItems.VOID_TOUCH_TENTACLE, 3, 4)});
        DROPS.put(ModEntities.BLIGHTED_THORN_FLOWER.get(), new Drop[]{d(ModItems.FADED_FLOWER_BRANCH, 5, 5), d(ModItems.MIMI_MEDLEY_LIQUID, 2, 2),
                d(ModItems.REMNANT_FALLEN_INGOT, 1, 1), d(ModItems.THORN_PETAL, 2, 3)});
        DROPS.put(ModEntities.FALLEN_RICE_PHOENIX.get(), new Drop[]{d(ModItems.CELESTIAL_PHOENIX_BEAK, 1, 1), d(ModItems.ADVANCED_SWORD_TEMPER_INGOT, 3, 5),
                d(ModItems.ADVANCED_ARMOR_CORE_INGOT, 3, 5), d(ModItems.ADVANCED_BOWSTRING_INGOT, 3, 5),
                d(ModItems.REMNANT_FALLEN_INGOT, 1, 1), d(ModItems.RICE_PHOENIX_MEAT_CHUNK, 1, 2)});
        DROPS.put(ModEntities.HUALING_RICE.get(), new Drop[]{d(ModItems.REMNANT_FALLEN_RICE_ESSENCE, 16, 16), d(ModItems.REMNANT_FALLEN_INGOT, 1, 1), d(ModItems.SPECIALTY_FALLEN_RICE, 10, 20)});
        DROPS.put(ModEntities.PALE_MERMAID.get(), new Drop[]{d(ModItems.MERMAID_TEAR, 3, 5), d(ModItems.SUPER_LAZY_ALLOY_INGOT, 1, 1),
                d(ModItems.REMNANT_FALLEN_INGOT, 1, 1), d(ModItems.PALE_MERMAID_TAIL, 1, 1),
                d(ModItems.DYED_MERMAID_GREASE, 1, 2)});
        DROPS.put(ModEntities.WANGMI_WOUNDED_MERMAID.get(), new Drop[]{d(ModItems.JIAOLONG_TALON, 1, 4), d(ModItems.FALLEN_CROWN, 1, 1),
                d(ModItems.REMNANT_FALLEN_INGOT, 1, 1), d(ModItems.WANGMI_WOUNDED_MERMAID_LEG, 1, 4)});
        DROPS.put(ModEntities.RICE_MIRAGE.get(), new Drop[]{d(ModItems.MIRAGE_BONE, 1, 1), d(ModItems.CLEANSING_RICE_SEPARATION_SOLUTION, 3, 3),
                d(ModItems.REMNANT_FALLEN_INGOT, 1, 1), d(ModItems.HALF_MIRAGE_MEAT_SLICE, 5, 6),
                d(ModItems.RICE_MIRAGE_GREASE, 5, 6)});
        DROPS.put(ModEntities.LAZY_ELF.get(), new Drop[]{d(ModItems.ELF_HAT, 1, 1), d(ModItems.COMPOUND_RICE_MEAL_INGOT, 3, 5),
                d(ModItems.REMNANT_FALLEN_INGOT, 1, 1), d(ModItems.FRAGRANT_POUCH, 0, 1), d(ModItems.FRAGRANT_POWDER, 16, 24)});
        DROPS.put(ModEntities.RICE_WOLF_VULTURE.get(), new Drop[]{d(ModItems.MOON_DEVOURER_WOLF_CLAW, 2, 2), d(Items.NETHER_STAR, 3, 3),
                d(ModItems.REMNANT_FALLEN_INGOT, 1, 1), d(ModItems.DEVOURER_VULTURE_WING, 1, 2)});
    }

    private static Drop d(ItemLike item, int min, int max) {
        return new Drop(item, min, max);
    }

    private static class EntityDrops implements LootTableSubProvider {
        @Override
        public void generate(BiConsumer<ResourceLocation, LootTable.Builder> consumer) {
            DROPS.forEach((entityType, drops) -> {
                LootTable.Builder table = LootTable.lootTable();
                for (Drop drop : drops) {
                    table.withPool(LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1.0F))
                            .add(LootItem.lootTableItem(drop.item())
                                    .apply(SetItemCountFunction.setCount(
                                            UniformGenerator.between(drop.min(), drop.max())))));
                }
                consumer.accept(entityType.getDefaultLootTable(), table);
            });
        }
    }
}
