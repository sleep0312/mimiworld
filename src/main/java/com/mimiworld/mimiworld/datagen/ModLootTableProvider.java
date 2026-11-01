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

    /** 下界合金碎片：0~2 概率掉落（0 表示本次不掉，1~2 为实际掉落数量）。 */
    private static final Drop NETHERITE_SCRAP = d(Items.NETHERITE_SCRAP, 0, 2);

    static {
        var rice = ModItems.RICE.get();
        DROPS.put(ModEntities.ZHUOMI_CHICKEN.get(), new Drop[]{d(rice, 1, 1), d(ModItems.ZHUOMI_CHICKEN.get(), 1, 1), d(ModItems.ZHUOMI_FEATHER.get(), 0, 2)});
        DROPS.put(ModEntities.MIMOO_COW.get(), new Drop[]{d(rice, 4, 5), d(ModItems.RAW_MIMOO_BEEF.get(), 2, 3), d(ModItems.MIMOO_LEATHER.get(), 2, 3)});
        DROPS.put(ModEntities.MIMI_SHEEP.get(), new Drop[]{d(rice, 2, 3), d(ModItems.RAW_MIMI_MUTTON.get(), 2, 3), d(ModItems.MIMI_WOOL.get(), 1, 2)});
        DROPS.put(ModEntities.MIHENG_PIG.get(), new Drop[]{d(rice, 4, 5), d(ModItems.RAW_MIHENG_PORK.get(), 2, 3), d(ModItems.MIHENG_HIDE.get(), 2, 3)});
        DROPS.put(ModEntities.TANMI_FISH.get(), new Drop[]{d(rice, 1, 1), d(ModItems.RAW_TANMI_FISH.get(), 1, 1)});
        DROPS.put(ModEntities.PENMI_FISH.get(), new Drop[]{d(rice, 1, 1), d(ModItems.PENMI_FISH_SAC.get(), 1, 1)});
        DROPS.put(ModEntities.RICE_GOLEM.get(), new Drop[]{d(rice, 10, 20)});
        DROPS.put(ModEntities.HAOMI_WOLF.get(), new Drop[]{d(rice, 5, 6), d(ModItems.HAOMI_WOLF_HEART.get(), 0, 1)});
        DROPS.put(ModEntities.MIMI_SKELETON.get(), new Drop[]{d(rice, 6, 7), d(ModItems.RICE_BONE.get(), 3, 4), d(ModItems.RICE_ARROW.get(), 2, 3), NETHERITE_SCRAP});
        DROPS.put(ModEntities.HUMI_CORPSE.get(), new Drop[]{d(rice, 8, 9), d(ModItems.ROTTEN_RICE_FLESH.get(), 1, 2), d(ModItems.CRYSTALLIZED_RICE.get(), 0, 1), NETHERITE_SCRAP});
        DROPS.put(ModEntities.PAMI_CRAWLER.get(), new Drop[]{d(rice, 3, 4), d(ModItems.HUOMI_POWDER.get(), 1, 2), NETHERITE_SCRAP});
        DROPS.put(ModEntities.RICE_WITCH.get(), new Drop[]{d(rice, 8, 9), d(ModItems.RICE_FRUIT_CLEANSING_SOLUTION.get(), 0, 1), d(Items.GLOWSTONE_DUST, 0, 1), NETHERITE_SCRAP});
        DROPS.put(ModEntities.MOMI_ENTRANT.get(), new Drop[]{d(rice, 19, 20), d(ModItems.YANMI_PEARL.get(), 0, 2), NETHERITE_SCRAP});
        DROPS.put(ModEntities.EXPLOSIVE_RICE_SPIRIT.get(), new Drop[]{d(ModItems.BAOLIEMI_ROD.get(), 0, 2), d(rice, 15, 16), NETHERITE_SCRAP});
        DROPS.put(ModEntities.CARVED_RICE_SKELETON.get(), new Drop[]{d(ModItems.CARVED_RICE_BONE.get(), 0, 2), d(rice, 15, 16), NETHERITE_SCRAP});
        DROPS.put(ModEntities.MILAI_HORSE.get(), new Drop[]{d(rice, 3, 4), d(ModItems.MILAIMA_BALL.get(), 1, 2), NETHERITE_SCRAP});
        DROPS.put(ModEntities.STICKY_RICE_SPIDER.get(), new Drop[]{d(rice, 6, 7), d(ModItems.NIANMI_SILK_THREAD.get(), 1, 2), NETHERITE_SCRAP});
        DROPS.put(ModEntities.MIHENG_PIG_CORPSE.get(), new Drop[]{d(rice, 8, 9), d(ModItems.RAW_MIHENG_PORK.get(), 1, 2), NETHERITE_SCRAP});
        DROPS.put(ModEntities.RICE_STEALER.get(), new Drop[]{d(ModItems.RICE_STEALER_HEART.get(), 0, 1), d(rice, 188, 189), NETHERITE_SCRAP});
        DROPS.put(ModEntities.RICE_SOUL.get(), new Drop[]{d(rice, 1, 2), d(ModItems.RICE_SOUL_TEAR.get(), 0, 2), NETHERITE_SCRAP});
        // 界天米：第二条掉落为堕落之心
        DROPS.put(ModEntities.JIETIAN_RICE.get(), new Drop[]{d(ModItems.REMNANT_HEAVEN_WING.get(), 1, 2), d(ModItems.FALLEN_HEART.get(), 1, 1),
                d(ModItems.REMNANT_FALLEN_INGOT.get(), 1, 1), d(ModItems.REALM_HEAVEN_BRAIN_FLOWER.get(), 0, 1)});
        DROPS.put(ModEntities.CHAOTIC_VOID_TOUCHER.get(), new Drop[]{d(ModItems.VOID_TOUCH.get(), 4, 4), d(ModItems.REMNANT_FALLEN_INGOT.get(), 1, 1), d(ModItems.VOID_TOUCH_TENTACLE.get(), 3, 4)});
        DROPS.put(ModEntities.BLIGHTED_THORN_FLOWER.get(), new Drop[]{d(ModItems.FADED_FLOWER_BRANCH.get(), 5, 5), d(ModItems.MIMI_MEDLEY_LIQUID.get(), 2, 2),
                d(ModItems.REMNANT_FALLEN_INGOT.get(), 1, 1), d(ModItems.THORN_PETAL.get(), 2, 3)});
        DROPS.put(ModEntities.FALLEN_RICE_PHOENIX.get(), new Drop[]{d(ModItems.CELESTIAL_PHOENIX_BEAK.get(), 1, 1), d(ModItems.ADVANCED_SWORD_TEMPER_INGOT.get(), 3, 5),
                d(ModItems.ADVANCED_ARMOR_CORE_INGOT.get(), 3, 5), d(ModItems.ADVANCED_BOWSTRING_INGOT.get(), 3, 5),
                d(ModItems.REMNANT_FALLEN_INGOT.get(), 1, 1), d(ModItems.RICE_PHOENIX_MEAT_CHUNK.get(), 1, 2)});
        DROPS.put(ModEntities.HUALING_RICE.get(), new Drop[]{d(ModItems.REMNANT_FALLEN_RICE_ESSENCE.get(), 16, 16), d(ModItems.REMNANT_FALLEN_INGOT.get(), 1, 1), d(ModItems.SPECIALTY_FALLEN_RICE.get(), 10, 20)});
        DROPS.put(ModEntities.PALE_MERMAID.get(), new Drop[]{d(ModItems.MERMAID_TEAR.get(), 3, 5), d(ModItems.SUPER_LAZY_ALLOY_INGOT.get(), 1, 1),
                d(ModItems.REMNANT_FALLEN_INGOT.get(), 1, 1), d(ModItems.PALE_MERMAID_TAIL.get(), 1, 1),
                d(ModItems.DYED_MERMAID_GREASE.get(), 1, 2)});
        DROPS.put(ModEntities.WANGMI_WOUNDED_MERMAID.get(), new Drop[]{d(ModItems.JIAOLONG_TALON.get(), 1, 4), d(ModItems.FALLEN_CROWN.get(), 1, 1),
                d(ModItems.REMNANT_FALLEN_INGOT.get(), 1, 1), d(ModItems.WANGMI_WOUNDED_MERMAID_LEG.get(), 1, 4)});
        DROPS.put(ModEntities.RICE_MIRAGE.get(), new Drop[]{d(ModItems.MIRAGE_BONE.get(), 1, 1), d(ModItems.CLEANSING_RICE_SEPARATION_SOLUTION.get(), 3, 3),
                d(ModItems.REMNANT_FALLEN_INGOT.get(), 1, 1), d(ModItems.HALF_MIRAGE_MEAT_SLICE.get(), 5, 6),
                d(ModItems.RICE_MIRAGE_GREASE.get(), 5, 6)});
        DROPS.put(ModEntities.LAZY_ELF.get(), new Drop[]{d(ModItems.ELF_HAT.get(), 1, 1), d(ModItems.COMPOUND_RICE_MEAL_INGOT.get(), 3, 5),
                d(ModItems.REMNANT_FALLEN_INGOT.get(), 1, 1), d(ModItems.FRAGRANT_POUCH.get(), 0, 1), d(ModItems.FRAGRANT_POWDER.get(), 16, 24)});
        DROPS.put(ModEntities.RICE_WOLF_VULTURE.get(), new Drop[]{d(ModItems.MOON_DEVOURER_WOLF_CLAW.get(), 2, 2), d(Items.NETHER_STAR, 3, 3),
                d(ModItems.REMNANT_FALLEN_INGOT.get(), 1, 1), d(ModItems.DEVOURER_VULTURE_WING.get(), 1, 2)});
        // 米米猴王：必掉猴王如意棍×1、米米猴王头×1，外加米 16~32
        DROPS.put(ModEntities.MIMI_MONKEY_KING.get(), new Drop[]{d(ModItems.MONKEY_KING_RUYI_STAFF.get(), 1, 1),
                d(ModItems.MIMI_MONKEY_HEAD.get(), 1, 1), d(rice, 16, 32)});
        // 饭晶虎王：必掉饭晶×12~16、虎王利爪×1、饭晶虎王头×1
        DROPS.put(ModEntities.RICE_CRYSTAL_TIGER_KING.get(), new Drop[]{d(ModItems.CRYSTALLIZED_RICE_MEAL.get(), 12, 16),
                d(ModItems.TIGER_KING_RAZOR_CLAW.get(), 1, 1), d(ModItems.RICE_CRYSTAL_TIGER_KING_HEAD.get(), 1, 1)});
        // 超晶牛王：必掉饭晶合金锭×4~5、牛王晶角×1、超晶牛王头×1
        DROPS.put(ModEntities.SUPER_CRYSTAL_COW_KING.get(), new Drop[]{d(ModItems.RICE_CRYSTAL_ALLOY_INGOT.get(), 4, 5),
                d(ModItems.OX_KING_CRYSTAL_HORN.get(), 1, 1), d(ModItems.SUPER_CRYSTAL_OX_HEAD.get(), 1, 1)});
        // 重晶兔王：必掉超结晶×1~2、兔王腥红眼×1、重晶兔王头×1
        DROPS.put(ModEntities.HEAVY_CRYSTAL_RABBIT_KING.get(), new Drop[]{d(ModItems.SUPER_CRYSTAL.get(), 1, 2),
                d(ModItems.RABBIT_KING_BLOODY_RED_EYE.get(), 1, 1), d(ModItems.HEAVY_CRYSTAL_RABBIT_KING_HEAD.get(), 1, 1)});
        // 复晶天青蟒：必掉超重结晶×1~2、极毒青蟒牙×1、复晶天青蟒头×1
        DROPS.put(ModEntities.COMPOUND_AZURE_PYTHON.get(), new Drop[]{d(ModItems.SUPER_HEAVY_CRYSTAL.get(), 1, 2),
                d(ModItems.VENOM_AZURE_PYTHON_FANG.get(), 1, 1), d(ModItems.COMPOUND_CRYSTAL_AZURE_PYTHON_HEAD.get(), 1, 1)});
        // 清洁长命夔：必掉精制强化米网×2~3、恶夔长命角×1、清洁长命夔头×1，粗制复合米饭锭 0~2 概率掉落
        DROPS.put(ModEntities.CLEANSING_LONGEVITY_KUI.get(), new Drop[]{d(ModItems.COARSE_COMPOUND_RICE_MEAL_INGOT.get(), 0, 2),
                d(ModItems.REFINED_REINFORCED_RICE_NET.get(), 2, 3), d(ModItems.EVIL_KUI_LONGEVITY_HORN.get(), 1, 1),
                d(ModItems.PURIFIED_LONGEVITY_KUI_HEAD.get(), 1, 1)});
        // 最懒终末龙：必掉精制强化米网×4~5、终末龙逆鳞×1、最懒终末龙头×1，粗制清洁复合锭 0~2 概率掉落
        DROPS.put(ModEntities.LAZIEST_FINAL_DRAGON.get(), new Drop[]{d(ModItems.COARSE_CLEANSING_COMPOUND_INGOT.get(), 0, 2),
                d(ModItems.REFINED_REINFORCED_RICE_NET.get(), 4, 5), d(ModItems.END_DRAGON_REVERSE_SCALE.get(), 1, 1),
                d(ModItems.LAZIEST_END_DRAGON_HEAD.get(), 1, 1)});
        // 第八宗罪龙：最终 Boss，掉落沿用最懒终末龙并追加残堕精华锭 1~3，头颅换为第八宗罪龙头
        DROPS.put(ModEntities.DIBAZONGZUILONG.get(), new Drop[]{d(ModItems.COARSE_CLEANSING_COMPOUND_INGOT.get(), 0, 2),
                d(ModItems.REFINED_REINFORCED_RICE_NET.get(), 4, 5), d(ModItems.END_DRAGON_REVERSE_SCALE.get(), 1, 1),
                d(ModItems.DIBAZONGZUI_HEAD.get(), 1, 1), d(ModItems.REMNANT_FALLEN_WORLD_ESSENCE_INGOT.get(), 1, 3)});
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
