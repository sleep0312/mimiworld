package com.mimiworld.mimiworld.client;

import com.mimiworld.mimiworld.ModConfig;
import com.mojang.logging.LogUtils;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.DoubleFieldBuilder;
import me.shedaniel.clothconfig2.impl.builders.IntFieldBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.ForgeConfigSpec;
import org.slf4j.Logger;

import java.util.function.Consumer;

/**
 * 米米世界数值配置界面（Cloth Config）。
 *
 * <p>直接读取 {@link ModConfig} 的 ForgeConfigSpec 项构建界面，保存时写回同一个 spec 并落到
 * config/mimiworld-common.toml。该界面属于可选的客户端集成：未安装 cloth_config 时不会加载本类。</p>
 *
 * <p>生效规则与 TOML 一致：生物数值在实体下次加入世界时生效；防具/武器需重启游戏；
 * 全局战斗与掉落倍率保存后立即生效。</p>
 */
public final class ClothConfigScreen {

    private static final Logger LOGGER = LogUtils.getLogger();

    private ClothConfigScreen() {
    }

    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.literal("米米世界数值配置"))
                .setSavingRunnable(ClothConfigScreen::save);
        ConfigEntryBuilder entries = builder.entryBuilder();

        buildMobCategory(builder, entries);
        buildArmorCategory(builder, entries);
        buildWeaponCategory(builder, entries);
        buildGlobalCategory(builder, entries);

        return builder.build();
    }

    private static void save() {
        try {
            ModConfig.SPEC.save();
        } catch (RuntimeException exception) {
            // 配置尚未加载（例如在标题界面打开）时 Forge 会抛异常，此时数值只停留在内存
            LOGGER.warn("Failed to save mimiworld config", exception);
        }
    }

    /** [mobs.*]：每个生物一个可折叠子分类，内含八项数值。 */
    private static void buildMobCategory(ConfigBuilder builder, ConfigEntryBuilder entries) {
        ConfigCategory category = builder.getOrCreateCategory(Component.literal("生物数值"));
        category.addEntry(entries.startTextDescription(
                Component.literal("改动在生物下次加入世界时生效；已生成的生物不会立即变化。")).build());

        ModConfig.COMMON.mobs.forEach((id, stats) -> {
            SubCategoryBuilder sub = entries.startSubCategory(
                            Component.translatable("entity.mimiworld." + id))
                    .setExpanded(false);
            Consumer<AbstractConfigListEntry> sink = sub::add;
            addDouble(sink, entries, "最大生命值", stats.maxHealth(), 1.0E-4D, 1.0E7D, false);
            addDouble(sink, entries, "近战攻击力", stats.attackDamage(), 0.0D, 1.0E7D, false);
            addDouble(sink, entries, "移动速度", stats.movementSpeed(), 0.0D, 100.0D, false);
            addDouble(sink, entries, "护甲值", stats.armor(), 0.0D, 10000.0D, false);
            addDouble(sink, entries, "护甲韧性", stats.armorToughness(), 0.0D, 10000.0D, false);
            addDouble(sink, entries, "击退抗性", stats.knockbackResistance(), 0.0D, 1.0D, false);
            addDouble(sink, entries, "索敌范围", stats.followRange(), 0.0D, 512.0D, false);
            addDouble(sink, entries, "飞行速度（0 = 不修改）", stats.flyingSpeed(), 0.0D, 100.0D, false);
            category.addEntry(sub.build());
        });
    }

    /** [armor.*]：每套防具一个子分类；防御项顺序为脚/腿/胸/头。 */
    private static void buildArmorCategory(ConfigBuilder builder, ConfigEntryBuilder entries) {
        ConfigCategory category = builder.getOrCreateCategory(Component.literal("防具数值"));
        category.addEntry(entries.startTextDescription(
                Component.literal("防具数值在物品注册时读取，改动后需重启游戏（游戏中会标注 *）。")).build());

        ModConfig.COMMON.armors.forEach((id, stats) -> {
            SubCategoryBuilder sub = entries.startSubCategory(
                            Component.literal(ModConfig.Common.armorName(id)))
                    .setExpanded(false);
            Consumer<AbstractConfigListEntry> sink = sub::add;
            String[] slotNames = {"靴子防御", "护腿防御", "胸甲防御", "头盔防御"};
            for (int slot = 0; slot < slotNames.length; slot++) {
                addInt(sink, entries, slotNames[slot], stats.defense().get(slot), 0, 1000, true);
            }
            addInt(sink, entries, "耐久系数", stats.durabilityMultiplier(), 1, 1000, true);
            addInt(sink, entries, "附魔能力", stats.enchantmentValue(), 0, 100, true);
            addDouble(sink, entries, "护甲韧性", stats.toughness(), 0.0D, 1000.0D, true);
            addDouble(sink, entries, "击退抗性", stats.knockbackResistance(), 0.0D, 1.0D, true);
            category.addEntry(sub.build());
        });
    }

    /** [weapons.*]：每个工具/武器材料等级一个子分类。 */
    private static void buildWeaponCategory(ConfigBuilder builder, ConfigEntryBuilder entries) {
        ConfigCategory category = builder.getOrCreateCategory(Component.literal("武器数值"));
        category.addEntry(entries.startTextDescription(
                Component.literal("同等级的所有剑/锤/镐共用一套数值，改动后需重启游戏（游戏中会标注 *）。")).build());

        ModConfig.COMMON.weapons.forEach((id, stats) -> {
            SubCategoryBuilder sub = entries.startSubCategory(
                            Component.literal(ModConfig.Common.weaponName(id)))
                    .setExpanded(false);
            Consumer<AbstractConfigListEntry> sink = sub::add;
            addInt(sink, entries, "耐久值", stats.uses(), 1, 1000000, true);
            addDouble(sink, entries, "挖掘速度", stats.miningSpeed(), 0.0D, 1000.0D, true);
            addDouble(sink, entries, "攻击力加成", stats.attackDamageBonus(), 0.0D, 10000.0D, true);
            addInt(sink, entries, "附魔能力", stats.enchantmentValue(), 0, 100, true);
            category.addEntry(sub.build());
        });
    }

    /** [combat] / [drops]：全局倍率，保存后立即生效。 */
    private static void buildGlobalCategory(ConfigBuilder builder, ConfigEntryBuilder entries) {
        ConfigCategory category = builder.getOrCreateCategory(Component.literal("全局倍率"));
        category.addEntry(entries.startTextDescription(
                Component.literal("只作用于本模组生物，保存后立即生效。")).build());

        Consumer<AbstractConfigListEntry> sink = category::addEntry;
        addDouble(sink, entries, "生物造成伤害倍率", ModConfig.COMMON.mobDamageMultiplier, 0.0D, 1000.0D, false);
        addDouble(sink, entries, "生物受到伤害倍率", ModConfig.COMMON.mobDamageTakenMultiplier, 0.0D, 1000.0D, false);
        addDouble(sink, entries, "掉落数量倍率", ModConfig.COMMON.lootMultiplier, 0.0D, 64.0D, false);
    }

    private static void addDouble(Consumer<AbstractConfigListEntry> sink, ConfigEntryBuilder entries,
                                  String label, ForgeConfigSpec.DoubleValue value,
                                  double min, double max, boolean requireRestart) {
        DoubleFieldBuilder field = entries.startDoubleField(Component.literal(label), ModConfig.read(value))
                .setDefaultValue(value.getDefault())
                .setMin(min)
                .setMax(max)
                .setSaveConsumer(value::set);
        if (requireRestart) {
            field.requireRestart();
        }
        sink.accept(field.build());
    }

    private static void addInt(Consumer<AbstractConfigListEntry> sink, ConfigEntryBuilder entries,
                               String label, ForgeConfigSpec.IntValue value,
                               int min, int max, boolean requireRestart) {
        IntFieldBuilder field = entries.startIntField(Component.literal(label), ModConfig.read(value))
                .setDefaultValue(value.getDefault())
                .setMin(min)
                .setMax(max)
                .setSaveConsumer(value::set);
        if (requireRestart) {
            field.requireRestart();
        }
        sink.accept(field.build());
    }
}
