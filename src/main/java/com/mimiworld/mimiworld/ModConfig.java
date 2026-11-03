package com.mimiworld.mimiworld;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig.Type;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 米米世界数值配置（COMMON，生成于 config/mimiworld-common.toml）。
 *
 * <p>包含五块可热改的数值：</p>
 * <ul>
 *   <li>{@code [mobs.*]}：每种自定义生物的生命/攻击/移速/护甲/韧性/击退抗性/索敌范围/飞行速度；</li>
 *   <li>{@code [armor.*]}：每套自定义防具的四个部位防御、耐久系数、韧性、击退抗性、附魔能力；</li>
 *   <li>{@code [weapons.*]}：每个自定义工具/武器材料等级的耐久、挖掘速度、攻击加成、附魔能力；</li>
 *   <li>{@code [combat]}：全局伤害倍率（本模组生物造成/承受的伤害）；</li>
 *   <li>{@code [drops]}：本模组生物死亡时的掉落物数量倍率；</li>
 *   <li>{@code [messages]}：界面提示开关（如猩红竖眼传送结果提示）；</li>
 *   <li>{@code [slots]}：玩家各 Curios 栏位的数量（-1 表示沿用数据包默认）。</li>
 * </ul>
 *
 * <p>怪物数值在实体加入世界（{@code EntityJoinLevelEvent}）时写入基础属性，
 * 因此在游戏中重新载入存档或重新生成该生物后生效；配置本身在启动时读取。</p>
 */
public final class ModConfig {

    public static final Common COMMON;
    public static final ForgeConfigSpec SPEC;

    static {
        Pair<Common, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(Common::new);
        COMMON = pair.getLeft();
        SPEC = pair.getRight();
    }

    private ModConfig() {
    }

    /** 在 mod 构造阶段注册配置文件；COMMON 类型保证客户端与服务端读到同一份数值。 */
    public static void register() {
        ModLoadingContext.get().registerConfig(Type.COMMON, SPEC, "mimiworld-common.toml");
    }

    // ===================== 对外读取入口 =====================

    public static ArmorStats armor(String id) {
        return COMMON.armors.get(id);
    }

    public static WeaponStats weapon(String id) {
        return COMMON.weapons.get(id);
    }

    public static double mobDamageMultiplier() {
        return read(COMMON.mobDamageMultiplier);
    }

    public static double mobDamageTakenMultiplier() {
        return read(COMMON.mobDamageTakenMultiplier);
    }

    public static double lootMultiplier() {
        return read(COMMON.lootMultiplier);
    }

    /** 是否在快捷栏上方显示猩红竖眼的传送结果提示。 */
    public static boolean crimsonEyeTeleportHint() {
        return read(COMMON.crimsonEyeTeleportHint);
    }

    /** 玩家栏位数量覆盖值；-1 表示沿用数据包默认，0 表示隐藏该栏位。 */
    public static int slotSize(String identifier) {
        ForgeConfigSpec.IntValue value = COMMON.slots.get(identifier);
        return value == null ? -1 : read(value);
    }

    /**
     * 读取配置项；配置尚未加载完成时退回默认值。
     * （开发环境下 Forge 在未加载时 {@code get()} 会抛异常，生产环境则直接返回默认值。）
     */
    public static <T> T read(ForgeConfigSpec.ConfigValue<T> value) {
        try {
            return value.get();
        } catch (IllegalStateException | NullPointerException e) {
            return value.getDefault();
        }
    }

    /** 把配置里的怪物数值写入实体基础属性（只在服务端调用）。 */
    public static void applyMobStats(Mob mob) {
        if (mob.level().isClientSide()) {
            return;
        }
        var key = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
        if (key == null || !MimiWorld.MODID.equals(key.getNamespace())) {
            return;
        }
        MobStats stats = COMMON.mobs.get(key.getPath());
        if (stats == null) {
            return;
        }

        // 生命上限单独处理：先改上限；刚生成（含生物蛋）的实体原本是满血，需要跟着新上限回满，
        // 从存档载入的实体保留当前血量，只在超过新上限时压回去
        double maxHealth = read(stats.maxHealth);
        AttributeInstance health = mob.getAttribute(Attributes.MAX_HEALTH);
        if (health != null) {
            float oldHealth = mob.getHealth();
            double oldMaxHealth = health.getValue();
            health.setBaseValue(maxHealth);
            if (oldHealth > maxHealth) {
                mob.setHealth((float) maxHealth);
            } else if (oldHealth >= oldMaxHealth - 1.0E-4D) {
                // 生成时血量为旧上限（即满血），按新上限回满
                mob.setHealth((float) maxHealth);
            }
        }

        setBase(mob, Attributes.ATTACK_DAMAGE, read(stats.attackDamage));
        setBase(mob, Attributes.MOVEMENT_SPEED, read(stats.movementSpeed));
        setBase(mob, Attributes.ARMOR, read(stats.armor));
        setBase(mob, Attributes.ARMOR_TOUGHNESS, read(stats.armorToughness));
        setBase(mob, Attributes.KNOCKBACK_RESISTANCE, read(stats.knockbackResistance));
        setBase(mob, Attributes.FOLLOW_RANGE, read(stats.followRange));

        // 飞行速度仅对真正具备该属性的飞行生物生效，0 表示不修改
        double flyingSpeed = read(stats.flyingSpeed);
        if (flyingSpeed > 0.0D) {
            setBase(mob, Attributes.FLYING_SPEED, flyingSpeed);
        }
    }

    private static void setBase(Mob mob, Attribute attribute, double value) {
        AttributeInstance instance = mob.getAttribute(attribute);
        if (instance != null) {
            instance.setBaseValue(value);
        }
    }

    // ===================== 配置项结构 =====================

    /** 单个生物的八项数值。 */
    public record MobStats(
            ForgeConfigSpec.DoubleValue maxHealth,
            ForgeConfigSpec.DoubleValue attackDamage,
            ForgeConfigSpec.DoubleValue movementSpeed,
            ForgeConfigSpec.DoubleValue armor,
            ForgeConfigSpec.DoubleValue armorToughness,
            ForgeConfigSpec.DoubleValue knockbackResistance,
            ForgeConfigSpec.DoubleValue followRange,
            ForgeConfigSpec.DoubleValue flyingSpeed) {
    }

    /**
     * 单套防具的数值。
     *
     * <p>{@code defense} 顺序固定为脚、腿、胸、头，对应 {@code ArmorItem.Type} 的槽位索引。</p>
     */
    public record ArmorStats(
            List<ForgeConfigSpec.IntValue> defense,
            ForgeConfigSpec.IntValue durabilityMultiplier,
            ForgeConfigSpec.IntValue enchantmentValue,
            ForgeConfigSpec.DoubleValue toughness,
            ForgeConfigSpec.DoubleValue knockbackResistance) {
    }

    /** 单个工具/武器材料等级的数值。 */
    public record WeaponStats(
            ForgeConfigSpec.IntValue uses,
            ForgeConfigSpec.DoubleValue miningSpeed,
            ForgeConfigSpec.DoubleValue attackDamageBonus,
            ForgeConfigSpec.IntValue enchantmentValue) {
    }

    public static final class Common {

        public final Map<String, MobStats> mobs;
        public final Map<String, ArmorStats> armors;
        public final Map<String, WeaponStats> weapons;
        public final ForgeConfigSpec.DoubleValue mobDamageMultiplier;
        public final ForgeConfigSpec.DoubleValue mobDamageTakenMultiplier;
        public final ForgeConfigSpec.DoubleValue lootMultiplier;
        public final ForgeConfigSpec.BooleanValue crimsonEyeTeleportHint;
        public final Map<String, ForgeConfigSpec.IntValue> slots;

        Common(ForgeConfigSpec.Builder builder) {
            Map<String, MobStats> mobMap = new LinkedHashMap<>();
            Map<String, ArmorStats> armorMap = new LinkedHashMap<>();
            Map<String, WeaponStats> weaponMap = new LinkedHashMap<>();

            // ---------- 生物数值 ----------
            builder.comment("自定义生物的数值；改动在生物下次加入世界时生效。").push("mobs");

            mob(builder, mobMap, "mimi_monkey_king", "米米猴王",
                    300, 9, 0.30, 8, 0, 0.5, 32, 0);
            mob(builder, mobMap, "rice_crystal_tiger_king", "饭晶虎王",
                    500, 12, 0.32, 10, 1.0, 0.6, 36, 0);
            mob(builder, mobMap, "super_crystal_cow_king", "超晶牛王",
                    700, 16, 0.30, 12, 1.5, 0.8, 40, 0);
            mob(builder, mobMap, "heavy_crystal_rabbit_king", "重晶兔王",
                    700, 18, 0.34, 12, 1.5, 0.7, 40, 0);
            mob(builder, mobMap, "compound_azure_python", "复晶天青蟒",
                    1200, 20, 0.33, 14, 2.0, 0.7, 40, 0);
            mob(builder, mobMap, "cleansing_longevity_kui", "清洁长命夔",
                    1500, 24, 0.30, 16, 2.5, 0.8, 40, 0);
            mob(builder, mobMap, "laziest_final_dragon", "最懒终末龙",
                    2500, 28, 0.30, 18, 3.0, 1.0, 64, 4.5);
            mob(builder, mobMap, "dibazongzuilong", "第八宗罪龙",
                    3000, 32, 0.30, 20, 3.5, 1.0, 64, 4.5);

            mob(builder, mobMap, "rice_stealer", "盗米者",
                    50, 6, 0.30, 0, 0, 0, 20, 0);
            mob(builder, mobMap, "sticky_rice_spider", "黏米之蛛",
                    40, 3, 0.30, 0, 0, 0, 16, 0);
            mob(builder, mobMap, "mimi_skeleton", "米米骷髅",
                    45, 4, 0.25, 0, 0, 0, 16, 0);
            mob(builder, mobMap, "humi_corpse", "糊米古尸",
                    50, 4, 0.23, 0, 0, 0, 16, 0);
            mob(builder, mobMap, "pami_crawler", "爪巴米者",
                    24, 2, 0.25, 0, 0, 0, 16, 0);
            mob(builder, mobMap, "rice_witch", "米巫",
                    50, 5, 0.25, 0, 0, 0, 16, 0);
            mob(builder, mobMap, "momi_entrant", "没米入",
                    80, 8, 0.30, 0, 0, 0, 64, 0);
            mob(builder, mobMap, "explosive_rice_spirit", "爆烈米灵",
                    80, 6, 0.23, 0, 0, 0, 48, 1.0);
            mob(builder, mobMap, "carved_rice_skeleton", "雕米骷髅",
                    70, 6, 0.25, 0, 0, 0, 16, 0);
            mob(builder, mobMap, "milai_horse", "米莱马",
                    32, 4, 0.28, 0, 0, 0, 16, 0);
            mob(builder, mobMap, "miheng_pig_corpse", "米哼猪尸",
                    50, 5, 0.23, 0, 0, 0, 16, 0);
            mob(builder, mobMap, "rice_soul", "米魂",
                    28, 3, 0.25, 0, 0, 0, 32, 0);

            mob(builder, mobMap, "jietian_rice", "界天米",
                    220, 12, 0.25, 0, 0, 0.5, 40, 1.0);
            mob(builder, mobMap, "chaotic_void_toucher", "混沌虚无触",
                    700, 16, 0.0, 0, 0, 1.0, 36, 0);
            mob(builder, mobMap, "blighted_thorn_flower", "污棘花",
                    400, 14, 0.0, 0, 0, 1.0, 32, 0);
            mob(builder, mobMap, "fallen_rice_phoenix", "堕落米凤",
                    700, 20, 0.30, 0, 0, 1.0, 40, 1.5);
            mob(builder, mobMap, "hualing_rice", "化灵米",
                    1200, 14, 0.25, 0, 0, 0, 40, 1.2);
            mob(builder, mobMap, "pale_mermaid", "浅染鲛",
                    120, 10, 1.20, 0, 0, 0.5, 32, 0);
            mob(builder, mobMap, "wangmi_wounded_mermaid", "望米伤鲛",
                    800, 24, 1.00, 0, 0, 0.8, 48, 0);
            mob(builder, mobMap, "rice_mirage", "米蜃",
                    500, 22, 0.35, 0, 0, 0, 32, 0);
            mob(builder, mobMap, "lazy_elf", "懒精灵",
                    400, 16, 0.30, 0, 0, 0, 32, 0);
            mob(builder, mobMap, "rice_wolf_vulture", "哮噬米狼鹫",
                    600, 22, 0.32, 0, 0, 1.0, 40, 0);

            mob(builder, mobMap, "mimi_sheep", "米羊羊",
                    16, 0, 0.23, 0, 0, 0, 16, 0);
            mob(builder, mobMap, "zhuomi_chicken", "啄米鸡",
                    8, 0, 0.25, 0, 0, 0, 16, 0);
            mob(builder, mobMap, "mimoo_cow", "米哞牛",
                    20, 0, 0.20, 0, 0, 0, 16, 0);
            mob(builder, mobMap, "miheng_pig", "米哼猪",
                    20, 0, 0.25, 0, 0, 0, 16, 0);
            mob(builder, mobMap, "tanmi_fish", "贪米鱼",
                    8, 0, 1.00, 0, 0, 0, 16, 0);
            mob(builder, mobMap, "penmi_fish", "喷米鱼",
                    8, 0, 1.00, 0, 0, 0, 16, 0);
            mob(builder, mobMap, "rice_golem", "米傀儡",
                    200, 0, 0.25, 0, 0, 1.0, 16, 0);
            mob(builder, mobMap, "haomi_wolf", "嗥米狼",
                    40, 0, 0.30, 0, 0, 0, 16, 0);

            builder.pop();
            this.mobs = Collections.unmodifiableMap(mobMap);

            // ---------- 防具数值 ----------
            builder.comment("自定义防具每套的数值；防御顺序为脚/腿/胸/头。耐久系数与物品注册时读取，改动后需重启游戏。")
                    .push("armor");
            for (ModArmorMaterials material : ModArmorMaterials.values()) {
                String id = material.id();
                builder.comment(armorName(id) + "（mimiworld:" + id + "）").push(id);
                List<ForgeConfigSpec.IntValue> defense = new ArrayList<>(4);
                String[] slotNames = {"boots_defense", "leggings_defense", "chestplate_defense", "helmet_defense"};
                String[] slotComments = {"靴子防御", "护腿防御", "胸甲防御", "头盔防御"};
                for (int i = 0; i < 4; i++) {
                    defense.add(builder.comment(slotComments[i])
                            .defineInRange(slotNames[i], material.rawDefense(i), 0, 1000));
                }
                ForgeConfigSpec.IntValue durability = builder
                        .comment("耐久系数（各部位耐久 = 原版基准 × 该系数）")
                        .defineInRange("durability_multiplier", material.rawDurabilityMultiplier(), 1, 1000);
                ForgeConfigSpec.IntValue enchantment = builder
                        .comment("附魔能力")
                        .defineInRange("enchantment_value", material.rawEnchantmentValue(), 0, 100);
                ForgeConfigSpec.DoubleValue toughness = builder
                        .comment("护甲韧性")
                        .defineInRange("toughness", material.rawToughness(), 0.0D, 1000.0D);
                ForgeConfigSpec.DoubleValue knockback = builder
                        .comment("击退抗性（0~1）")
                        .defineInRange("knockback_resistance", material.rawKnockbackResistance(), 0.0D, 1.0D);
                builder.pop();
                armorMap.put(id, new ArmorStats(defense, durability, enchantment, toughness, knockback));
            }
            builder.pop();
            this.armors = Collections.unmodifiableMap(armorMap);

            // ---------- 武器/工具数值 ----------
            builder.comment("自定义工具与武器材料等级的数值（同等级的所有剑、锤、镐共用）。").push("weapons");
            for (ModItemTiers tier : ModItemTiers.values()) {
                String id = tier.name().toLowerCase(Locale.ROOT);
                builder.comment(weaponName(id) + "（" + id + "）").push(id);
                ForgeConfigSpec.IntValue uses = builder
                        .comment("耐久值（剑、锤、镐等工具的耐久上限）")
                        .defineInRange("durability", tier.rawUses(), 1, 1000000);
                ForgeConfigSpec.DoubleValue speed = builder
                        .comment("挖掘速度")
                        .defineInRange("mining_speed", tier.rawSpeed(), 0.0D, 1000.0D);
                ForgeConfigSpec.DoubleValue attack = builder
                        .comment("攻击力加成（叠加在武器自身基础伤害之上）")
                        .defineInRange("attack_damage_bonus", tier.rawAttackDamageBonus(), 0.0D, 10000.0D);
                ForgeConfigSpec.IntValue enchantment = builder
                        .comment("附魔能力")
                        .defineInRange("enchantment_value", tier.rawEnchantmentValue(), 0, 100);
                builder.pop();
                weaponMap.put(id, new WeaponStats(uses, speed, attack, enchantment));
            }
            builder.pop();
            this.weapons = Collections.unmodifiableMap(weaponMap);

            // ---------- 全局战斗 ==========
            builder.comment("全局战斗倍率，只影响本模组生物。").push("combat");
            this.mobDamageMultiplier = builder
                    .comment("本模组生物造成伤害的倍率（近战、技能、弹射物均适用）")
                    .defineInRange("mob_damage_multiplier", 1.0D, 0.0D, 1000.0D);
            this.mobDamageTakenMultiplier = builder
                    .comment("本模组生物受到伤害的倍率（越大越脆）")
                    .defineInRange("mob_damage_taken_multiplier", 1.0D, 0.0D, 1000.0D);
            builder.pop();

            // ---------- 掉落 ----------
            builder.comment("本模组生物死亡掉落物数量倍率（0 表示不掉落）。").push("drops");
            this.lootMultiplier = builder
                    .comment("掉落数量倍率，最终数量不会超过物品堆叠上限")
                    .defineInRange("loot_multiplier", 1.0D, 0.0D, 64.0D);
            builder.pop();

            // ---------- 界面提示 ----------
            builder.comment("界面提示开关（快捷栏上方的操作反馈）。").push("messages");
            this.crimsonEyeTeleportHint = builder
                    .comment("猩红竖眼：按「自定义」键传送后，是否在快捷栏上方显示成功或失败原因")
                    .define("crimson_eye_teleport_hint", false);
            builder.pop();

            // ---------- 玩家栏位数量 ----------
            builder.comment("玩家各 Curios 栏位的数量。-1 = 沿用数据包默认，0 = 隐藏该栏位，≥1 = 覆盖为该数量。"
                    + "改动后重新进入世界（或重生）生效；缩小栏位时超出的饰品会退回背包。")
                    .push("slots");
            Map<String, ForgeConfigSpec.IntValue> slotMap = new LinkedHashMap<>();
            slot(builder, slotMap, "sacred_relic", "圣遗物", 3);
            slot(builder, slotMap, "shenyi_book", "神遗之书", 1);
            slot(builder, slotMap, "ring", "戒指", -1);
            slot(builder, slotMap, "necklace", "项链", -1);
            slot(builder, slotMap, "charm", "护符", -1);
            slot(builder, slotMap, "belt", "腰带", -1);
            slot(builder, slotMap, "back", "背部", -1);
            slot(builder, slotMap, "body", "身体", -1);
            slot(builder, slotMap, "bracelet", "手镯", -1);
            slot(builder, slotMap, "hands", "手部", -1);
            slot(builder, slotMap, "head", "头部饰品", -1);
            slot(builder, slotMap, "curio", "通用饰品", -1);
            builder.pop();
            this.slots = Collections.unmodifiableMap(slotMap);
        }

        /** 定义一个栏位的数量配置。 */
        private static void slot(ForgeConfigSpec.Builder builder, Map<String, ForgeConfigSpec.IntValue> out,
                                 String id, String name, int defaultSize) {
            out.put(id, builder.comment(name + "（curios:" + id + "）")
                    .defineInRange(id, defaultSize, -1, 64));
        }

        /** 定义一个生物的全部八项数值。 */
        private static void mob(ForgeConfigSpec.Builder builder, Map<String, MobStats> out,
                                String id, String name,
                                double maxHealth, double attackDamage, double movementSpeed,
                                double armor, double armorToughness, double knockbackResistance,
                                double followRange, double flyingSpeed) {
            builder.comment(name + "（mimiworld:" + id + "）").push(id);
            MobStats stats = new MobStats(
                    builder.comment("最大生命值").defineInRange("max_health", maxHealth, 1.0E-4D, 1.0E7D),
                    builder.comment("近战攻击力（Boss 的实际伤害还会叠加其常驻力量效果）")
                            .defineInRange("attack_damage", attackDamage, 0.0D, 1.0E7D),
                    builder.comment("移动速度").defineInRange("movement_speed", movementSpeed, 0.0D, 100.0D),
                    builder.comment("护甲值").defineInRange("armor", armor, 0.0D, 10000.0D),
                    builder.comment("护甲韧性").defineInRange("armor_toughness", armorToughness, 0.0D, 10000.0D),
                    builder.comment("击退抗性（0~1）").defineInRange("knockback_resistance", knockbackResistance, 0.0D, 1.0D),
                    builder.comment("索敌范围（格）").defineInRange("follow_range", followRange, 0.0D, 512.0D),
                    builder.comment("飞行速度（0 = 不修改，仅飞行生物有效）")
                            .defineInRange("flying_speed", flyingSpeed, 0.0D, 100.0D));
            builder.pop();
            out.put(id, stats);
        }

        public static String armorName(String id) {
            return switch (id) {
                case "rice_crystal_alloy" -> "饭晶合金套";
                case "super_heavy_crystal" -> "超重结晶套";
                case "compound_rice_meal" -> "复合米饭套";
                case "cleansing_compound" -> "清洁复合套";
                case "super_lazy_alloy" -> "超懒合金套";
                case "mimi_wings" -> "米米神翼";
                case "super_lazy_wings" -> "超懒神翼";
                case "heavenfall" -> "最懒天堕套";
                case "remnant_fallen_cardinal_sin" -> "残堕宗罪套";
                default -> id;
            };
        }

        public static String weaponName(String id) {
            return switch (id) {
                case "rice_crystal" -> "饭晶级";
                case "super_crystal" -> "超结晶级";
                case "rice_crystal_alloy" -> "饭晶合金级";
                case "super_heavy_crystal" -> "超重结晶级";
                case "compound_rice_meal" -> "复合米饭级";
                case "cleansing_compound" -> "清洁复合级";
                case "super_lazy_alloy" -> "超懒合金级";
                case "laziest_heavenfall" -> "最懒天堕级";
                case "remnant_fallen" -> "残堕级";
                default -> id;
            };
        }
    }
}
