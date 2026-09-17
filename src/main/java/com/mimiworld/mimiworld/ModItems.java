package com.mimiworld.mimiworld;

import com.mimiworld.mimiworld.block.ModBlocks;
import com.mimiworld.mimiworld.entity.ModEntities;
import com.mimiworld.mimiworld.item.RainbowNameItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.SwordItem;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 米米世界物品注册类。所有物品类新内容统一在此注册。
 * 注册的物品会被 ModCreativeTabs 按命名空间自动收录进「米米世界」创造标签页。
 *
 * ================== 物品编码备忘录（仅代码层可见，游戏内不显示） ==================
 * 每个物品按【注册顺序】自动获得从 1 开始的连续编号，配方等代码用 byCode(n) 引用，
 * 不需要写物品注册名。编号由 registerItem() 内的计数器自动分配，严禁手工指定。
 *
 * 铁律：新物品只能【追加在本类注册区末尾】，禁止插入到已有物品之间或调整顺序，
 * 否则历史编号会整体错位，已写好的配方引用将全部错乱。
 *
 * 编码表（每新增一个物品，在此补一行注释）：
 *   1 = rice（米）
 *   2 = cooked_rice（熟米）
 *   3 = rice_ball（米团，可投掷）
 *   4 = cooked_rice_ball（熟米团，可投掷）
 *   5 = rice_log（米原木，方块物品 BlockItem；方块本体见 block.ModBlocks，不另编号）
 *   6 = rice_planks（米木板，方块物品 BlockItem；方块本体见 block.ModBlocks，不另编号）
 *   7 = rice_stick（米木棍，可燃烧 300t = 1.5 个物品）
 *   8 = rice_leaves（米树叶，方块物品 BlockItem；方块本体见 block.ModBlocks，不另编号）
 *   9 = rice_bucket（米桶，堆叠 1，BucketItem 绑定米汤流体 mimiworld:rice_soup，见 fluid.ModFluids/RiceBucketItem）
 *   10 = rice_sapling（米树苗，方块物品 BlockItem；骨粉催熟长成米树，方块本体见 block.ModBlocks）
 *   11 = hardened_rice（硬化米，方块物品 BlockItem；石头类方块，铁镐挖掘等级2，方块本体见 block.ModBlocks）
 *   12 = deepslate_hardened_rice（深层硬化米，方块物品 BlockItem；石头类方块，铁镐挖掘等级2，方块本体见 block.ModBlocks）
 *   13 = rice_bedrock（基米，方块物品 BlockItem；基岩类方块，生存不可破坏，方块本体见 block.ModBlocks）
 *   14 = rice_grass（米草，方块物品 BlockItem，自定义 RiceGrassItem；可放置的草类植物，剪刀采集；可食用，方块本体见 block.ModBlocks）
 *   15 = rice_fruit（米果，类似原版苹果的食物；漂浮I/反胃I/虚弱I，食用2s）
 *   16 = crystallized_rice（结晶米，类似原版锭的矿石材料）
 *   17 = crystallized_rice_ore（结晶米矿石，方块物品 BlockItem；下界合金镐挖掘等级4，掉经验3-7，方块本体见 block.ModBlocks）
 *   18 = recrystallized_rice_ore（重结晶米矿石，方块物品 BlockItem；深板岩质地，下界合金镐，掉经验3-7，烧炼得19号；原名 deepslate_crystallized_rice_ore 已按中文名整体改名，方块本体见 block.ModBlocks）
 *   19 = recrystallized_rice（重结晶米，精炼锭类材料；由18号重结晶米矿石烧炼得到）
 *   20 = combustible_rice（可燃米，普通材料；熔炉燃料 6400t = 可烧 32 个物品，见 event.ModFuelEvents）
 *   21 = combustible_rice_ore（可燃米矿石，方块物品 BlockItem；石头类矿石，下界合金镐，掉经验3-7，烧炼得20号，方块本体见 block.ModBlocks）
 *   22 = mud_rice（泥米，方块物品 BlockItem；泥巴质地，铲挖掘等级2=铁铲，方块本体见 block.ModBlocks）
 *   23 = reinforced_rice_stick（强化米棍，普通材料物品，最大堆叠 64）
 *   24 = rice_baton（米棍，普通材料物品，最大堆叠 64；英文名 Rice Baton，避开 7 号米木棍已占用的 Rice Stick）
 *   25 = rice_flour（米粉，普通材料物品，最大堆叠 64；英文名 Rice Flour）
 *   26 = coarse_rice_block（粗米块，方块物品 BlockItem；石头类方块，镐挖掘等级3=钻石镐，方块本体见 block.ModBlocks）
 *   27 = rice_maker（造米机，方块物品 BlockItem；石头类方块，镐挖掘等级3=钻石镐，方块本体见 block.ModBlocks）
 *   28 = rice_block（米块，方块物品 BlockItem；石头类方块，镐挖掘等级3=钻石镐，方块本体见 block.ModBlocks）
 *   29 = rice_pounding_table（锤米台，方块物品 BlockItem；石头类方块，镐挖掘等级3=钻石镐，方块本体见 block.ModBlocks）
 *   30 = rice_pail（饭桶，自定义桶物品 RicePailItem extends BucketItem，最大堆叠 1；绑定饭汤 rice_broth 流体，行为同原版水桶；英文名 Rice Pail，避开 9 号 rice_bucket）
 *   31 = rice_press（压饭机，方块物品 BlockItem；石头类方块，镐挖掘等级2=铁镐，方块本体见 block.ModBlocks；英文名 Rice Press）
 *   32 = compressed_rice_pail（压缩饭桶，普通物品，最大堆叠 64；英文名 Compressed Rice Pail）
 *   33 = coarse_crystallized_rice_meal（粗制结晶饭，普通物品，最大堆叠 64；英文名 Coarse Crystallized Rice Meal）
 *   34 = crystallized_rice_meal（结晶饭，普通物品，最大堆叠 64；英文名 Crystallized Rice Meal，区别于 16 号结晶米 crystallized_rice）
 *   35 = rice_crystal_alloy_ingot（饭晶合金锭，普通物品，最大堆叠 64；英文名 Rice Crystal Alloy Ingot）
 *   36 = rice_crystal_hammer（饭晶锤，自定义工具类物品 RiceCrystalHammerItem，最大堆叠 1；镐类挖掘、钻石级 tier，英文名 Rice Crystal Hammer）
 *   37 = wet_grass_fiber（湿草麻，普通物品，最大堆叠 64；英文名 Wet Grass Fiber）
 *   38 = coarse_rice_fiber（粗制米麻，普通物品，最大堆叠 64；英文名 Coarse Rice Fiber）
 *   39 = reinforced_wet_grass_fiber（强化湿草麻，普通物品，最大堆叠 64；英文名 Reinforced Wet Grass Fiber）
 *   40 = refined_rice_fiber（精制米麻，普通物品，最大堆叠 64；英文名 Refined Rice Fiber）
 *   41 = wet_grass_net（湿草网，普通物品，最大堆叠 64；英文名 Wet Grass Net）
 *   42 = coarse_wet_grass_net（粗制湿草网，普通物品，最大堆叠 64；英文名 Coarse Wet Grass Net）
 *   43 = coarse_rice_net（粗制米网，普通物品，最大堆叠 64；英文名 Coarse Rice Net）
 *   44 = reinforced_wet_grass_net（强化湿草网，普通物品，最大堆叠 64；英文名 Reinforced Wet Grass Net）
 *   45 = refined_reinforced_wet_grass_net（精制强化湿草网，普通物品，最大堆叠 64；英文名 Refined Reinforced Wet Grass Net）
 *   46 = refined_reinforced_rice_net（精制强化米网，普通物品，最大堆叠 64；英文名 Refined Reinforced Rice Net）
 *   47 = rice_crystal_alloy_hammer（饭晶合金锤，自定义工具类物品 RiceCrystalAlloyHammerItem，最大堆叠 1；镐类挖掘、下界合金级 tier，36号饭晶锤强化版，英文名 Rice Crystal Alloy Hammer）
 *   48 = rice_crystal_bow（饭晶弓，工具类物品 BowItem，最大堆叠 1，耐久 384，饭晶等级见 ModItemTiers；英文名 Rice Crystal Bow）
 *   49 = advanced_rice_pounding_table（高级锤米台，方块物品 BlockItem；石头类方块，镐挖掘等级2=铁镐，破坏掉本身，方块本体见 block.ModBlocks；英文名 Advanced Rice Pounding Table）
 *   50 = coarse_super_crystal（粗超结晶，普通物品，最大堆叠 64；英文名 Coarse Super Crystal）
 *   51 = super_crystal（超结晶，普通物品，最大堆叠 64；英文名 Super Crystal）
 *   52 = rice_ore（米矿石，方块物品 BlockItem；铁矿石类方块，镐挖掘等级3=钻石镐，破坏掉本身并掉3~7经验，方块本体见 block.ModBlocks；英文名 Rice Ore）
 *   53 = rice_stone（米石，普通物品，最大堆叠 64；嵌有米纹的小石块，英文名 Rice Stone）
 *   54 = super_crystal_block（超结晶块，方块物品 BlockItem；晶块类方块，镐挖掘等级2=铁镐，破坏掉本身，方块本体见 block.ModBlocks；英文名 Block of Super Crystal）
 *   55 = super_heavy_crystal（超重结晶，普通物品，最大堆叠 64；英文名 Super Heavy Crystal）
 *   56 = ionization_table（电离台，方块物品 BlockItem；金属机器方块，镐挖掘等级3=钻石镐，破坏掉本身，方块本体见 block.ModBlocks；英文名 Ionization Table）
 *   57 = precipitation_table（沉淀台，方块物品 BlockItem；金属机器方块，镐挖掘等级3=钻石镐，破坏掉本身，方块本体见 block.ModBlocks；英文名 Precipitation Table）
 *   58 = coarse_compound_rice_meal_ingot（粗制复合米饭锭，普通物品，最大堆叠 64；英文名 Coarse Compound Rice Meal Ingot）
 *   59 = compound_rice_meal_plate（复合米饭板，普通物品，最大堆叠 64；英文名 Compound Rice Meal Plate）
 *   60 = compound_rice_meal_ingot（复合米饭锭，普通物品，最大堆叠 64；英文名 Compound Rice Meal Ingot）
 *   61 = rice_pomace（米果渣，普通物品，最大堆叠 64；英文名 Rice Pomace）
 *   62 = rice_fruit_juice（米果汁，普通物品非流体、药水瓶造型，最大堆叠 64；英文名 Rice Fruit Juice）
 *   63 = rice_fruit_mixture（米果混合液，普通物品非流体、药水瓶造型，最大堆叠 64；英文名 Rice Fruit Mixture）
 *   64 = dissociation_table（离解台，方块物品 BlockItem；金属机器方块，镐挖掘等级2=铁镐，破坏掉本身，方块本体见 block.ModBlocks；英文名 Dissociation Table）
 *   65 = rice_book（米书，普通物品，最大堆叠 64；英文名 Rice Book）
 *   66 = rice_paper（米纸，普通物品，最大堆叠 64；英文名 Rice Paper）
 *   67 = rice_fruit_analyzing_solution（米果解析液，普通物品非流体、药水瓶造型，最大堆叠 64；英文名 Rice Fruit Analyzing Solution）
 *   68 = rice_fruit_compound_solution（米果复合液，普通物品非流体、药水瓶造型，最大堆叠 64；英文名 Rice Fruit Compound Solution）
 *   69 = rice_meal_mixture（米饭混合液，普通物品非流体、药水瓶造型，最大堆叠 64；英文名 Rice Meal Mixture）
 *   70 = rice_fruit_mixed_pomace（米果混合渣，普通物品，最大堆叠 64；英文名 Rice Fruit Mixed Pomace）
 *   71 = rice_fruit_cleansing_solution（米果清洁液，普通物品非流体、药水瓶造型，最大堆叠 64；英文名 Rice Fruit Cleansing Solution）
 *   72 = coarse_cleansing_compound_ingot（粗制清洁复合锭，普通物品，最大堆叠 64；英文名 Coarse Cleansing Compound Ingot）
 *   73 = cleansing_compound_chunk（清洁复合碎块，普通物品，最大堆叠 64；英文名 Cleansing Compound Chunk）
 *   74 = cleansing_compound_plate（清洁复合板，普通物品，最大堆叠 64；英文名 Cleansing Compound Plate）
 *   75 = cleansing_compound_ingot（清洁复合锭，普通物品，最大堆叠 64；英文名 Cleansing Compound Ingot）
 *   76 = coarse_super_lazy_alloy_ingot（粗制超懒合金锭，普通物品，最大堆叠 64；英文名 Coarse Super Lazy Alloy Ingot）
 *   77 = super_lazy_alloy_ionic_form（超懒合金离子态，普通物品非流体、药水瓶造型，最大堆叠 64；英文名 Super Lazy Alloy Ionic Form）
 *   78 = super_lazy_alloy_dust（超懒合金粉，粉末状普通物品，最大堆叠 64；英文名 Super Lazy Alloy Dust）
 *   79 = super_lazy_alloy_chunk（超懒合金碎块，普通物品，最大堆叠 64；英文名 Super Lazy Alloy Chunk）
 *   80 = super_lazy_alloy_ingot（超懒合金锭，普通物品，最大堆叠 64；英文名 Super Lazy Alloy Ingot）
 *   81 = rice_stone_block（米石块，方块物品 BlockItem；石头类方块，镐挖掘等级2=铁镐，破坏掉本身，方块本体见 block.ModBlocks；英文名 Block of Rice Stone）
 *   82 = combustible_mixture（可燃混合液，普通物品非流体、药水瓶造型，最大堆叠 64；英文名 Combustible Mixture）
 *   83 = combustible_solid（可燃固态物，普通物品，最大堆叠 64；英文名 Combustible Solid）
 *   84 = cleansing_rice_separation_solution（清洁米沉离液，普通物品非流体、药水瓶造型，最大堆叠 64；英文名 Cleansing Rice Separation Solution）
 *   85 = rice_crystal_sword（饭晶剑，工具类 SwordItem，最大堆叠 1，ModItemTiers.RICE_CRYSTAL；英文名 Rice Crystal Sword）
 *   86 = super_crystal_sword（超结晶剑，工具类 SwordItem，最大堆叠 1，SUPER_CRYSTAL；英文名 Super Crystal Sword）
 *   87 = super_crystal_bow（超结晶弓，BowItem，最大堆叠 1，耐久 640；英文名 Super Crystal Bow）
 *   88 = rice_crystal_alloy_sword（饭晶合金剑，SwordItem，最大堆叠 1，RICE_CRYSTAL_ALLOY；英文名 Rice Crystal Alloy Sword）
 *   89 = rice_crystal_alloy_bow（饭晶合金弓，BowItem，最大堆叠 1，耐久 896；英文名 Rice Crystal Alloy Bow）
 *   90 = rice_crystal_alloy_helmet（饭晶合金头盔，ArmorItem，最大堆叠 1；英文名 Rice Crystal Alloy Helmet）
 *   91 = rice_crystal_alloy_chestplate（饭晶合金胸甲，ArmorItem，最大堆叠 1；英文名 Rice Crystal Alloy Chestplate）
 *   92 = rice_crystal_alloy_leggings（饭晶合金护腿，ArmorItem，最大堆叠 1；英文名 Rice Crystal Alloy Leggings）
 *   93 = rice_crystal_alloy_boots（饭晶合金靴子，ArmorItem，最大堆叠 1；英文名 Rice Crystal Alloy Boots）
 *   94 = super_heavy_crystal_sword（超重结晶剑，SwordItem，最大堆叠 1，SUPER_HEAVY_CRYSTAL；英文名 Super Heavy Crystal Sword）
 *   95 = super_heavy_crystal_bow（超重结晶弓，BowItem，最大堆叠 1，耐久 1024；英文名 Super Heavy Crystal Bow）
 *   96 = sword_temper_ingot（磨剑锭，普通物品，最大堆叠 64；英文名 Sword Temper Ingot）
 *   97 = bowstring_ingot（弓弦锭，普通物品，最大堆叠 64；英文名 Bowstring Ingot）
 *   98 = armor_core_ingot（含甲锭，普通物品，最大堆叠 64；英文名 Armor Core Ingot）
 *   99 = advanced_sword_temper_ingot（高级磨剑锭，普通物品，最大堆叠 64；英文名 Advanced Sword Temper Ingot）
 *   100 = advanced_bowstring_ingot（高级弓弦锭，普通物品，最大堆叠 64；英文名 Advanced Bowstring Ingot）
 *   101 = advanced_armor_core_ingot（高级含甲锭，普通物品，最大堆叠 64；英文名 Advanced Armor Core Ingot）
 *   102 = super_heavy_crystal_helmet（超重结晶头盔，ArmorItem，最大堆叠 1；英文名 Super Heavy Crystal Helmet）
 *   103 = super_heavy_crystal_chestplate（超重结晶胸甲，ArmorItem，最大堆叠 1；英文名 Super Heavy Crystal Chestplate）
 *   104 = super_heavy_crystal_leggings（超重结晶护腿，ArmorItem，最大堆叠 1；英文名 Super Heavy Crystal Leggings）
 *   105 = super_heavy_crystal_boots（超重结晶靴子，ArmorItem，最大堆叠 1；英文名 Super Heavy Crystal Boots）
 *   106 = compound_rice_meal_sword（复合米饭剑，SwordItem，最大堆叠 1，COMPOUND_RICE_MEAL；英文名 Compound Rice Meal Sword）
 *   107 = compound_rice_meal_bow（复合米饭弓，BowItem，最大堆叠 1，耐久 1280；英文名 Compound Rice Meal Bow）
 *   108 = compound_rice_meal_helmet（复合米饭头盔，ArmorItem，最大堆叠 1；英文名 Compound Rice Meal Helmet）
 *   109 = compound_rice_meal_chestplate（复合米饭胸甲，ArmorItem，最大堆叠 1；英文名 Compound Rice Meal Chestplate）
 *   110 = compound_rice_meal_leggings（复合米饭护腿，ArmorItem，最大堆叠 1；英文名 Compound Rice Meal Leggings）
 *   111 = compound_rice_meal_boots（复合米饭靴子，ArmorItem，最大堆叠 1；英文名 Compound Rice Meal Boots）
 *   112 = cleansing_compound_sword_blank（清洁复合剑胚，普通物品，最大堆叠 64；英文名 Cleansing Compound Sword Blank）
 *   113 = cleansing_compound_bow_blank（清洁复合弓胚，普通物品，最大堆叠 64；英文名 Cleansing Compound Bow Blank）
 *   114 = cleansing_compound_sword（清洁复合剑，SwordItem，最大堆叠 1，CLEANSING_COMPOUND；英文名 Cleansing Compound Sword）
 *   115 = cleansing_compound_bow（清洁复合弓，BowItem，最大堆叠 1，耐久 1536；英文名 Cleansing Compound Bow）
 *   116 = cleansing_compound_armor_blank（清洁复合甲胚，普通物品，最大堆叠 64；英文名 Cleansing Compound Armor Blank）
 *   117 = cleansing_compound_helmet（清洁复合头盔，ArmorItem，最大堆叠 1；英文名 Cleansing Compound Helmet）
 *   118 = cleansing_compound_chestplate（清洁复合胸甲，ArmorItem，最大堆叠 1；英文名 Cleansing Compound Chestplate）
 *   119 = cleansing_compound_leggings（清洁复合护腿，ArmorItem，最大堆叠 1；英文名 Cleansing Compound Leggings）
 *   120 = cleansing_compound_boots（清洁复合靴子，ArmorItem，最大堆叠 1；英文名 Cleansing Compound Boots）
 *   121 = super_lazy_alloy_sword_blank（超懒合金剑胚，普通物品，最大堆叠 64；英文名 Super Lazy Alloy Sword Blank）
 *   122 = super_lazy_alloy_bow_blank（超懒合金弓胚，普通物品，最大堆叠 64；英文名 Super Lazy Alloy Bow Blank）
 *   123 = super_lazy_alloy_armor_blank（超懒合金甲胚，普通物品，最大堆叠 64；英文名 Super Lazy Alloy Armor Blank）
 *   124 = super_lazy_alloy_sword（超懒合金剑，SwordItem，最大堆叠 1，SUPER_LAZY_ALLOY；英文名 Super Lazy Alloy Sword）
 *   125 = super_lazy_alloy_bow（超懒合金弓，BowItem，最大堆叠 1，耐久 2048；英文名 Super Lazy Alloy Bow）
 *   126 = super_lazy_alloy_helmet（超懒合金头盔，ArmorItem，最大堆叠 1；英文名 Super Lazy Alloy Helmet）
 *   127 = super_lazy_alloy_chestplate（超懒合金胸甲，ArmorItem，最大堆叠 1；英文名 Super Lazy Alloy Chestplate）
 *   128 = super_lazy_alloy_leggings（超懒合金护腿，ArmorItem，最大堆叠 1；英文名 Super Lazy Alloy Leggings）
 *   129 = super_lazy_alloy_boots（超懒合金靴子，ArmorItem，最大堆叠 1；英文名 Super Lazy Alloy Boots）
 *   130 = sword_forging_table（铸剑台，方块物品 BlockItem；金属机器方块，镐挖掘等级3=钻石镐，破坏掉落本身，方块本体见 block.ModBlocks；英文名 Sword Forging Table）
 *   131 = armor_forging_table（铸甲台，方块物品 BlockItem；金属机器方块，镐挖掘等级3=钻石镐，破坏掉落本身，方块本体见 block.ModBlocks；英文名 Armor Forging Table）
 *   132 = bow_forging_table（铸弓台，方块物品 BlockItem；金属机器方块，镐挖掘等级3=钻石镐，破坏掉落本身，方块本体见 block.ModBlocks；英文名 Bow Forging Table）
 *   133 = mimi_eye（米米之眼，普通物品，最大堆叠 64；英文名 Eye of Mimi）
 *   134 = mimi_teleporter（米米传送器，普通物品，最大堆叠 64；英文名 Mimi Teleporter）
 *   135 = small_summoner（小型召唤器，普通物品，最大堆叠 64；英文名 Small Summoner）
 *   136 = placeholder_136（待定占位，不进创造标签页；英文名 Reserved Slot 136）
 *   137 = medium_summoner（中型召唤器，普通物品，最大堆叠 64；英文名 Medium Summoner）
 *   138 = large_summoner（大型召唤器，普通物品，最大堆叠 64；英文名 Large Summoner）
 *   139 = laziest_summoner（最懒召唤器，普通物品，最大堆叠 64；英文名 Laziest Summoner）
 *   140 = summon_core_blank（召唤核心胚，普通物品，最大堆叠 64；英文名 Summon Core Blank）
 *   141 = coarse_summon_core_blank（粗制召唤核心胚，普通物品，最大堆叠 64；英文名 Coarse Summon Core Blank）
 *   142 = summon_core（召唤核心，普通物品，最大堆叠 64；英文名 Summon Core）
 *   143 = advanced_summon_core_blank（高级召唤核心胚，普通物品，最大堆叠 64；英文名 Advanced Summon Core Blank）
 *   144 = coarse_advanced_summon_core_blank（粗制高级召唤核心胚，普通物品，最大堆叠 64；英文名 Coarse Advanced Summon Core Blank）
 *   145 = advanced_summon_core（高级召唤核心，普通物品，最大堆叠 64；英文名 Advanced Summon Core）
 *   146 = mixed_crystal（混合结晶，普通物品，最大堆叠 64；英文名 Mixed Crystal）
 *   147 = molten_mixed_crystal（熔融混合结晶，普通物品，最大堆叠 64；英文名 Molten Mixed Crystal）
 *   148 = laziest_summon_core_blank（最懒召唤核心胚，普通物品，最大堆叠 64；英文名 Laziest Summon Core Blank）
 *   149 = coarse_laziest_summon_core_blank（粗制最懒召唤核心胚，普通物品，最大堆叠 64；英文名 Coarse Laziest Summon Core Blank）
 *   150 = laziest_summon_core（手稿写"最懒召唤核心胚"与148重名，按胚/粗胚/核心三级链修正为【最懒召唤核心】，普通物品，最大堆叠 64；英文名 Laziest Summon Core）
 *   151 = mimi_monkey_king_spawn_egg（米米猴王生物蛋，ForgeSpawnEggItem，绑定实体 mimi_monkey_king；英文名 Mimi Monkey King Spawn Egg）
 *   152 = rice_crystal_tiger_king_spawn_egg（饭晶虎王生物蛋；英文名 Rice Crystal Tiger King Spawn Egg）
 *   153 = super_crystal_cow_king_spawn_egg（超晶牛王生物蛋；英文名 Super Crystal Cow King Spawn Egg）
 *   154 = heavy_crystal_rabbit_king_spawn_egg（重晶兔王生物蛋；英文名 Heavy Crystal Rabbit King Spawn Egg）
 *   155 = compound_azure_python_spawn_egg（复晶天青蟒生物蛋；英文名 Compound Azure Python Spawn Egg）
 *   156 = cleansing_longevity_kui_spawn_egg（清洁长命夔生物蛋；英文名 Cleansing Longevity Kui Spawn Egg）
 *   157 = laziest_final_dragon_spawn_egg（最懒终末龙生物蛋；英文名 Laziest Final Dragon Spawn Egg）
 *   158 = placeholder_158（待定占位，不进创造标签页；英文名 Reserved Slot 158）
 *   159 = mimi_divine_wings（米米神翼，ArmorItem 胸甲，最大堆叠 1，MIMI_WINGS；英文名 Mimi Divine Wings）
 *   160 = mimi_phantom_membrane（米米幻翼膜，普通物品，最大堆叠 64；英文名 Mimi Phantom Membrane）
 *   161 = super_lazy_wings_chestplate（超懒神翼胸甲，ArmorItem 胸甲，最大堆叠 1，SUPER_LAZY_WINGS；英文名 Super Lazy Divine Wings Chestplate）
 *   162 = ancient_sword_intent（上古剑意，普通物品，最大堆叠 64；英文名 Ancient Sword Intent）
 *   163 = super_lazy_divine_sword_broken_1（超懒神剑·残缺·壹，普通物品【非装备】，最大堆叠 1，RainbowNameItem 动态彩虹渐变名称；英文名 Super Lazy Divine Sword - Broken I）
 *   164 = sword_quenching_table（淬剑台，方块物品 BlockItem；金属机器方块，镐挖掘等级2=铁镐，破坏掉落本身，方块本体见 block.ModBlocks；英文名 Sword Quenching Table）
 *   165 = super_lazy_divine_sword_broken_2（超懒神剑·残缺·贰，普通物品【非装备】，最大堆叠 1，RainbowNameItem 动态彩虹渐变名称；英文名 Super Lazy Divine Sword - Broken II）
 *   166 = bloodletting_table（汲血台，方块物品 BlockItem；金属机器方块，镐挖掘等级2=铁镐，破坏掉落本身，方块本体见 block.ModBlocks；英文名 Bloodletting Table）
 *   167 = blood_vial（血瓶，普通物品，最大堆叠 64；英文名 Blood Vial）
 *   168 = sword_quenching_liquid（淬剑液，普通物品，最大堆叠 64；英文名 Sword Quenching Liquid）
 *   169 = ancient_sword_soul（远古剑魄，普通物品，最大堆叠 64；英文名 Ancient Sword Soul）
 *   170 = super_lazy_divine_sword_defective（超懒神剑·残次，普通物品【非装备】，最大堆叠 1，RainbowNameItem 动态彩虹渐变名称；英文名 Super Lazy Divine Sword - Defective）
 *   171 = primordial_sword_soul（太古剑魄，普通物品，最大堆叠 64；英文名 Primordial Sword Soul）
 *   172 = super_lazy_divine_sword_counterfeit（超懒神剑·赝品，普通物品【非装备】，最大堆叠 1，RainbowNameItem；英文名 Super Lazy Divine Sword - Counterfeit）
 *   173 = divine_jade（神玉，普通物品，最大堆叠 64；英文名 Divine Jade）
 *   174 = golden_rice_jade（金米玉，普通物品，最大堆叠 64；英文名 Golden Rice Jade）
 *   175 = divine_sword_appraisal_jade（神剑鉴玉，普通物品，最大堆叠 64；英文名 Divine Sword Appraisal Jade）
 *   176 = super_lazy_divine_sword_flawed（超懒神剑·瑕疵，普通物品【非装备】，最大堆叠 1，RainbowNameItem；英文名 Super Lazy Divine Sword - Flawed）
 *   177 = forbidden_sword_manual（剑法禁卷，普通物品，最大堆叠 64；英文名 Forbidden Sword Manual）
 *   178 = super_lazy_sword_art（超懒剑法，普通物品【非装备】，最大堆叠 1，RainbowNameItem；英文名 Super Lazy Sword Art）
 *   179 = super_lazy_divine_sword_complete（超懒神剑·完整，普通物品【非装备】，最大堆叠 1，RainbowNameItem；英文名 Super Lazy Divine Sword - Complete）
 *   180 = crystallized_rice_jade（结晶米玉，普通物品，最大堆叠 64；英文名 Crystallized Rice Jade）
 *   181 = final_sword_codex（终末剑诀，普通物品【非装备】，最大堆叠 1，RainbowNameItem；英文名 Final Sword Codex）
 *   182 = laziest_heavenfall_sword_counterfeit（最懒天堕剑·赝品，普通物品【非装备】，最大堆叠 1，RainbowNameItem；英文名 Laziest Heavenfall Sword - Counterfeit）
 *   183 = ruined_heart（破败之心，普通物品，最大堆叠 64；英文名 Ruined Heart）
 *   184 = fallen_heart（堕落之心，普通物品，最大堆叠 64；英文名 Fallen Heart；同时是 HEAVENFALL 盔甲修复材料）
 *   185 = fallen_crown（堕落王冠，普通物品，最大堆叠 64；英文名 Fallen Crown）
 *   186 = seven_deadly_falls（七宗堕落，普通物品，最大堆叠 64；英文名 Seven Deadly Falls）
 *   187 = laziest_heavenfall_sword_original_sin（最懒天堕剑·原罪，普通物品【非装备】，最大堆叠 1，RainbowNameItem；英文名 Laziest Heavenfall Sword - Original Sin）
 *   188 = heavenfall_wings_chestplate（天堕翼胸甲，ArmorItem 胸甲，最大堆叠 1，HEAVENFALL；英文名 Heavenfall Wing Chestplate）
 *   189 = laziest_heavenfall_helmet（最懒天堕头盔，ArmorItem 头盔，最大堆叠 1，HEAVENFALL；英文名 Laziest Heavenfall Helmet）
 *   190 = laziest_heavenfall_leggings（最懒天堕护腿，ArmorItem 护腿，最大堆叠 1，HEAVENFALL；英文名 Laziest Heavenfall Leggings）
 *   191 = laziest_heavenfall_boots（最懒天堕靴子，ArmorItem 靴子，最大堆叠 1，HEAVENFALL；英文名 Laziest Heavenfall Boots）
 *   192 = cooked_liemi_chicken（熟咧米鸡肉，食物，最大堆叠 64，2.5饱食度；1%中毒10s/50%反胃5s，食用1.5s；英文名 Cooked Liemi Chicken）
 *   193 = raw_mimoo_beef（米吽牛肉，食物，最大堆叠 64，1饱食度；100%反胃20s/100%饥饿30s/5%中毒25s，食用1.5s；英文名 Raw Mimoo Beef）
 *   194 = cooked_mimoo_beef（熟米吽牛肉，食物，最大堆叠 64，4饱食度；100%缓慢2s/25%反胃10s，食用1.5s；英文名 Cooked Mimoo Beef）
 *   195 = raw_mimi_mutton（米羊羊肉，食物，最大堆叠 64，1饱食度；50%虚弱10s/70%饥饿20s/100%反胃10s，食用1.5s；英文名 Raw Mimi Mutton）
 *   196 = cooked_mimi_mutton（熟米羊羊肉，食物，最大堆叠 64，3饱食度；10%反胃5s/1%虚弱2s，食用1.5s；英文名 Cooked Mimi Mutton）
 *   197 = raw_miheng_pork（米哼猪肉，食物，最大堆叠 64，2饱食度；100%失明/虚弱/缓慢/中毒/反胃各10s，食用1.5s；英文名 Raw Miheng Pork）
 *   198 = cooked_miheng_pork（熟米哼猪肉，食物，最大堆叠 64，6饱食度；100%失明10s/100%虚弱10s/50%缓慢10s/50%反胃10s，食用1.5s；英文名 Cooked Miheng Pork）
 *   199 = raw_tanmi_fish（贪米鱼肉，食物，最大堆叠 64，0.5饱食度；100%缓慢10s，食用1.5s；英文名 Raw Tanmi Fish）
 *   200 = cooked_tanmi_fish（熟贪米鱼肉，食物，最大堆叠 64，3饱食度；100%缓慢5s/100%虚弱10s，食用1.5s；英文名 Cooked Tanmi Fish）
 *   201 = zhuomi_feather（啄米羽毛，生物掉落普通材料，最大堆叠 64；英文名 Zhuomi Feather）
 *   202 = mimoo_leather（米哞皮革，生物掉落普通材料，最大堆叠 64；英文名 Mimoo Leather）
 *   203 = mimi_wool（米羊羊毛，生物掉落普通材料，最大堆叠 64；英文名 Mimi Wool）
 *   204 = miheng_hide（米哼猪皮，生物掉落普通材料，最大堆叠 64；英文名 Miheng Hide）
 *   205 = penmi_fish_sac（喷米鱼囊，生物掉落普通材料，最大堆叠 64；英文名 Penmi Fish Sac）
 *   206 = haomi_wolf_heart（嗥米狼心，生物掉落普通材料，最大堆叠 64；英文名 Haomi Wolf Heart）
 *   207 = rice_bone（米骨，生物掉落普通材料，最大堆叠 64；英文名 Rice Bone）
 *   208 = rice_arrow（米箭，生物掉落普通材料，最大堆叠 64；英文名 Rice Arrow）
 *   209 = huomi_powder（火米药，生物掉落普通材料，最大堆叠 64；英文名 Huomi Powder）
 *   210 = yanmi_pearl（眼米珍珠，生物掉落普通材料，最大堆叠 64；英文名 Yanmi Pearl）
 *   211 = baoliemi_rod（爆烈米棒，生物掉落普通材料，最大堆叠 64；英文名 Baoliemi Rod）
 *   212 = carved_rice_bone（雕米骨，生物掉落普通材料，最大堆叠 64；英文名 Carved Rice Bone）
 *   213 = milaima_ball（米莱马球，生物掉落普通材料，最大堆叠 64；英文名 Milaima Ball）
 *   214 = nianmi_silk_thread（黏米丝线，生物掉落普通材料，最大堆叠 64；英文名 Nianmi Silk Thread）
 *   215 = rotten_rice_flesh（腐米肉，食物，最大堆叠 64，1饱食度；100%中毒5s/100%饥饿30s/100%缓慢10s/1%中毒60s，食用1.5s；英文名 Rotten Rice Flesh）
 *   216 = mimi_medley（米米大杂烩，普通物品，最大堆叠 64；英文名 Mimi Medley）
 *   217 = rice_bone_meal（米骨粉，普通物品，最大堆叠 64；英文名 Rice Bone Meal）
 *   218 = rice_essence（米精，普通物品，最大堆叠 64；英文名 Rice Essence）
 *   219 = coarse_rice（粗米，普通物品，最大堆叠 64；英文名 Coarse Rice）
 *   220 = rice_brush_pen（米笔，普通物品，最大堆叠 64；英文名 Rice Brush Pen）
 *   221 = book_rice_pen（书米笔，普通物品，最大堆叠 64；英文名 Book Rice Pen）
 *   222 = rice_fruit_black_mixture（米果黑混液，普通物品（非流体），最大堆叠 64，药水瓶外形；英文名 Rice Fruit Black Mixture）
 *   223 = rice_fruit_viscous_mixture（米果混黏液，普通物品（非流体），最大堆叠 64，药水瓶外形；英文名 Rice Fruit Viscous Mixture）
 *   224 = poprice_bloom_liquid（爆米绽放液，普通物品（非流体），最大堆叠 64，药水瓶外形；英文名 Poprice Bloom Liquid）
 *   225 = yanmi_heart_sip_liquid（眼米嘬心液，普通物品（非流体），最大堆叠 64，药水瓶外形；英文名 Yanmi Heart-Sip Liquid）
 *   226 = mimi_medley_liquid（米米杂烩液，普通物品（非流体），最大堆叠 64，药水瓶外形；英文名 Mimi Medley Liquid）
 *   227 = monkey_king_ruyi_staff（猴王如意棍，普通物品，最大堆叠 64；英文名 Monkey King Ruyi Staff）
 *   228 = mimi_monkey_head（米米猴头，普通物品，最大堆叠 64；英文名 Mimi Monkey Head）
 *   229 = tiger_king_razor_claw（虎王利爪，普通物品，最大堆叠 64；英文名 Tiger King Razor Claw）
 *   230 = rice_crystal_tiger_king_head（饭晶虎王头，普通物品，最大堆叠 64；英文名 Rice Crystal Tiger King Head）
 *   231 = ox_king_crystal_horn（牛王晶角，普通物品，最大堆叠 64；英文名 Ox King Crystal Horn）
 *   232 = super_crystal_ox_head（超晶牛头，普通物品，最大堆叠 64；英文名 Super Crystal Ox Head）
 *   233 = rabbit_king_bloody_red_eye（兔王腥红眼，普通物品，最大堆叠 64；英文名 Rabbit King Bloody Red Eye）
 *   234 = heavy_crystal_rabbit_king_head（重晶兔王头，普通物品，最大堆叠 64；英文名 Heavy Crystal Rabbit King Head）
 *   235 = venom_azure_python_fang（极毒青蟒牙，普通物品，最大堆叠 64；英文名 Venom Azure Python Fang）
 *   236 = compound_crystal_azure_python_head（复晶天青蟒头，普通物品，最大堆叠 64；英文名 Compound Crystal Azure Python Head）
 *   237 = evil_kui_longevity_horn（恶夔长命角，普通物品，最大堆叠 64；英文名 Evil Kui Longevity Horn）
 *   238 = purified_longevity_kui_head（清洁长命夔头，普通物品，最大堆叠 64；英文名 Purified Longevity Kui Head）
 *   239 = end_dragon_reverse_scale（终末龙逆鳞，普通物品，最大堆叠 64；英文名 End Dragon Reverse Scale）
 *   240 = laziest_end_dragon_head（最懒终末龙头，普通物品，最大堆叠 64；英文名 Laziest End Dragon Head）
 *   241 = poprice_heart_sip_liquid（爆米嘬心液，普通物品（非流体），最大堆叠 64，药水瓶外形；英文名 Poprice Heart-Sip Liquid）
 *   242 = super_heavy_crystal_pickaxe_head（超重结晶镐头，普通物品（镐头部部件），最大堆叠 64；英文名 Super Heavy Crystal Pickaxe Head）
 *   243 = compound_rice_meal_pickaxe_head（复合米饭镐头，普通物品（镐头部部件），最大堆叠 64；英文名 Compound Rice Meal Pickaxe Head）
 *   244 = cleansing_compound_pickaxe_head（清洁复合镐头，普通物品（镐头部部件），最大堆叠 64；英文名 Cleansing Compound Pickaxe Head）
 *   245 = super_lazy_alloy_pickaxe_head（超懒合金镐头，普通物品（镐头部部件），最大堆叠 64；英文名 Super Lazy Alloy Pickaxe Head）
 *   246 = liemi_chicken（咧米鸡肉，食物，最大堆叠 64，0.5饱食度；10%中毒20s/90%饥饿30s/100%反胃15s，食用1.5s；英文名 Liemi Chicken）
 *   247 = super_heavy_crystal_rod（超重结晶棒，普通物品（手柄部件），最大堆叠 64；英文名 Super Heavy Crystal Rod）
 *   248 = compound_rice_meal_rod（复合米饭棒，普通物品（手柄部件），最大堆叠 64；英文名 Compound Rice Meal Rod）
 *   249 = cleansing_compound_rod（清洁复合棒，普通物品（手柄部件），最大堆叠 64；英文名 Cleansing Compound Rod）
 *   250 = super_lazy_alloy_rod（超懒合金棒，普通物品（手柄部件），最大堆叠 64；英文名 Super Lazy Alloy Rod）
 *   251 = super_heavy_crystal_pickaxe（超重结晶镐，工具类 PickaxeItem，最大堆叠 1，SUPER_HEAVY_CRYSTAL；英文名 Super Heavy Crystal Pickaxe）
 *   252 = compound_rice_meal_pickaxe（复合米饭镐，工具类 PickaxeItem，最大堆叠 1，COMPOUND_RICE_MEAL；英文名 Compound Rice Meal Pickaxe）
 *   253 = cleansing_compound_pickaxe（清洁复合镐，工具类 PickaxeItem，最大堆叠 1，CLEANSING_COMPOUND；英文名 Cleansing Compound Pickaxe）
 *   254 = super_lazy_alloy_pickaxe（超懒合金镐，工具类 PickaxeItem，最大堆叠 1，SUPER_LAZY_ALLOY；英文名 Super Lazy Alloy Pickaxe）
 *   255 = laziest_heavenfall_pickaxe（最懒天堕镐，工具类 PickaxeItem，最大堆叠 1，LAZIEST_HEAVENFALL（修复材料 184 堕落之心）；英文名 Laziest Heavenfall Pickaxe）
 *   256 = rice_stealer_heart（盗米之心，普通物品，最大堆叠 64；英文名 Rice Stealer Heart）
 *   257 = rice_stealer_spawn_egg（盗米者生物蛋，ForgeSpawnEggItem，绑定实体 rice_stealer（当前 PendingBossEntity 占位）；英文名 Rice Stealer Spawn Egg）
 *   258 = rice_soul_tear（米魂之泪，普通物品，最大堆叠 64；英文名 Rice Soul Tear）
 *   259 = remnant_heaven_wing（残天翼，普通物品，最大堆叠 64；英文名 Remnant Heaven Wing）
 *   260 = placeholder_260（待定占位，不进创造标签页；英文名 Reserved Slot 260）
 *   261 = void_touch（虚无之触，普通物品，最大堆叠 64；英文名 Void Touch）
 *   262 = faded_flower_branch（残花枝，普通物品，最大堆叠 64；英文名 Faded Flower Branch）
 *   263 = celestial_phoenix_beak（天凤之喙，普通物品，最大堆叠 64；英文名 Celestial Phoenix Beak）
 *   264 = remnant_fallen_rice_essence（残堕米精，普通物品，最大堆叠 64；英文名 Remnant Fallen Rice Essence）
 *   265 = remnant_fallen_ingot（残堕锭，普通物品，最大堆叠 64；英文名 Remnant Fallen Ingot）
 *   266 = mermaid_tear（鲛泪，普通物品，最大堆叠 64；英文名 Mermaid Tear）
 *   267 = jiaolong_talon（蛟龙爪，普通物品，最大堆叠 64；英文名 Jiaolong Talon）
 *   268 = mirage_bone（蜃楼骨，普通物品，最大堆叠 64；英文名 Mirage Bone）
 *   269 = elf_hat（精灵帽，普通物品【非装备】，最大堆叠 64；英文名 Elf Hat）
 *   270 = moon_devourer_wolf_claw（噬月狼爪，普通物品，最大堆叠 64；英文名 Moon Devourer Wolf Claw）
 *   271 = fallen_altar（堕落台，方块物品 BlockItem；暗合金仪式台方块，镐挖掘等级2=铁镐，破坏掉落本身，方块本体见 block.ModBlocks；英文名 Fallen Altar）
 *   272 = fallen_cross（堕落十字架，普通物品，最大堆叠 64；英文名 Fallen Cross）
 *   273 = placeholder_273（待定占位，不进创造标签页；英文名 Reserved Slot 273）
 *   274 = placeholder_274（待定占位，不进创造标签页；英文名 Reserved Slot 274）
 *   275 = dyeing_solution（浸染液，普通物品，最大堆叠 64；英文名 Dyeing Solution）
 *   276 = deep_dyed_remnant_heaven_wing（深染残天翼，鞘翅类装备，胸甲槽可滑翔，耐久 432 自动锁定堆叠 1；英文名 Deep-Dyed Remnant Heaven Wing）
 *   277 = deep_dyed_void_touch（深染虚无之触，普通物品，最大堆叠 64；英文名 Deep-Dyed Void Touch）
 *   278 = deep_dyed_faded_flower_branch（深染残花枝，普通物品，最大堆叠 64；英文名 Deep-Dyed Faded Flower Branch）
 *   279 = deep_dyed_celestial_phoenix_beak（深染天凤之喙，普通物品，最大堆叠 64；英文名 Deep-Dyed Celestial Phoenix Beak）
 *   280 = deep_dyed_remnant_fallen_rice_essence（深染残堕米精，普通物品，最大堆叠 64；英文名 Deep-Dyed Remnant Fallen Rice Essence）
 *   281 = deep_dyed_mermaid_tear（深染鲛泪，普通物品，最大堆叠 64；英文名 Deep-Dyed Mermaid Tear）
 *   282 = deep_dyed_jiaolong_talon（深染蛟龙爪，普通物品，最大堆叠 64；英文名 Deep-Dyed Jiaolong Talon）
 *   283 = deep_dyed_mirage_bone（深染蜃楼骨，普通物品，最大堆叠 64；英文名 Deep-Dyed Mirage Bone）
 *   284 = deep_dyed_elf_hat（深染精灵帽，普通物品【非装备】，最大堆叠 64；英文名 Deep-Dyed Elf Hat）
 *   285 = deep_dyed_moon_devourer_wolf_claw（深染噬月狼爪，普通物品，最大堆叠 64；英文名 Deep-Dyed Moon Devourer Wolf Claw）
 *   286 = coarse_remnant_fallen_world_essence（粗制残堕世界精华，普通物品，最大堆叠 64；英文名 Coarse Remnant Fallen World Essence）
 * ================================================================================
 */
public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MimiWorld.MODID);

    // 自动编号计数器与双向映射表
    private static int nextItemCode = 0;
    private static final Map<Integer, RegistryObject<Item>> ITEM_BY_CODE = new HashMap<>();
    private static final Map<ResourceLocation, Integer> CODE_BY_ITEM = new HashMap<>();

    /**
     * 注册物品并自动分配下一个连续编号（1, 2, 3 ...）。
     * 物品字段必须按声明顺序从上到下排列，编号即注册顺序。
     */
    private static RegistryObject<Item> registerItem(String name, Supplier<Item> factory) {
        int code = ++nextItemCode;
        RegistryObject<Item> item = ITEMS.register(name, factory);
        ITEM_BY_CODE.put(code, item);
        CODE_BY_ITEM.put(item.getId(), code);
        return item;
    }

    /** 按编码获取物品实例（配方代码里用编码表达需求时使用），编码不存在则抛异常 */
    public static Item byCode(int code) {
        RegistryObject<Item> item = ITEM_BY_CODE.get(code);
        if (item == null) {
            throw new IllegalArgumentException("不存在的物品编码: " + code);
        }
        return item.get();
    }

    /** 按编码获取物品注册名（生成配方 JSON / 数据包内容时使用） */
    public static ResourceLocation idOfCode(int code) {
        RegistryObject<Item> item = ITEM_BY_CODE.get(code);
        if (item == null) {
            throw new IllegalArgumentException("不存在的物品编码: " + code);
        }
        return item.getId();
    }

    /** 查询某物品对应的编码（反向查询），未注册物品抛异常 */
    public static int codeOf(Item item) {
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
        Integer code = key == null ? null : CODE_BY_ITEM.get(key);
        if (code == null) {
            throw new IllegalArgumentException("未登记编码的物品: " + key);
        }
        return code;
    }

    // ============================ 物品注册区（只能末尾追加） ============================

    // 米（Rice）：可食用，最大堆叠 64；食用后 100% 触发四种负面效果（20 tick = 1 秒，amplifier 从 0 起算）
    public static final FoodProperties RICE_FOOD = new FoodProperties.Builder()
            .nutrition(1)            // 饱食度回复（饥饿栏鸡腿数，后续可调）
            .saturationMod(0.1F)     // 饱和度，后续可调
            .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 200, 2), 1.0F)   // 反胃 III 10s
            .effect(() -> new MobEffectInstance(MobEffects.POISON, 100, 0), 1.0F)       // 中毒 I 5s
            .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 0), 1.0F) // 缓慢 I 10s
            .effect(() -> new MobEffectInstance(MobEffects.HUNGER, 600, 0), 1.0F)       // 饥饿 I 30s
            .build();

    // 编码 1 = rice（米）
    public static final RegistryObject<Item> RICE = registerItem("rice",
            () -> new Item(new Item.Properties().stacksTo(64).food(RICE_FOOD)));

    // 熟米（Cooked Rice）：食用回复 2.5 个鸡腿（nutrition 单位=半个鸡腿 → 5）；
    // 实际饱和度 = 2 × nutrition × saturationMod = 2×5×0.3 = 3；100% 反胃 I 5s；食用耗时 1s（默认食物为 32 tick）
    public static final FoodProperties COOKED_RICE_FOOD = new FoodProperties.Builder()
            .nutrition(5)
            .saturationMod(0.3F)
            .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 100, 0), 1.0F) // 反胃 I 5s
            .build();

    // 编码 2 = cooked_rice（熟米）
    public static final RegistryObject<Item> COOKED_RICE = registerItem("cooked_rice",
            () -> new Item(new Item.Properties().stacksTo(64).food(COOKED_RICE_FOOD)) {
                @Override
                public int getUseDuration(ItemStack stack) {
                    return 20; // 食用时间 1 秒
                }
            });

    // 编码 3 = rice_ball（米团）：可投掷，命中造成 1 伤害 + 反胃 I/缓慢 I 各 10s，投掷冷却 2s（逻辑见 RiceBallItem / RiceBallProjectile）
    public static final RegistryObject<Item> RICE_BALL = registerItem("rice_ball",
            () -> new RiceBallItem(new Item.Properties().stacksTo(64)));

    // 编码 4 = cooked_rice_ball（熟米团）：可投掷，命中 2.5 伤害 + 缓慢 III/失明 II 各 10s，投掷冷却 2s（逻辑见 CookedRiceBallItem / CookedRiceBallProjectile）
    public static final RegistryObject<Item> COOKED_RICE_BALL = registerItem("cooked_rice_ball",
            () -> new CookedRiceBallItem(new Item.Properties().stacksTo(64)));

    // 编码 5 = rice_log（米原木）：方块物品，放置后对应 ModBlocks.RICE_LOG；破坏掉落自身（loot table），斧/挖掘等级 1 由方块标签控制
    public static final RegistryObject<Item> RICE_LOG = registerItem("rice_log",
            () -> new BlockItem(ModBlocks.RICE_LOG.get(), new Item.Properties()));

    // 编码 6 = rice_planks（米木板）：方块物品，放置后对应 ModBlocks.RICE_PLANKS；破坏掉落自身，斧可挖、挖掘等级 0（无 needs_*_tool 标签）
    public static final RegistryObject<Item> RICE_PLANKS = registerItem("rice_planks",
            () -> new BlockItem(ModBlocks.RICE_PLANKS.get(), new Item.Properties()));

    // 编码 7 = rice_stick（米木棍）：普通材料物品，最大堆叠 64；熔炉燃料 300t（可烧 1.5 个物品，见 event.ModFuelEvents）
    public static final RegistryObject<Item> RICE_STICK = registerItem("rice_stick",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 8 = rice_leaves（米树叶）：方块物品，放置后对应 ModBlocks.RICE_LEAVES；破坏掉落自身，剪刀可快速采集、挖掘等级 0，燃料 100t
    public static final RegistryObject<Item> RICE_LEAVES = registerItem("rice_leaves",
            () -> new BlockItem(ModBlocks.RICE_LEAVES.get(), new Item.Properties()));

    // 编码 9 = rice_bucket（米桶）：米汤桶，最大堆叠 1；RiceBucketItem 继承 BucketItem，
    // 构造器内部绑定 fluid.ModFluids.RICE_SOUP（米汤源流体），行为同原版水桶
    public static final RegistryObject<Item> RICE_BUCKET = registerItem("rice_bucket",
            () -> new RiceBucketItem(new Item.Properties().stacksTo(1)));

    // 编码 10 = rice_sapling（米树苗）：方块物品，放置后对应 ModBlocks.RICE_SAPLING；可骨粉催熟/随机刻长成米树（白桦形态，米原木+米树叶）
    public static final RegistryObject<Item> RICE_SAPLING = registerItem("rice_sapling",
            () -> new BlockItem(ModBlocks.RICE_SAPLING.get(), new Item.Properties()));

    // 编码 11 = hardened_rice（硬化米）：石头类方块物品，放置后对应 ModBlocks.HARDENED_RICE
    public static final RegistryObject<Item> HARDENED_RICE = registerItem("hardened_rice",
            () -> new BlockItem(ModBlocks.HARDENED_RICE.get(), new Item.Properties()));

    // 编码 12 = deepslate_hardened_rice（深层硬化米）：石头类方块物品，放置后对应 ModBlocks.DEEPSLATE_HARDENED_RICE
    public static final RegistryObject<Item> DEEPSLATE_HARDENED_RICE = registerItem("deepslate_hardened_rice",
            () -> new BlockItem(ModBlocks.DEEPSLATE_HARDENED_RICE.get(), new Item.Properties()));

    // 编码 13 = rice_bedrock（基米）：基岩类方块物品，放置后对应 ModBlocks.RICE_BEDROCK；仅创造模式获取
    public static final RegistryObject<Item> RICE_BEDROCK = registerItem("rice_bedrock",
            () -> new BlockItem(ModBlocks.RICE_BEDROCK.get(), new Item.Properties()));

    // 米草食物属性：nutrition=1 = 0.5 个鸡腿（nutrition 单位为半个鸡腿）；saturationMod 0.5；
    // 100% 反胃 I 5s + 缓慢 II（amplifier=1）5s；食用耗时 0.5s 在 RiceGrassItem#getUseDuration 中固定为 10t
    public static final FoodProperties RICE_GRASS_FOOD = new FoodProperties.Builder()
            .nutrition(1)
            .saturationMod(0.5F)
            .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 100, 0), 1.0F)       // 反胃 I 5s
            .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1), 1.0F) // 缓慢 II 5s
            .build();

    // 编码 14 = rice_grass（米草）：可放置的草类植物方块物品（自定义 RiceGrassItem，右键可种草也可食用）；
    // 放置后对应 ModBlocks.RICE_GRASS，该植物空手破坏不掉落，仅剪刀采集掉本物品
    public static final RegistryObject<Item> RICE_GRASS = registerItem("rice_grass",
            () -> new RiceGrassItem(ModBlocks.RICE_GRASS.get(),
                    new Item.Properties().stacksTo(64).food(RICE_GRASS_FOOD)));

    // 米果食物属性（定位类似原版苹果）：nutrition=5 = 2.5 个鸡腿；saturationMod=2.0F
    // （实际饱和度 = 2×5×2 = 20）；100% 漂浮 I 30s + 反胃 I 5s + 虚弱 I 30s
    public static final FoodProperties RICE_FRUIT_FOOD = new FoodProperties.Builder()
            .nutrition(5)
            .saturationMod(2.0F)
            .effect(() -> new MobEffectInstance(MobEffects.LEVITATION, 600, 0), 1.0F) // 漂浮 I 30s
            .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 100, 0), 1.0F)  // 反胃 I 5s
            .effect(() -> new MobEffectInstance(MobEffects.WEAKNESS, 600, 0), 1.0F)  // 虚弱 I 30s
            .build();

    // 编码 15 = rice_fruit（米果）：普通食物物品，最大堆叠 64；食用耗时 2s（默认食物 32t ≈ 1.6s，这里覆盖为 40t）
    public static final RegistryObject<Item> RICE_FRUIT = registerItem("rice_fruit",
            () -> new Item(new Item.Properties().stacksTo(64).food(RICE_FRUIT_FOOD)) {
                @Override
                public int getUseDuration(ItemStack stack) {
                    return 40; // 食用时间 2 秒
                }
            });

    // 编码 16 = crystallized_rice（结晶米）：类似原版锭的普通矿石材料，最大堆叠 64
    public static final RegistryObject<Item> CRYSTALLIZED_RICE = registerItem("crystallized_rice",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 17 = crystallized_rice_ore（结晶米矿石）：石头类矿石方块物品，放置后对应 ModBlocks.CRYSTALLIZED_RICE_ORE；
    // 下界合金镐才能采集，破坏掉 3~7 经验，可烧炼成 16 号结晶米锭
    public static final RegistryObject<Item> CRYSTALLIZED_RICE_ORE = registerItem("crystallized_rice_ore",
            () -> new BlockItem(ModBlocks.CRYSTALLIZED_RICE_ORE.get(), new Item.Properties()));

    // 编码 18 = recrystallized_rice_ore（重结晶米矿石）：深板岩质地矿石，
    // 放置后对应 ModBlocks.RECRYSTALLIZED_RICE_ORE；下界合金镐采集，破坏掉 3~7 经验，烧炼得 19 号重结晶米
    public static final RegistryObject<Item> RECRYSTALLIZED_RICE_ORE = registerItem("recrystallized_rice_ore",
            () -> new BlockItem(ModBlocks.RECRYSTALLIZED_RICE_ORE.get(), new Item.Properties()));

    // 编码 19 = recrystallized_rice（重结晶米）：精炼锭类普通材料，最大堆叠 64；由 18 号重结晶米矿石熔炉/高炉烧炼得到
    public static final RegistryObject<Item> RECRYSTALLIZED_RICE = registerItem("recrystallized_rice",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 20 = combustible_rice（可燃米）：普通材料，最大堆叠 64；
    // 作为熔炉燃料可烧 32 个物品（6400 tick），燃料值在 event.ModFuelEvents 中赋予
    public static final RegistryObject<Item> COMBUSTIBLE_RICE = registerItem("combustible_rice",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 21 = combustible_rice_ore（可燃米矿石）：石头类矿石方块物品，放置后对应 ModBlocks.COMBUSTIBLE_RICE_ORE；
    // 下界合金镐采集，破坏掉 3~7 经验，熔炉/高炉烧炼得 20 号可燃米
    public static final RegistryObject<Item> COMBUSTIBLE_RICE_ORE = registerItem("combustible_rice_ore",
            () -> new BlockItem(ModBlocks.COMBUSTIBLE_RICE_ORE.get(), new Item.Properties()));

    // 编码 22 = mud_rice（泥米）：泥巴质地方块物品，放置后对应 ModBlocks.MUD_RICE；铁铲（挖掘等级2）采集
    public static final RegistryObject<Item> MUD_RICE = registerItem("mud_rice",
            () -> new BlockItem(ModBlocks.MUD_RICE.get(), new Item.Properties()));

    // 编码 23 = reinforced_rice_stick（强化米棍，Reinforced Rice Stick）：普通材料物品，最大堆叠 64
    public static final RegistryObject<Item> REINFORCED_RICE_STICK = registerItem("reinforced_rice_stick",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 24 = rice_baton（米棍，Rice Baton）：普通材料物品，最大堆叠 64
    public static final RegistryObject<Item> RICE_BATON = registerItem("rice_baton",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 25 = rice_flour（米粉，Rice Flour）：普通材料物品，最大堆叠 64
    public static final RegistryObject<Item> RICE_FLOUR = registerItem("rice_flour",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 26 = coarse_rice_block（粗米块，Coarse Rice Block）：石头类方块物品，放置后对应 ModBlocks.COARSE_RICE_BLOCK；钻石镐（挖掘等级3）采集
    public static final RegistryObject<Item> COARSE_RICE_BLOCK = registerItem("coarse_rice_block",
            () -> new BlockItem(ModBlocks.COARSE_RICE_BLOCK.get(), new Item.Properties()));

    // 编码 27 = rice_maker（造米机，Rice Maker）：石头类方块物品，放置后对应 ModBlocks.RICE_MAKER；钻石镐（挖掘等级3）采集
    public static final RegistryObject<Item> RICE_MAKER = registerItem("rice_maker",
            () -> new BlockItem(ModBlocks.RICE_MAKER.get(), new Item.Properties()));

    // 编码 28 = rice_block（米块，Rice Block）：石头类方块物品，放置后对应 ModBlocks.RICE_BLOCK；钻石镐（挖掘等级3）采集
    public static final RegistryObject<Item> RICE_BLOCK = registerItem("rice_block",
            () -> new BlockItem(ModBlocks.RICE_BLOCK.get(), new Item.Properties()));

    // 编码 29 = rice_pounding_table（锤米台，Rice Pounding Table）：石头类方块物品，放置后对应 ModBlocks.RICE_POUNDING_TABLE；钻石镐（挖掘等级3）采集
    public static final RegistryObject<Item> RICE_POUNDING_TABLE = registerItem("rice_pounding_table",
            () -> new BlockItem(ModBlocks.RICE_POUNDING_TABLE.get(), new Item.Properties()));

    // 编码 30 = rice_pail（饭桶，Rice Pail）：饭汤桶，最大堆叠 1；RicePailItem 继承 BucketItem，
    // 构造器内部绑定 fluid.ModFluids.RICE_BROTH（饭汤源流体），行为同原版水桶
    public static final RegistryObject<Item> RICE_PAIL = registerItem("rice_pail",
            () -> new RicePailItem(new Item.Properties().stacksTo(1)));

    // 编码 31 = rice_press（压饭机，Rice Press）：石头类方块物品，放置后对应 ModBlocks.RICE_PRESS；铁镐（挖掘等级2）采集
    public static final RegistryObject<Item> RICE_PRESS = registerItem("rice_press",
            () -> new BlockItem(ModBlocks.RICE_PRESS.get(), new Item.Properties()));

    // 编码 32 = compressed_rice_pail（压缩饭桶，Compressed Rice Pail）：普通物品，最大堆叠 64
    public static final RegistryObject<Item> COMPRESSED_RICE_PAIL = registerItem("compressed_rice_pail",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 33 = coarse_crystallized_rice_meal（粗制结晶饭，Coarse Crystallized Rice Meal）：普通物品，最大堆叠 64
    public static final RegistryObject<Item> COARSE_CRYSTALLIZED_RICE_MEAL = registerItem("coarse_crystallized_rice_meal",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 34 = crystallized_rice_meal（结晶饭，Crystallized Rice Meal）：普通物品，最大堆叠 64；注意区别于 16 号结晶米 CRYSTALLIZED_RICE
    public static final RegistryObject<Item> CRYSTALLIZED_RICE_MEAL = registerItem("crystallized_rice_meal",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 35 = rice_crystal_alloy_ingot（饭晶合金锭，Rice Crystal Alloy Ingot）：普通物品，最大堆叠 64
    public static final RegistryObject<Item> RICE_CRYSTAL_ALLOY_INGOT = registerItem("rice_crystal_alloy_ingot",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 36 = rice_crystal_hammer（饭晶锤，Rice Crystal Hammer）：自定义工具类，镐类挖掘（钻石级 tier），近战 +7 伤害/-3.2 攻速；
    // 直接继承 Item（非 DiggerItem），stacksTo(1) 与普通工具一致，逻辑见 RiceCrystalHammerItem
    public static final RegistryObject<Item> RICE_CRYSTAL_HAMMER = registerItem("rice_crystal_hammer",
            RiceCrystalHammerItem::new);

    // 编码 37 = wet_grass_fiber（湿草麻，Wet Grass Fiber）：纤维束材料
    public static final RegistryObject<Item> WET_GRASS_FIBER = registerItem("wet_grass_fiber",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 38 = coarse_rice_fiber（粗制米麻，Coarse Rice Fiber）：纤维束材料
    public static final RegistryObject<Item> COARSE_RICE_FIBER = registerItem("coarse_rice_fiber",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 39 = reinforced_wet_grass_fiber（强化湿草麻，Reinforced Wet Grass Fiber）：纤维束材料
    public static final RegistryObject<Item> REINFORCED_WET_GRASS_FIBER = registerItem("reinforced_wet_grass_fiber",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 40 = refined_rice_fiber（精制米麻，Refined Rice Fiber）：纤维束材料
    public static final RegistryObject<Item> REFINED_RICE_FIBER = registerItem("refined_rice_fiber",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 41 = wet_grass_net（湿草网，Wet Grass Net）：编织网材料
    public static final RegistryObject<Item> WET_GRASS_NET = registerItem("wet_grass_net",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 42 = coarse_wet_grass_net（粗制湿草网，Coarse Wet Grass Net）：编织网材料
    public static final RegistryObject<Item> COARSE_WET_GRASS_NET = registerItem("coarse_wet_grass_net",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 43 = coarse_rice_net（粗制米网，Coarse Rice Net）：编织网材料
    public static final RegistryObject<Item> COARSE_RICE_NET = registerItem("coarse_rice_net",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 44 = reinforced_wet_grass_net（强化湿草网，Reinforced Wet Grass Net）：编织网材料
    public static final RegistryObject<Item> REINFORCED_WET_GRASS_NET = registerItem("reinforced_wet_grass_net",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 45 = refined_reinforced_wet_grass_net（精制强化湿草网，Refined Reinforced Wet Grass Net）：编织网材料
    public static final RegistryObject<Item> REFINED_REINFORCED_WET_GRASS_NET = registerItem("refined_reinforced_wet_grass_net",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 46 = refined_reinforced_rice_net（精制强化米网，Refined Reinforced Rice Net）：编织网材料
    public static final RegistryObject<Item> REFINED_REINFORCED_RICE_NET = registerItem("refined_reinforced_rice_net",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 47 = rice_crystal_alloy_hammer（饭晶合金锤，Rice Crystal Alloy Hammer）：自定义工具类，
    // 36号饭晶锤的合金强化版；镐类挖掘（下界合金级 tier，speed 9.0），近战 +9 伤害/-3.1 攻速；
    // 直接继承 Item（非 DiggerItem），stacksTo(1) 与普通工具一致，逻辑见 RiceCrystalAlloyHammerItem
    public static final RegistryObject<Item> RICE_CRYSTAL_ALLOY_HAMMER = registerItem("rice_crystal_alloy_hammer",
            RiceCrystalAlloyHammerItem::new);

    // 编码 48 = rice_crystal_bow（饭晶弓，Rice Crystal Bow）：工具类弓（BowItem），最大堆叠 1，
    // 耐久 384（与原版弓一致）；原 placeholder_48 占位物按手稿原位替换为该物品，编码 48 不变
    public static final RegistryObject<Item> RICE_CRYSTAL_BOW = registerItem("rice_crystal_bow",
            () -> new BowItem(new Item.Properties().durability(384)));

    // 编码 49 = advanced_rice_pounding_table（高级锤米台，Advanced Rice Pounding Table）：石头类方块物品，
    // 放置后对应 ModBlocks.ADVANCED_RICE_POUNDING_TABLE；铁镐（挖掘等级2）采集，破坏掉落本身
    public static final RegistryObject<Item> ADVANCED_RICE_POUNDING_TABLE = registerItem("advanced_rice_pounding_table",
            () -> new BlockItem(ModBlocks.ADVANCED_RICE_POUNDING_TABLE.get(), new Item.Properties()));

    // 编码 50 = coarse_super_crystal（粗超结晶，Coarse Super Crystal）：结晶材料，粗糙灰蓝晶簇含金色杂质
    public static final RegistryObject<Item> COARSE_SUPER_CRYSTAL = registerItem("coarse_super_crystal",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 51 = super_crystal（超结晶，Super Crystal）：结晶材料，高纯度亮蓝晶簇含金核
    public static final RegistryObject<Item> SUPER_CRYSTAL = registerItem("super_crystal",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 52 = rice_ore（米矿石，Rice Ore）：铁矿石类方块物品，放置后对应 ModBlocks.RICE_ORE；
    // 钻石镐（挖掘等级3）采集，破坏掉落本身并额外掉 3~7 经验球（非精准采集）
    public static final RegistryObject<Item> RICE_ORE = registerItem("rice_ore",
            () -> new BlockItem(ModBlocks.RICE_ORE.get(), new Item.Properties()));

    // 编码 53 = rice_stone（米石，Rice Stone）：嵌有奶白米纹的小石块，普通材料物品，最大堆叠 64
    public static final RegistryObject<Item> RICE_STONE = registerItem("rice_stone",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 54 = super_crystal_block（超结晶块，Block of Super Crystal）：51号超结晶的压缩块，
    // 放置后对应 ModBlocks.SUPER_CRYSTAL_BLOCK；铁镐（挖掘等级2）采集，破坏掉落本身
    public static final RegistryObject<Item> SUPER_CRYSTAL_BLOCK = registerItem("super_crystal_block",
            () -> new BlockItem(ModBlocks.SUPER_CRYSTAL_BLOCK.get(), new Item.Properties()));

    // 编码 55 = super_heavy_crystal（超重结晶，Super Heavy Crystal）：高密度重晶材料，普通物品，最大堆叠 64
    public static final RegistryObject<Item> SUPER_HEAVY_CRYSTAL = registerItem("super_heavy_crystal",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 56 = ionization_table（电离台，Ionization Table）：工艺机器方块（装饰性全立方块），
    // 放置后对应 ModBlocks.IONIZATION_TABLE；钻石镐（挖掘等级3）采集，破坏掉落本身
    public static final RegistryObject<Item> IONIZATION_TABLE = registerItem("ionization_table",
            () -> new BlockItem(ModBlocks.IONIZATION_TABLE.get(), new Item.Properties()));

    // 编码 57 = precipitation_table（沉淀台，Precipitation Table）：工艺机器方块（装饰性全立方块），
    // 放置后对应 ModBlocks.PRECIPITATION_TABLE；钻石镐（挖掘等级3）采集，破坏掉落本身
    public static final RegistryObject<Item> PRECIPITATION_TABLE = registerItem("precipitation_table",
            () -> new BlockItem(ModBlocks.PRECIPITATION_TABLE.get(), new Item.Properties()));

    // 编码 58 = coarse_compound_rice_meal_ingot（粗制复合米饭锭，Coarse Compound Rice Meal Ingot）：粗糙半成品锭
    public static final RegistryObject<Item> COARSE_COMPOUND_RICE_MEAL_INGOT =
            registerItem("coarse_compound_rice_meal_ingot",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 59 = compound_rice_meal_plate（复合米饭板，Compound Rice Meal Plate）：压制薄板半成品
    public static final RegistryObject<Item> COMPOUND_RICE_MEAL_PLATE =
            registerItem("compound_rice_meal_plate",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 60 = compound_rice_meal_ingot（复合米饭锭，Compound Rice Meal Ingot）：精制光滑成品锭
    public static final RegistryObject<Item> COMPOUND_RICE_MEAL_INGOT =
            registerItem("compound_rice_meal_ingot",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 61 = rice_pomace（米果渣，Rice Pomace）：榨汁后的米果残渣
    public static final RegistryObject<Item> RICE_POMACE = registerItem("rice_pomace",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 62 = rice_fruit_juice（米果汁，Rice Fruit Juice）：药水瓶造型的普通物品（非流体、不可倒出）
    public static final RegistryObject<Item> RICE_FRUIT_JUICE = registerItem("rice_fruit_juice",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 63 = rice_fruit_mixture（米果混合液，Rice Fruit Mixture）：药水瓶造型的普通物品（非流体、不可倒出）
    public static final RegistryObject<Item> RICE_FRUIT_MIXTURE = registerItem("rice_fruit_mixture",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 64 = dissociation_table（离解台，Dissociation Table）：工艺机器方块（装饰性全立方块），
    // 放置后对应 ModBlocks.DISSOCIATION_TABLE；铁镐（挖掘等级2）采集，破坏掉落本身
    public static final RegistryObject<Item> DISSOCIATION_TABLE = registerItem("dissociation_table",
            () -> new BlockItem(ModBlocks.DISSOCIATION_TABLE.get(), new Item.Properties()));

    // ===== 65-80：米纸文书、米果加工液/渣、清洁复合与超懒合金材料链（均为普通物品，最大堆叠 64） =====

    // 编码 65 = rice_book（米书，Rice Book）
    public static final RegistryObject<Item> RICE_BOOK = registerItem("rice_book",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 66 = rice_paper（米纸，Rice Paper）
    public static final RegistryObject<Item> RICE_PAPER = registerItem("rice_paper",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 67 = rice_fruit_analyzing_solution（米果解析液，Rice Fruit Analyzing Solution）：药水瓶造型普通物品（非流体）
    public static final RegistryObject<Item> RICE_FRUIT_ANALYZING_SOLUTION =
            registerItem("rice_fruit_analyzing_solution",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 68 = rice_fruit_compound_solution（米果复合液，Rice Fruit Compound Solution）：药水瓶造型普通物品（非流体）
    public static final RegistryObject<Item> RICE_FRUIT_COMPOUND_SOLUTION =
            registerItem("rice_fruit_compound_solution",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 69 = rice_meal_mixture（米饭混合液，Rice Meal Mixture）：药水瓶造型普通物品（非流体）
    public static final RegistryObject<Item> RICE_MEAL_MIXTURE = registerItem("rice_meal_mixture",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 70 = rice_fruit_mixed_pomace（米果混合渣，Rice Fruit Mixed Pomace）
    public static final RegistryObject<Item> RICE_FRUIT_MIXED_POMACE =
            registerItem("rice_fruit_mixed_pomace",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 71 = rice_fruit_cleansing_solution（米果清洁液，Rice Fruit Cleansing Solution）：药水瓶造型普通物品（非流体）
    public static final RegistryObject<Item> RICE_FRUIT_CLEANSING_SOLUTION =
            registerItem("rice_fruit_cleansing_solution",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 72 = coarse_cleansing_compound_ingot（粗制清洁复合锭，Coarse Cleansing Compound Ingot）：粗糙半成品锭
    public static final RegistryObject<Item> COARSE_CLEANSING_COMPOUND_INGOT =
            registerItem("coarse_cleansing_compound_ingot",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 73 = cleansing_compound_chunk（清洁复合碎块，Cleansing Compound Chunk）
    public static final RegistryObject<Item> CLEANSING_COMPOUND_CHUNK =
            registerItem("cleansing_compound_chunk",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 74 = cleansing_compound_plate（清洁复合板，Cleansing Compound Plate）
    public static final RegistryObject<Item> CLEANSING_COMPOUND_PLATE =
            registerItem("cleansing_compound_plate",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 75 = cleansing_compound_ingot（清洁复合锭，Cleansing Compound Ingot）：精制光滑成品锭
    public static final RegistryObject<Item> CLEANSING_COMPOUND_INGOT =
            registerItem("cleansing_compound_ingot",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 76 = coarse_super_lazy_alloy_ingot（粗制超懒合金锭，Coarse Super Lazy Alloy Ingot）：粗糙半成品锭
    public static final RegistryObject<Item> COARSE_SUPER_LAZY_ALLOY_INGOT =
            registerItem("coarse_super_lazy_alloy_ingot",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 77 = super_lazy_alloy_ionic_form（超懒合金离子态，Super Lazy Alloy Ionic Form）：药水瓶造型普通物品（非流体）
    public static final RegistryObject<Item> SUPER_LAZY_ALLOY_IONIC_FORM =
            registerItem("super_lazy_alloy_ionic_form",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 78 = super_lazy_alloy_dust（超懒合金粉，Super Lazy Alloy Dust）：粉末状普通物品
    public static final RegistryObject<Item> SUPER_LAZY_ALLOY_DUST =
            registerItem("super_lazy_alloy_dust",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 79 = super_lazy_alloy_chunk（超懒合金碎块，Super Lazy Alloy Chunk）
    public static final RegistryObject<Item> SUPER_LAZY_ALLOY_CHUNK =
            registerItem("super_lazy_alloy_chunk",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 80 = super_lazy_alloy_ingot（超懒合金锭，Super Lazy Alloy Ingot）：精制光滑成品锭
    public static final RegistryObject<Item> SUPER_LAZY_ALLOY_INGOT =
            registerItem("super_lazy_alloy_ingot",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 81 = rice_stone_block（米石块，Block of Rice Stone）：53号米石的压缩存储块，
    // 放置后对应 ModBlocks.RICE_STONE_BLOCK；铁镐（挖掘等级2）采集，破坏掉落本身
    public static final RegistryObject<Item> RICE_STONE_BLOCK = registerItem("rice_stone_block",
            () -> new BlockItem(ModBlocks.RICE_STONE_BLOCK.get(), new Item.Properties()));

    // 编码 82 = combustible_mixture（可燃混合液，Combustible Mixture）：药水瓶造型普通物品（非流体），橙红易燃液
    public static final RegistryObject<Item> COMBUSTIBLE_MIXTURE = registerItem("combustible_mixture",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 83 = combustible_solid（可燃固态物，Combustible Solid）：带余烬裂纹的可燃硬块
    public static final RegistryObject<Item> COMBUSTIBLE_SOLID = registerItem("combustible_solid",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 84 = cleansing_rice_separation_solution（清洁米沉离液，Cleansing Rice Separation Solution）：
    // 药水瓶造型普通物品（非流体），瓶内上层清洁清液、下层米质沉离
    public static final RegistryObject<Item> CLEANSING_RICE_SEPARATION_SOLUTION =
            registerItem("cleansing_rice_separation_solution",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // ================== 85-129：装备材料链（米木<饭晶<饭晶合金<超重结晶<复合米饭<清洁复合<超懒合金） ==================

    // ---- 85-87 饭晶 / 超结晶基础武器 ----
    // 编码 85 = rice_crystal_sword（饭晶剑）：珍珠米白晶质剑，ModItemTiers.RICE_CRYSTAL（修复材料16号结晶米）
    public static final RegistryObject<Item> RICE_CRYSTAL_SWORD = registerItem("rice_crystal_sword",
            () -> new SwordItem(ModItemTiers.RICE_CRYSTAL, 3, -2.4F, new Item.Properties().stacksTo(1)));

    // 编码 86 = super_crystal_sword（超结晶剑）：蓝晶金核剑，SUPER_CRYSTAL（修复材料51号超结晶）
    public static final RegistryObject<Item> SUPER_CRYSTAL_SWORD = registerItem("super_crystal_sword",
            () -> new SwordItem(ModItemTiers.SUPER_CRYSTAL, 3, -2.4F, new Item.Properties().stacksTo(1)));

    // 编码 87 = super_crystal_bow（超结晶弓）：耐久 640 的弓
    public static final RegistryObject<Item> SUPER_CRYSTAL_BOW = registerItem("super_crystal_bow",
            () -> new BowItem(new Item.Properties().durability(640)));

    // ---- 88-93 饭晶合金武器/防具（修复材料35号饭晶合金锭） ----
    public static final RegistryObject<Item> RICE_CRYSTAL_ALLOY_SWORD = registerItem("rice_crystal_alloy_sword",
            () -> new SwordItem(ModItemTiers.RICE_CRYSTAL_ALLOY, 3, -2.4F, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> RICE_CRYSTAL_ALLOY_BOW = registerItem("rice_crystal_alloy_bow",
            () -> new BowItem(new Item.Properties().durability(896)));

    public static final RegistryObject<Item> RICE_CRYSTAL_ALLOY_HELMET = registerItem("rice_crystal_alloy_helmet",
            () -> new ArmorItem(ModArmorMaterials.RICE_CRYSTAL_ALLOY, ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> RICE_CRYSTAL_ALLOY_CHESTPLATE = registerItem("rice_crystal_alloy_chestplate",
            () -> new ArmorItem(ModArmorMaterials.RICE_CRYSTAL_ALLOY, ArmorItem.Type.CHESTPLATE, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> RICE_CRYSTAL_ALLOY_LEGGINGS = registerItem("rice_crystal_alloy_leggings",
            () -> new ArmorItem(ModArmorMaterials.RICE_CRYSTAL_ALLOY, ArmorItem.Type.LEGGINGS, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> RICE_CRYSTAL_ALLOY_BOOTS = registerItem("rice_crystal_alloy_boots",
            () -> new ArmorItem(ModArmorMaterials.RICE_CRYSTAL_ALLOY, ArmorItem.Type.BOOTS, new Item.Properties().stacksTo(1)));

    // ---- 94-95 超重结晶武器（慢速重武器，修复材料55号超重结晶） ----
    public static final RegistryObject<Item> SUPER_HEAVY_CRYSTAL_SWORD = registerItem("super_heavy_crystal_sword",
            () -> new SwordItem(ModItemTiers.SUPER_HEAVY_CRYSTAL, 5, -2.8F, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> SUPER_HEAVY_CRYSTAL_BOW = registerItem("super_heavy_crystal_bow",
            () -> new BowItem(new Item.Properties().durability(1024)));

    // ---- 96-101 装备锻打专用锭（普通材料，堆叠64） ----
    // 编码 96 = sword_temper_ingot（磨剑锭）：锻打剑刃用
    public static final RegistryObject<Item> SWORD_TEMPER_INGOT = registerItem("sword_temper_ingot",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 97 = bowstring_ingot（弓弦锭）：锻打弓弦用
    public static final RegistryObject<Item> BOWSTRING_INGOT = registerItem("bowstring_ingot",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 98 = armor_core_ingot（含甲锭）：锻打甲片用
    public static final RegistryObject<Item> ARMOR_CORE_INGOT = registerItem("armor_core_ingot",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 99 = advanced_sword_temper_ingot（高级磨剑锭）
    public static final RegistryObject<Item> ADVANCED_SWORD_TEMPER_INGOT = registerItem("advanced_sword_temper_ingot",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 100 = advanced_bowstring_ingot（高级弓弦锭）
    public static final RegistryObject<Item> ADVANCED_BOWSTRING_INGOT = registerItem("advanced_bowstring_ingot",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 101 = advanced_armor_core_ingot（高级含甲锭）
    public static final RegistryObject<Item> ADVANCED_ARMOR_CORE_INGOT = registerItem("advanced_armor_core_ingot",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // ---- 102-105 超重结晶防具（高防重甲，修复材料55号超重结晶） ----
    public static final RegistryObject<Item> SUPER_HEAVY_CRYSTAL_HELMET = registerItem("super_heavy_crystal_helmet",
            () -> new ArmorItem(ModArmorMaterials.SUPER_HEAVY_CRYSTAL, ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> SUPER_HEAVY_CRYSTAL_CHESTPLATE = registerItem("super_heavy_crystal_chestplate",
            () -> new ArmorItem(ModArmorMaterials.SUPER_HEAVY_CRYSTAL, ArmorItem.Type.CHESTPLATE, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> SUPER_HEAVY_CRYSTAL_LEGGINGS = registerItem("super_heavy_crystal_leggings",
            () -> new ArmorItem(ModArmorMaterials.SUPER_HEAVY_CRYSTAL, ArmorItem.Type.LEGGINGS, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> SUPER_HEAVY_CRYSTAL_BOOTS = registerItem("super_heavy_crystal_boots",
            () -> new ArmorItem(ModArmorMaterials.SUPER_HEAVY_CRYSTAL, ArmorItem.Type.BOOTS, new Item.Properties().stacksTo(1)));

    // ---- 106-111 复合米饭武器/防具（修复材料60号复合米饭锭） ----
    public static final RegistryObject<Item> COMPOUND_RICE_MEAL_SWORD = registerItem("compound_rice_meal_sword",
            () -> new SwordItem(ModItemTiers.COMPOUND_RICE_MEAL, 4, -2.4F, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> COMPOUND_RICE_MEAL_BOW = registerItem("compound_rice_meal_bow",
            () -> new BowItem(new Item.Properties().durability(1280)));

    public static final RegistryObject<Item> COMPOUND_RICE_MEAL_HELMET = registerItem("compound_rice_meal_helmet",
            () -> new ArmorItem(ModArmorMaterials.COMPOUND_RICE_MEAL, ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> COMPOUND_RICE_MEAL_CHESTPLATE = registerItem("compound_rice_meal_chestplate",
            () -> new ArmorItem(ModArmorMaterials.COMPOUND_RICE_MEAL, ArmorItem.Type.CHESTPLATE, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> COMPOUND_RICE_MEAL_LEGGINGS = registerItem("compound_rice_meal_leggings",
            () -> new ArmorItem(ModArmorMaterials.COMPOUND_RICE_MEAL, ArmorItem.Type.LEGGINGS, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> COMPOUND_RICE_MEAL_BOOTS = registerItem("compound_rice_meal_boots",
            () -> new ArmorItem(ModArmorMaterials.COMPOUND_RICE_MEAL, ArmorItem.Type.BOOTS, new Item.Properties().stacksTo(1)));

    // ---- 112-120 清洁复合胚件与成品（修复材料75号清洁复合锭） ----
    // 胚件是锻打中间产物（普通材料，堆叠64）
    public static final RegistryObject<Item> CLEANSING_COMPOUND_SWORD_BLANK = registerItem("cleansing_compound_sword_blank",
            () -> new Item(new Item.Properties().stacksTo(64)));

    public static final RegistryObject<Item> CLEANSING_COMPOUND_BOW_BLANK = registerItem("cleansing_compound_bow_blank",
            () -> new Item(new Item.Properties().stacksTo(64)));

    public static final RegistryObject<Item> CLEANSING_COMPOUND_SWORD = registerItem("cleansing_compound_sword",
            () -> new SwordItem(ModItemTiers.CLEANSING_COMPOUND, 4, -2.4F, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> CLEANSING_COMPOUND_BOW = registerItem("cleansing_compound_bow",
            () -> new BowItem(new Item.Properties().durability(1536)));

    public static final RegistryObject<Item> CLEANSING_COMPOUND_ARMOR_BLANK = registerItem("cleansing_compound_armor_blank",
            () -> new Item(new Item.Properties().stacksTo(64)));

    public static final RegistryObject<Item> CLEANSING_COMPOUND_HELMET = registerItem("cleansing_compound_helmet",
            () -> new ArmorItem(ModArmorMaterials.CLEANSING_COMPOUND, ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> CLEANSING_COMPOUND_CHESTPLATE = registerItem("cleansing_compound_chestplate",
            () -> new ArmorItem(ModArmorMaterials.CLEANSING_COMPOUND, ArmorItem.Type.CHESTPLATE, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> CLEANSING_COMPOUND_LEGGINGS = registerItem("cleansing_compound_leggings",
            () -> new ArmorItem(ModArmorMaterials.CLEANSING_COMPOUND, ArmorItem.Type.LEGGINGS, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> CLEANSING_COMPOUND_BOOTS = registerItem("cleansing_compound_boots",
            () -> new ArmorItem(ModArmorMaterials.CLEANSING_COMPOUND, ArmorItem.Type.BOOTS, new Item.Properties().stacksTo(1)));

    // ---- 121-129 超懒合金胚件与终极成品（修复材料80号超懒合金锭） ----
    public static final RegistryObject<Item> SUPER_LAZY_ALLOY_SWORD_BLANK = registerItem("super_lazy_alloy_sword_blank",
            () -> new Item(new Item.Properties().stacksTo(64)));

    public static final RegistryObject<Item> SUPER_LAZY_ALLOY_BOW_BLANK = registerItem("super_lazy_alloy_bow_blank",
            () -> new Item(new Item.Properties().stacksTo(64)));

    public static final RegistryObject<Item> SUPER_LAZY_ALLOY_ARMOR_BLANK = registerItem("super_lazy_alloy_armor_blank",
            () -> new Item(new Item.Properties().stacksTo(64)));

    public static final RegistryObject<Item> SUPER_LAZY_ALLOY_SWORD = registerItem("super_lazy_alloy_sword",
            () -> new SwordItem(ModItemTiers.SUPER_LAZY_ALLOY, 5, -2.4F, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> SUPER_LAZY_ALLOY_BOW = registerItem("super_lazy_alloy_bow",
            () -> new BowItem(new Item.Properties().durability(2048)));

    public static final RegistryObject<Item> SUPER_LAZY_ALLOY_HELMET = registerItem("super_lazy_alloy_helmet",
            () -> new ArmorItem(ModArmorMaterials.SUPER_LAZY_ALLOY, ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> SUPER_LAZY_ALLOY_CHESTPLATE = registerItem("super_lazy_alloy_chestplate",
            () -> new ArmorItem(ModArmorMaterials.SUPER_LAZY_ALLOY, ArmorItem.Type.CHESTPLATE, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> SUPER_LAZY_ALLOY_LEGGINGS = registerItem("super_lazy_alloy_leggings",
            () -> new ArmorItem(ModArmorMaterials.SUPER_LAZY_ALLOY, ArmorItem.Type.LEGGINGS, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> SUPER_LAZY_ALLOY_BOOTS = registerItem("super_lazy_alloy_boots",
            () -> new ArmorItem(ModArmorMaterials.SUPER_LAZY_ALLOY, ArmorItem.Type.BOOTS, new Item.Properties().stacksTo(1)));

    // ===== 130-132：装备锻造台系列方块（金属机器方块，钻石镐采集，破坏掉落本身） =====

    // 编码 130 = sword_forging_table（铸剑台，Sword Forging Table）：放置后对应 ModBlocks.SWORD_FORGING_TABLE
    public static final RegistryObject<Item> SWORD_FORGING_TABLE = registerItem("sword_forging_table",
            () -> new BlockItem(ModBlocks.SWORD_FORGING_TABLE.get(), new Item.Properties()));

    // 编码 131 = armor_forging_table（铸甲台，Armor Forging Table）：放置后对应 ModBlocks.ARMOR_FORGING_TABLE
    public static final RegistryObject<Item> ARMOR_FORGING_TABLE = registerItem("armor_forging_table",
            () -> new BlockItem(ModBlocks.ARMOR_FORGING_TABLE.get(), new Item.Properties()));

    // 编码 132 = bow_forging_table（铸弓台，Bow Forging Table）：放置后对应 ModBlocks.BOW_FORGING_TABLE
    public static final RegistryObject<Item> BOW_FORGING_TABLE = registerItem("bow_forging_table",
            () -> new BlockItem(ModBlocks.BOW_FORGING_TABLE.get(), new Item.Properties()));

    // ===== 133-139：米米之眼/传送器/召唤器系列（普通物品，最大堆叠 64） =====

    // 编码 133 = 米米之眼（Eye of Mimi）
    public static final RegistryObject<Item> MIMI_EYE = registerItem("mimi_eye",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 134 = 米米传送器（Mimi Teleporter）
    public static final RegistryObject<Item> MIMI_TELEPORTER = registerItem("mimi_teleporter",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 135 = 小型召唤器（Small Summoner）
    public static final RegistryObject<Item> SMALL_SUMMONER = registerItem("small_summoner",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 136 = 待定占位：不进创造标签页（ModCreativeTabs 过滤 placeholder_ 前缀），仅锁定编码
    public static final RegistryObject<Item> PLACEHOLDER_136 = registerItem("placeholder_136",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 137 = 中型召唤器（Medium Summoner）
    public static final RegistryObject<Item> MEDIUM_SUMMONER = registerItem("medium_summoner",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 138 = 大型召唤器（Large Summoner）
    public static final RegistryObject<Item> LARGE_SUMMONER = registerItem("large_summoner",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 139 = 最懒召唤器（Laziest Summoner）
    public static final RegistryObject<Item> LAZIEST_SUMMONER = registerItem("laziest_summoner",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // ===== 140-145：召唤核心三级链（胚 → 粗制胚 → 核心），普通物品堆叠 64 =====

    // 编码 140 = 召唤核心胚
    public static final RegistryObject<Item> SUMMON_CORE_BLANK = registerItem("summon_core_blank",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 141 = 粗制召唤核心胚
    public static final RegistryObject<Item> COARSE_SUMMON_CORE_BLANK = registerItem("coarse_summon_core_blank",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 142 = 召唤核心
    public static final RegistryObject<Item> SUMMON_CORE = registerItem("summon_core",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 143 = 高级召唤核心胚
    public static final RegistryObject<Item> ADVANCED_SUMMON_CORE_BLANK = registerItem("advanced_summon_core_blank",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 144 = 粗制高级召唤核心胚
    public static final RegistryObject<Item> COARSE_ADVANCED_SUMMON_CORE_BLANK =
            registerItem("coarse_advanced_summon_core_blank",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 145 = 高级召唤核心
    public static final RegistryObject<Item> ADVANCED_SUMMON_CORE = registerItem("advanced_summon_core",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // ===== 146-147：混合结晶加工中间体 =====

    // 编码 146 = 混合结晶（Mixed Crystal）
    public static final RegistryObject<Item> MIXED_CRYSTAL = registerItem("mixed_crystal",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 147 = 熔融混合结晶（Molten Mixed Crystal）
    public static final RegistryObject<Item> MOLTEN_MIXED_CRYSTAL = registerItem("molten_mixed_crystal",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // ===== 148-150：最懒召唤核心三级链 =====

    // 编码 148 = 最懒召唤核心胚
    public static final RegistryObject<Item> LAZIEST_SUMMON_CORE_BLANK = registerItem("laziest_summon_core_blank",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 149 = 粗制最懒召唤核心胚
    public static final RegistryObject<Item> COARSE_LAZIEST_SUMMON_CORE_BLANK =
            registerItem("coarse_laziest_summon_core_blank",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 150 = 最懒召唤核心（手稿 150 与 148 同名，按三级链判定为最终核心，详见编码表注释）
    public static final RegistryObject<Item> LAZIEST_SUMMON_CORE = registerItem("laziest_summon_core",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // ===== 151-157：七大 Boss 生物蛋（ForgeSpawnEggItem；实体当前为 PendingBossEntity 占位，后续替换真实 Boss） =====

    // 编码 151 = 米米猴王生物蛋：棕底/米黄斑点
    public static final RegistryObject<Item> MIMI_MONKEY_KING_SPAWN_EGG =
            registerItem("mimi_monkey_king_spawn_egg", () -> new ForgeSpawnEggItem(
                    ModEntities.MIMI_MONKEY_KING, 0x8C5A2B, 0xE8C26A, new Item.Properties()));

    // 编码 152 = 饭晶虎王生物蛋：米白底/金斑
    public static final RegistryObject<Item> RICE_CRYSTAL_TIGER_KING_SPAWN_EGG =
            registerItem("rice_crystal_tiger_king_spawn_egg", () -> new ForgeSpawnEggItem(
                    ModEntities.RICE_CRYSTAL_TIGER_KING, 0xEDE2C8, 0xC98A2E, new Item.Properties()));

    // 编码 153 = 超晶牛王生物蛋：深蓝底/亮蓝斑
    public static final RegistryObject<Item> SUPER_CRYSTAL_COW_KING_SPAWN_EGG =
            registerItem("super_crystal_cow_king_spawn_egg", () -> new ForgeSpawnEggItem(
                    ModEntities.SUPER_CRYSTAL_COW_KING, 0x2E4A80, 0x7FB6E8, new Item.Properties()));

    // 编码 154 = 重晶兔王生物蛋：紫底/浅紫斑
    public static final RegistryObject<Item> HEAVY_CRYSTAL_RABBIT_KING_SPAWN_EGG =
            registerItem("heavy_crystal_rabbit_king_spawn_egg", () -> new ForgeSpawnEggItem(
                    ModEntities.HEAVY_CRYSTAL_RABBIT_KING, 0x6E4E96, 0xD3B8F0, new Item.Properties()));

    // 编码 155 = 复晶天青蟒生物蛋：青底/薄荷斑
    public static final RegistryObject<Item> COMPOUND_AZURE_PYTHON_SPAWN_EGG =
            registerItem("compound_azure_python_spawn_egg", () -> new ForgeSpawnEggItem(
                    ModEntities.COMPOUND_AZURE_PYTHON, 0x1F7A72, 0x8FE0D2, new Item.Properties()));

    // 编码 156 = 清洁长命夔生物蛋：青白底/天青斑
    public static final RegistryObject<Item> CLEANSING_LONGEVITY_KUI_SPAWN_EGG =
            registerItem("cleansing_longevity_kui_spawn_egg", () -> new ForgeSpawnEggItem(
                    ModEntities.CLEANSING_LONGEVITY_KUI, 0xCFEAF2, 0x3FA0BC, new Item.Properties()));

    // 编码 157 = 最懒终末龙生物蛋：暗紫底/幻紫斑
    public static final RegistryObject<Item> LAZIEST_FINAL_DRAGON_SPAWN_EGG =
            registerItem("laziest_final_dragon_spawn_egg", () -> new ForgeSpawnEggItem(
                    ModEntities.LAZIEST_FINAL_DRAGON, 0x2E1E4A, 0xA974E0, new Item.Properties()));

    // 编码 158 = 待定占位：不进创造标签页，仅锁定编码
    public static final RegistryObject<Item> PLACEHOLDER_158 = registerItem("placeholder_158",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // ===== 159-163：米米神翼系列 + 上古剑意 + 超懒神剑残片 =====

    // 编码 159 = 米米神翼（Mimi Divine Wings）：胸甲装备，MIMI_WINGS 材质
    public static final RegistryObject<Item> MIMI_DIVINE_WINGS = registerItem("mimi_divine_wings",
            () -> new ArmorItem(ModArmorMaterials.MIMI_WINGS, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().stacksTo(1)));

    // 编码 160 = 米米幻翼膜（Mimi Phantom Membrane）：神翼修复材料，普通物品
    public static final RegistryObject<Item> MIMI_PHANTOM_MEMBRANE = registerItem("mimi_phantom_membrane",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 161 = 超懒神翼胸甲（Super Lazy Divine Wings Chestplate）：终极翼甲，SUPER_LAZY_WINGS 材质
    public static final RegistryObject<Item> SUPER_LAZY_WINGS_CHESTPLATE =
            registerItem("super_lazy_wings_chestplate",
                    () -> new ArmorItem(ModArmorMaterials.SUPER_LAZY_WINGS, ArmorItem.Type.CHESTPLATE,
                            new Item.Properties().stacksTo(1)));

    // 编码 162 = 上古剑意（Ancient Sword Intent）：普通物品
    public static final RegistryObject<Item> ANCIENT_SWORD_INTENT = registerItem("ancient_sword_intent",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 163 = 超懒神剑·残缺·壹（Super Lazy Divine Sword - Broken I）：普通物品【非装备】，
    // 最大堆叠 1；RainbowNameItem 覆盖显示名，逐字动态彩虹渐变
    public static final RegistryObject<Item> SUPER_LAZY_DIVINE_SWORD_BROKEN_1 =
            registerItem("super_lazy_divine_sword_broken_1",
                    () -> new RainbowNameItem(new Item.Properties().stacksTo(1)));

    // 编码 164 = 淬剑台（Sword Quenching Table）：放置后对应 ModBlocks.SWORD_QUENCHING_TABLE
    public static final RegistryObject<Item> SWORD_QUENCHING_TABLE = registerItem("sword_quenching_table",
            () -> new BlockItem(ModBlocks.SWORD_QUENCHING_TABLE.get(), new Item.Properties()));

    // 编码 165 = 超懒神剑·残缺·贰（Super Lazy Divine Sword - Broken II）：普通物品【非装备】，
    // 最大堆叠 1；RainbowNameItem 覆盖显示名，逐字动态彩虹渐变（与163残缺·壹同款）
    public static final RegistryObject<Item> SUPER_LAZY_DIVINE_SWORD_BROKEN_2 =
            registerItem("super_lazy_divine_sword_broken_2",
                    () -> new RainbowNameItem(new Item.Properties().stacksTo(1)));

    // 编码 166 = 汲血台（Bloodletting Table）：放置后对应 ModBlocks.BLOODLETTING_TABLE
    public static final RegistryObject<Item> BLOODLETTING_TABLE = registerItem("bloodletting_table",
            () -> new BlockItem(ModBlocks.BLOODLETTING_TABLE.get(), new Item.Properties()));

    // ===== 167-168：血瓶 / 淬剑液（普通物品，最大堆叠 64） =====

    // 编码 167 = 血瓶（Blood Vial）
    public static final RegistryObject<Item> BLOOD_VIAL = registerItem("blood_vial",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 168 = 淬剑液（Sword Quenching Liquid）
    public static final RegistryObject<Item> SWORD_QUENCHING_LIQUID = registerItem("sword_quenching_liquid",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // ===== 169-172：剑魄与超懒神剑·残次/赝品 =====

    // 编码 169 = 远古剑魄（Ancient Sword Soul）
    public static final RegistryObject<Item> ANCIENT_SWORD_SOUL = registerItem("ancient_sword_soul",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 170 = 超懒神剑·残次：普通物品【非装备】，堆叠 1，动态彩虹渐变名称
    public static final RegistryObject<Item> SUPER_LAZY_DIVINE_SWORD_DEFECTIVE =
            registerItem("super_lazy_divine_sword_defective",
                    () -> new RainbowNameItem(new Item.Properties().stacksTo(1)));

    // 编码 171 = 太古剑魄（Primordial Sword Soul）
    public static final RegistryObject<Item> PRIMORDIAL_SWORD_SOUL = registerItem("primordial_sword_soul",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 172 = 超懒神剑·赝品：普通物品【非装备】，堆叠 1，动态彩虹渐变名称
    public static final RegistryObject<Item> SUPER_LAZY_DIVINE_SWORD_COUNTERFEIT =
            registerItem("super_lazy_divine_sword_counterfeit",
                    () -> new RainbowNameItem(new Item.Properties().stacksTo(1)));

    // ===== 173-176：神玉系列与超懒神剑·瑕疵 =====

    // 编码 173 = 神玉（Divine Jade）
    public static final RegistryObject<Item> DIVINE_JADE = registerItem("divine_jade",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 174 = 金米玉（Golden Rice Jade）
    public static final RegistryObject<Item> GOLDEN_RICE_JADE = registerItem("golden_rice_jade",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 175 = 神剑鉴玉（Divine Sword Appraisal Jade）
    public static final RegistryObject<Item> DIVINE_SWORD_APPRAISAL_JADE =
            registerItem("divine_sword_appraisal_jade",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 176 = 超懒神剑·瑕疵：普通物品【非装备】，堆叠 1，动态彩虹渐变名称
    public static final RegistryObject<Item> SUPER_LAZY_DIVINE_SWORD_FLAWED =
            registerItem("super_lazy_divine_sword_flawed",
                    () -> new RainbowNameItem(new Item.Properties().stacksTo(1)));

    // ===== 177-181：剑谱系列（178/179/181 为彩虹名堆叠1） =====

    // 编码 177 = 剑法禁卷（Forbidden Sword Manual）
    public static final RegistryObject<Item> FORBIDDEN_SWORD_MANUAL = registerItem("forbidden_sword_manual",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 178 = 超懒剑法：普通物品【非装备】，堆叠 1，动态彩虹渐变名称
    public static final RegistryObject<Item> SUPER_LAZY_SWORD_ART = registerItem("super_lazy_sword_art",
            () -> new RainbowNameItem(new Item.Properties().stacksTo(1)));

    // 编码 179 = 超懒神剑·完整：普通物品【非装备】，堆叠 1，动态彩虹渐变名称
    public static final RegistryObject<Item> SUPER_LAZY_DIVINE_SWORD_COMPLETE =
            registerItem("super_lazy_divine_sword_complete",
                    () -> new RainbowNameItem(new Item.Properties().stacksTo(1)));

    // 编码 180 = 结晶米玉（Crystallized Rice Jade）
    public static final RegistryObject<Item> CRYSTALLIZED_RICE_JADE = registerItem("crystallized_rice_jade",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 181 = 终末剑诀：普通物品【非装备】，堆叠 1，动态彩虹渐变名称
    public static final RegistryObject<Item> FINAL_SWORD_CODEX = registerItem("final_sword_codex",
            () -> new RainbowNameItem(new Item.Properties().stacksTo(1)));

    // ===== 182-187：天堕/堕落系列 =====

    // 编码 182 = 最懒天堕剑·赝品：普通物品【非装备】，堆叠 1，动态彩虹渐变名称
    public static final RegistryObject<Item> LAZIEST_HEAVENFALL_SWORD_COUNTERFEIT =
            registerItem("laziest_heavenfall_sword_counterfeit",
                    () -> new RainbowNameItem(new Item.Properties().stacksTo(1)));

    // 编码 183 = 破败之心（Ruined Heart）
    public static final RegistryObject<Item> RUINED_HEART = registerItem("ruined_heart",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 184 = 堕落之心（Fallen Heart）：HEAVENFALL 盔甲的修复材料
    public static final RegistryObject<Item> FALLEN_HEART = registerItem("fallen_heart",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 185 = 堕落王冠（Fallen Crown）
    public static final RegistryObject<Item> FALLEN_CROWN = registerItem("fallen_crown",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 186 = 七宗堕落（Seven Deadly Falls）
    public static final RegistryObject<Item> SEVEN_DEADLY_FALLS = registerItem("seven_deadly_falls",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 187 = 最懒天堕剑·原罪：普通物品【非装备】，堆叠 1，动态彩虹渐变名称
    public static final RegistryObject<Item> LAZIEST_HEAVENFALL_SWORD_ORIGINAL_SIN =
            registerItem("laziest_heavenfall_sword_original_sin",
                    () -> new RainbowNameItem(new Item.Properties().stacksTo(1)));

    // ===== 188-191：最懒天堕盔甲四件套（HEAVENFALL 材质，全部 stacksTo 1） =====

    // 编码 188 = 天堕翼胸甲：CHESTPLATE
    public static final RegistryObject<Item> HEAVENFALL_WINGS_CHESTPLATE =
            registerItem("heavenfall_wings_chestplate",
                    () -> new ArmorItem(ModArmorMaterials.HEAVENFALL, ArmorItem.Type.CHESTPLATE,
                            new Item.Properties().stacksTo(1)));

    // 编码 189 = 最懒天堕头盔：HELMET
    public static final RegistryObject<Item> LAZIEST_HEAVENFALL_HELMET =
            registerItem("laziest_heavenfall_helmet",
                    () -> new ArmorItem(ModArmorMaterials.HEAVENFALL, ArmorItem.Type.HELMET,
                            new Item.Properties().stacksTo(1)));

    // 编码 190 = 最懒天堕护腿：LEGGINGS
    public static final RegistryObject<Item> LAZIEST_HEAVENFALL_LEGGINGS =
            registerItem("laziest_heavenfall_leggings",
                    () -> new ArmorItem(ModArmorMaterials.HEAVENFALL, ArmorItem.Type.LEGGINGS,
                            new Item.Properties().stacksTo(1)));

    // 编码 191 = 最懒天堕靴子：BOOTS
    public static final RegistryObject<Item> LAZIEST_HEAVENFALL_BOOTS =
            registerItem("laziest_heavenfall_boots",
                    () -> new ArmorItem(ModArmorMaterials.HEAVENFALL, ArmorItem.Type.BOOTS,
                            new Item.Properties().stacksTo(1)));

    // ===== 192-200：米米系列肉鱼食物（全部食用时长 1.5s = 30t，nutrition 单位=半个鸡腿） =====

    // 编码 192 = 熟咧米鸡肉（Cooked Liemi Chicken）：2.5 饱食度(nutrition=5)；1% 中毒 10s + 50% 反胃 5s
    public static final FoodProperties COOKED_LIEMI_CHICKEN_FOOD = new FoodProperties.Builder()
            .nutrition(5)
            .saturationMod(0.3F)
            .effect(() -> new MobEffectInstance(MobEffects.POISON, 200, 0), 0.01F)
            .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 100, 0), 0.5F)
            .build();

    public static final RegistryObject<Item> COOKED_LIEMI_CHICKEN = registerItem("cooked_liemi_chicken",
            () -> new Item(new Item.Properties().stacksTo(64).food(COOKED_LIEMI_CHICKEN_FOOD)) {
                @Override
                public int getUseDuration(ItemStack stack) {
                    return 30; // 食用时间 1.5 秒
                }
            });

    // 编码 193 = 米吽牛肉（Raw Mimoo Beef）：1 饱食度(nutrition=2)；100% 反胃 20s + 100% 饥饿 30s + 5% 中毒 25s
    public static final FoodProperties RAW_MIMOO_BEEF_FOOD = new FoodProperties.Builder()
            .nutrition(2)
            .saturationMod(0.3F)
            .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 400, 0), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.HUNGER, 600, 0), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.POISON, 500, 0), 0.05F)
            .build();

    public static final RegistryObject<Item> RAW_MIMOO_BEEF = registerItem("raw_mimoo_beef",
            () -> new Item(new Item.Properties().stacksTo(64).food(RAW_MIMOO_BEEF_FOOD)) {
                @Override
                public int getUseDuration(ItemStack stack) {
                    return 30;
                }
            });

    // 编码 194 = 熟米吽牛肉（Cooked Mimoo Beef）：4 饱食度(nutrition=8)；100% 缓慢 2s + 25% 反胃 10s
    public static final FoodProperties COOKED_MIMOO_BEEF_FOOD = new FoodProperties.Builder()
            .nutrition(8)
            .saturationMod(0.3F)
            .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 200, 0), 0.25F)
            .build();

    public static final RegistryObject<Item> COOKED_MIMOO_BEEF = registerItem("cooked_mimoo_beef",
            () -> new Item(new Item.Properties().stacksTo(64).food(COOKED_MIMOO_BEEF_FOOD)) {
                @Override
                public int getUseDuration(ItemStack stack) {
                    return 30;
                }
            });

    // 编码 195 = 米羊羊肉（Raw Mimi Mutton）：1 饱食度(nutrition=2)；50% 虚弱 10s + 70% 饥饿 20s + 100% 反胃 10s
    public static final FoodProperties RAW_MIMI_MUTTON_FOOD = new FoodProperties.Builder()
            .nutrition(2)
            .saturationMod(0.3F)
            .effect(() -> new MobEffectInstance(MobEffects.WEAKNESS, 200, 0), 0.5F)
            .effect(() -> new MobEffectInstance(MobEffects.HUNGER, 400, 0), 0.7F)
            .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 200, 0), 1.0F)
            .build();

    public static final RegistryObject<Item> RAW_MIMI_MUTTON = registerItem("raw_mimi_mutton",
            () -> new Item(new Item.Properties().stacksTo(64).food(RAW_MIMI_MUTTON_FOOD)) {
                @Override
                public int getUseDuration(ItemStack stack) {
                    return 30;
                }
            });

    // 编码 196 = 熟米羊羊肉（Cooked Mimi Mutton）：3 饱食度(nutrition=6)；10% 反胃 5s + 1% 虚弱 2s
    public static final FoodProperties COOKED_MIMI_MUTTON_FOOD = new FoodProperties.Builder()
            .nutrition(6)
            .saturationMod(0.3F)
            .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 100, 0), 0.1F)
            .effect(() -> new MobEffectInstance(MobEffects.WEAKNESS, 40, 0), 0.01F)
            .build();

    public static final RegistryObject<Item> COOKED_MIMI_MUTTON = registerItem("cooked_mimi_mutton",
            () -> new Item(new Item.Properties().stacksTo(64).food(COOKED_MIMI_MUTTON_FOOD)) {
                @Override
                public int getUseDuration(ItemStack stack) {
                    return 30;
                }
            });

    // 编码 197 = 米哼猪肉（Raw Miheng Pork）：2 饱食度(nutrition=4)；
    // 100% 失明 10s + 100% 虚弱 10s + 100% 缓慢 10s + 100% 中毒 10s + 100% 反胃 10s
    public static final FoodProperties RAW_MIHENG_PORK_FOOD = new FoodProperties.Builder()
            .nutrition(4)
            .saturationMod(0.3F)
            .effect(() -> new MobEffectInstance(MobEffects.BLINDNESS, 200, 0), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.WEAKNESS, 200, 0), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 0), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.POISON, 200, 0), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 200, 0), 1.0F)
            .build();

    public static final RegistryObject<Item> RAW_MIHENG_PORK = registerItem("raw_miheng_pork",
            () -> new Item(new Item.Properties().stacksTo(64).food(RAW_MIHENG_PORK_FOOD)) {
                @Override
                public int getUseDuration(ItemStack stack) {
                    return 30;
                }
            });

    // 编码 198 = 熟米哼猪肉（Cooked Miheng Pork）：6 饱食度(nutrition=12)；
    // 100% 失明 10s + 100% 虚弱 10s + 50% 缓慢 10s + 50% 反胃 10s
    public static final FoodProperties COOKED_MIHENG_PORK_FOOD = new FoodProperties.Builder()
            .nutrition(12)
            .saturationMod(0.3F)
            .effect(() -> new MobEffectInstance(MobEffects.BLINDNESS, 200, 0), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.WEAKNESS, 200, 0), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 0), 0.5F)
            .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 200, 0), 0.5F)
            .build();

    public static final RegistryObject<Item> COOKED_MIHENG_PORK = registerItem("cooked_miheng_pork",
            () -> new Item(new Item.Properties().stacksTo(64).food(COOKED_MIHENG_PORK_FOOD)) {
                @Override
                public int getUseDuration(ItemStack stack) {
                    return 30;
                }
            });

    // 编码 199 = 贪米鱼肉（Raw Tanmi Fish）：0.5 饱食度(nutrition=1)；100% 缓慢 10s
    public static final FoodProperties RAW_TANMI_FISH_FOOD = new FoodProperties.Builder()
            .nutrition(1)
            .saturationMod(0.3F)
            .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 0), 1.0F)
            .build();

    public static final RegistryObject<Item> RAW_TANMI_FISH = registerItem("raw_tanmi_fish",
            () -> new Item(new Item.Properties().stacksTo(64).food(RAW_TANMI_FISH_FOOD)) {
                @Override
                public int getUseDuration(ItemStack stack) {
                    return 30;
                }
            });

    // 编码 200 = 熟贪米鱼肉（Cooked Tanmi Fish）：3 饱食度(nutrition=6)；100% 缓慢 5s + 100% 虚弱 10s
    public static final FoodProperties COOKED_TANMI_FISH_FOOD = new FoodProperties.Builder()
            .nutrition(6)
            .saturationMod(0.3F)
            .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 0), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.WEAKNESS, 200, 0), 1.0F)
            .build();

    public static final RegistryObject<Item> COOKED_TANMI_FISH = registerItem("cooked_tanmi_fish",
            () -> new Item(new Item.Properties().stacksTo(64).food(COOKED_TANMI_FISH_FOOD)) {
                @Override
                public int getUseDuration(ItemStack stack) {
                    return 30;
                }
            });

    // ===== 201-214：米米生物掉落材料（均为普通物品，最大堆叠 64，无食用属性） =====

    // 编码 201 = 啄米羽毛（Zhuomi Feather）
    public static final RegistryObject<Item> ZHUOMI_FEATHER = registerItem("zhuomi_feather",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 202 = 米哞皮革（Mimoo Leather）
    public static final RegistryObject<Item> MIMOO_LEATHER = registerItem("mimoo_leather",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 203 = 米羊羊毛（Mimi Wool）
    public static final RegistryObject<Item> MIMI_WOOL = registerItem("mimi_wool",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 204 = 米哼猪皮（Miheng Hide）
    public static final RegistryObject<Item> MIHENG_HIDE = registerItem("miheng_hide",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 205 = 喷米鱼囊（Penmi Fish Sac）
    public static final RegistryObject<Item> PENMI_FISH_SAC = registerItem("penmi_fish_sac",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 206 = 嗥米狼心（Haomi Wolf Heart）
    public static final RegistryObject<Item> HAOMI_WOLF_HEART = registerItem("haomi_wolf_heart",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 207 = 米骨（Rice Bone）
    public static final RegistryObject<Item> RICE_BONE = registerItem("rice_bone",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 208 = 米箭（Rice Arrow）
    public static final RegistryObject<Item> RICE_ARROW = registerItem("rice_arrow",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 209 = 火米药（Huomi Powder）
    public static final RegistryObject<Item> HUOMI_POWDER = registerItem("huomi_powder",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 210 = 眼米珍珠（Yanmi Pearl）
    public static final RegistryObject<Item> YANMI_PEARL = registerItem("yanmi_pearl",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 211 = 爆烈米棒（Baoliemi Rod）
    public static final RegistryObject<Item> BAOLIEMI_ROD = registerItem("baoliemi_rod",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 212 = 雕米骨（Carved Rice Bone）
    public static final RegistryObject<Item> CARVED_RICE_BONE = registerItem("carved_rice_bone",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 213 = 米莱马球（Milaima Ball）
    public static final RegistryObject<Item> MILAIMA_BALL = registerItem("milaima_ball",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 214 = 黏米丝线（Nianmi Silk Thread）
    public static final RegistryObject<Item> NIANMI_SILK_THREAD = registerItem("nianmi_silk_thread",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 215 = 腐米肉（Rotten Rice Flesh）：1 饱食度(nutrition=2)，食用时长 1.5s(30t)；
    // 100% 中毒 5s + 100% 饥饿 30s + 100% 缓慢 10s + 1% 中毒 60s（与 5s 中毒同等级，触发时按原版规则取较长 60s）
    public static final FoodProperties ROTTEN_RICE_FLESH_FOOD = new FoodProperties.Builder()
            .nutrition(2)
            .saturationMod(0.1F)
            .effect(() -> new MobEffectInstance(MobEffects.POISON, 100, 0), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.HUNGER, 600, 0), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 0), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.POISON, 1200, 0), 0.01F)
            .build();

    public static final RegistryObject<Item> ROTTEN_RICE_FLESH = registerItem("rotten_rice_flesh",
            () -> new Item(new Item.Properties().stacksTo(64).food(ROTTEN_RICE_FLESH_FOOD)) {
                @Override
                public int getUseDuration(ItemStack stack) {
                    return 30; // 食用时间 1.5 秒
                }
            });

    // ===== 216-226：米米加工材料/液体（均为普通物品，最大堆叠 64；222-226 液体为药水瓶外形的非流体物品） =====

    // 编码 216 = 米米大杂烩（Mimi Medley）
    public static final RegistryObject<Item> MIMI_MEDLEY = registerItem("mimi_medley",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 217 = 米骨粉（Rice Bone Meal）
    public static final RegistryObject<Item> RICE_BONE_MEAL = registerItem("rice_bone_meal",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 218 = 米精（Rice Essence）
    public static final RegistryObject<Item> RICE_ESSENCE = registerItem("rice_essence",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 219 = 粗米（Coarse Rice）
    public static final RegistryObject<Item> COARSE_RICE = registerItem("coarse_rice",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 220 = 米笔（Rice Brush Pen）
    public static final RegistryObject<Item> RICE_BRUSH_PEN = registerItem("rice_brush_pen",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 221 = 书米笔（Book Rice Pen）
    public static final RegistryObject<Item> BOOK_RICE_PEN = registerItem("book_rice_pen",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 222 = 米果黑混液（Rice Fruit Black Mixture）
    public static final RegistryObject<Item> RICE_FRUIT_BLACK_MIXTURE = registerItem("rice_fruit_black_mixture",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 223 = 米果混黏液（Rice Fruit Viscous Mixture）
    public static final RegistryObject<Item> RICE_FRUIT_VISCOUS_MIXTURE = registerItem("rice_fruit_viscous_mixture",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 224 = 爆米绽放液（Poprice Bloom Liquid）
    public static final RegistryObject<Item> POPRICE_BLOOM_LIQUID = registerItem("poprice_bloom_liquid",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 225 = 眼米嘬心液（Yanmi Heart-Sip Liquid）
    public static final RegistryObject<Item> YANMI_HEART_SIP_LIQUID = registerItem("yanmi_heart_sip_liquid",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 226 = 米米杂烩液（Mimi Medley Liquid）
    public static final RegistryObject<Item> MIMI_MEDLEY_LIQUID = registerItem("mimi_medley_liquid",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // ===== 227-240：BOSS/神兽掉落（均为普通物品，最大堆叠 64；"头/棍/爪/角/牙/鳞"仅为同名掉落物，无装备/武器功能） =====

    // 编码 227 = 猴王如意棍（Monkey King Ruyi Staff）
    public static final RegistryObject<Item> MONKEY_KING_RUYI_STAFF = registerItem("monkey_king_ruyi_staff",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 228 = 米米猴头（Mimi Monkey Head）
    public static final RegistryObject<Item> MIMI_MONKEY_HEAD = registerItem("mimi_monkey_head",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 229 = 虎王利爪（Tiger King Razor Claw）
    public static final RegistryObject<Item> TIGER_KING_RAZOR_CLAW = registerItem("tiger_king_razor_claw",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 230 = 饭晶虎王头（Rice Crystal Tiger King Head）
    public static final RegistryObject<Item> RICE_CRYSTAL_TIGER_KING_HEAD = registerItem("rice_crystal_tiger_king_head",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 231 = 牛王晶角（Ox King Crystal Horn）
    public static final RegistryObject<Item> OX_KING_CRYSTAL_HORN = registerItem("ox_king_crystal_horn",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 232 = 超晶牛头（Super Crystal Ox Head）
    public static final RegistryObject<Item> SUPER_CRYSTAL_OX_HEAD = registerItem("super_crystal_ox_head",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 233 = 兔王腥红眼（Rabbit King Bloody Red Eye）
    public static final RegistryObject<Item> RABBIT_KING_BLOODY_RED_EYE = registerItem("rabbit_king_bloody_red_eye",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 234 = 重晶兔王头（Heavy Crystal Rabbit King Head）
    public static final RegistryObject<Item> HEAVY_CRYSTAL_RABBIT_KING_HEAD = registerItem("heavy_crystal_rabbit_king_head",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 235 = 极毒青蟒牙（Venom Azure Python Fang）
    public static final RegistryObject<Item> VENOM_AZURE_PYTHON_FANG = registerItem("venom_azure_python_fang",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 236 = 复晶天青蟒头（Compound Crystal Azure Python Head）
    public static final RegistryObject<Item> COMPOUND_CRYSTAL_AZURE_PYTHON_HEAD = registerItem("compound_crystal_azure_python_head",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 237 = 恶夔长命角（Evil Kui Longevity Horn）
    public static final RegistryObject<Item> EVIL_KUI_LONGEVITY_HORN = registerItem("evil_kui_longevity_horn",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 238 = 清洁长命夔头（Purified Longevity Kui Head）
    public static final RegistryObject<Item> PURIFIED_LONGEVITY_KUI_HEAD = registerItem("purified_longevity_kui_head",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 239 = 终末龙逆鳞（End Dragon Reverse Scale）
    public static final RegistryObject<Item> END_DRAGON_REVERSE_SCALE = registerItem("end_dragon_reverse_scale",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 240 = 最懒终末龙头（Laziest End Dragon Head）
    public static final RegistryObject<Item> LAZIEST_END_DRAGON_HEAD = registerItem("laziest_end_dragon_head",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // ===== 241-245：爆米嘬心液 + 四档镐头部件（均为普通物品，最大堆叠 64） =====

    // 编码 241 = 爆米嘬心液（Poprice Heart-Sip Liquid）：药水瓶外形的非流体物品
    public static final RegistryObject<Item> POPRICE_HEART_SIP_LIQUID = registerItem("poprice_heart_sip_liquid",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 242 = 超重结晶镐头（Super Heavy Crystal Pickaxe Head）
    public static final RegistryObject<Item> SUPER_HEAVY_CRYSTAL_PICKAXE_HEAD =
            registerItem("super_heavy_crystal_pickaxe_head",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 243 = 复合米饭镐头（Compound Rice Meal Pickaxe Head）
    public static final RegistryObject<Item> COMPOUND_RICE_MEAL_PICKAXE_HEAD =
            registerItem("compound_rice_meal_pickaxe_head",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 244 = 清洁复合镐头（Cleansing Compound Pickaxe Head）
    public static final RegistryObject<Item> CLEANSING_COMPOUND_PICKAXE_HEAD =
            registerItem("cleansing_compound_pickaxe_head",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 245 = 超懒合金镐头（Super Lazy Alloy Pickaxe Head）
    public static final RegistryObject<Item> SUPER_LAZY_ALLOY_PICKAXE_HEAD =
            registerItem("super_lazy_alloy_pickaxe_head",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 246 = 咧米鸡肉（Liemi Chicken）：0.5 饱食度(nutrition=1)，食用时长 1.5s(30t)；
    // 10% 中毒 20s + 90% 饥饿 30s + 100% 反胃 15s
    public static final FoodProperties LIEMI_CHICKEN_FOOD = new FoodProperties.Builder()
            .nutrition(1)
            .saturationMod(0.1F)
            .effect(() -> new MobEffectInstance(MobEffects.POISON, 400, 0), 0.1F)
            .effect(() -> new MobEffectInstance(MobEffects.HUNGER, 600, 0), 0.9F)
            .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 300, 0), 1.0F)
            .build();

    public static final RegistryObject<Item> LIEMI_CHICKEN = registerItem("liemi_chicken",
            () -> new Item(new Item.Properties().stacksTo(64).food(LIEMI_CHICKEN_FOOD)) {
                @Override
                public int getUseDuration(ItemStack stack) {
                    return 30; // 食用时间 1.5 秒
                }
            });

    // ===== 247-250：四档棒（手柄部件，均为普通物品，最大堆叠 64） =====

    // 编码 247 = 超重结晶棒（Super Heavy Crystal Rod）
    public static final RegistryObject<Item> SUPER_HEAVY_CRYSTAL_ROD = registerItem("super_heavy_crystal_rod",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 248 = 复合米饭棒（Compound Rice Meal Rod）
    public static final RegistryObject<Item> COMPOUND_RICE_MEAL_ROD = registerItem("compound_rice_meal_rod",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 249 = 清洁复合棒（Cleansing Compound Rod）
    public static final RegistryObject<Item> CLEANSING_COMPOUND_ROD = registerItem("cleansing_compound_rod",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 250 = 超懒合金棒（Super Lazy Alloy Rod）
    public static final RegistryObject<Item> SUPER_LAZY_ALLOY_ROD = registerItem("super_lazy_alloy_rod",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // ===== 251-255：五档镐（PickaxeItem 工具类，最大堆叠 1；镐头+棒合成的成品） =====

    // 编码 251 = 超重结晶镐（Super Heavy Crystal Pickaxe）
    public static final RegistryObject<Item> SUPER_HEAVY_CRYSTAL_PICKAXE =
            registerItem("super_heavy_crystal_pickaxe",
                    () -> new PickaxeItem(ModItemTiers.SUPER_HEAVY_CRYSTAL, 1, -2.8F,
                            new Item.Properties().stacksTo(1)));

    // 编码 252 = 复合米饭镐（Compound Rice Meal Pickaxe）
    public static final RegistryObject<Item> COMPOUND_RICE_MEAL_PICKAXE =
            registerItem("compound_rice_meal_pickaxe",
                    () -> new PickaxeItem(ModItemTiers.COMPOUND_RICE_MEAL, 1, -2.8F,
                            new Item.Properties().stacksTo(1)));

    // 编码 253 = 清洁复合镐（Cleansing Compound Pickaxe）
    public static final RegistryObject<Item> CLEANSING_COMPOUND_PICKAXE =
            registerItem("cleansing_compound_pickaxe",
                    () -> new PickaxeItem(ModItemTiers.CLEANSING_COMPOUND, 1, -2.8F,
                            new Item.Properties().stacksTo(1)));

    // 编码 254 = 超懒合金镐（Super Lazy Alloy Pickaxe）
    public static final RegistryObject<Item> SUPER_LAZY_ALLOY_PICKAXE =
            registerItem("super_lazy_alloy_pickaxe",
                    () -> new PickaxeItem(ModItemTiers.SUPER_LAZY_ALLOY, 1, -2.8F,
                            new Item.Properties().stacksTo(1)));

    // 编码 255 = 最懒天堕镐（Laziest Heavenfall Pickaxe）：传说级，堕落之心修复
    public static final RegistryObject<Item> LAZIEST_HEAVENFALL_PICKAXE =
            registerItem("laziest_heavenfall_pickaxe",
                    () -> new PickaxeItem(ModItemTiers.LAZIEST_HEAVENFALL, 1, -2.8F,
                            new Item.Properties().stacksTo(1)));

    // ===== 256-259：盗米者系列 + 米魂残天（均为普通物品，最大堆叠 64；257 除外为生物蛋） =====

    // 编码 256 = 盗米之心（Rice Stealer Heart）
    public static final RegistryObject<Item> RICE_STEALER_HEART = registerItem("rice_stealer_heart",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 257 = 盗米者生物蛋：米白底/贼墨黑斑（实体 rice_stealer 当前为 PendingBossEntity 占位，后续替换真实生物）
    public static final RegistryObject<Item> RICE_STEALER_SPAWN_EGG =
            registerItem("rice_stealer_spawn_egg", () -> new ForgeSpawnEggItem(
                    ModEntities.RICE_STEALER, 0xEDE6D2, 0x3A3A4A, new Item.Properties()));

    // 编码 258 = 米魂之泪（Rice Soul Tear）
    public static final RegistryObject<Item> RICE_SOUL_TEAR = registerItem("rice_soul_tear",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 259 = 残天翼（Remnant Heaven Wing）
    public static final RegistryObject<Item> REMNANT_HEAVEN_WING = registerItem("remnant_heaven_wing",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 260 = 待定占位：不进创造标签页（ModCreativeTabs 过滤 placeholder_ 前缀），仅锁定编码
    public static final RegistryObject<Item> PLACEHOLDER_260 = registerItem("placeholder_260",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // ===== 261-270：残/仙/神兽系列掉落（均为普通物品，最大堆叠 64；精灵帽仅为同名物品不可穿戴） =====

    // 编码 261 = 虚无之触（Void Touch）
    public static final RegistryObject<Item> VOID_TOUCH = registerItem("void_touch",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 262 = 残花枝（Faded Flower Branch）
    public static final RegistryObject<Item> FADED_FLOWER_BRANCH = registerItem("faded_flower_branch",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 263 = 天凤之喙（Celestial Phoenix Beak）
    public static final RegistryObject<Item> CELESTIAL_PHOENIX_BEAK = registerItem("celestial_phoenix_beak",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 264 = 残堕米精（Remnant Fallen Rice Essence）
    public static final RegistryObject<Item> REMNANT_FALLEN_RICE_ESSENCE =
            registerItem("remnant_fallen_rice_essence",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 265 = 残堕锭（Remnant Fallen Ingot）
    public static final RegistryObject<Item> REMNANT_FALLEN_INGOT = registerItem("remnant_fallen_ingot",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 266 = 鲛泪（Mermaid Tear）
    public static final RegistryObject<Item> MERMAID_TEAR = registerItem("mermaid_tear",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 267 = 蛟龙爪（Jiaolong Talon）
    public static final RegistryObject<Item> JIAOLONG_TALON = registerItem("jiaolong_talon",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 268 = 蜃楼骨（Mirage Bone）
    public static final RegistryObject<Item> MIRAGE_BONE = registerItem("mirage_bone",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 269 = 精灵帽（Elf Hat）：普通物品【非装备】，不可穿戴
    public static final RegistryObject<Item> ELF_HAT = registerItem("elf_hat",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 270 = 噬月狼爪（Moon Devourer Wolf Claw）
    public static final RegistryObject<Item> MOON_DEVOURER_WOLF_CLAW =
            registerItem("moon_devourer_wolf_claw",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // ===== 271：堕落台（Fallen Altar）：放置后对应 ModBlocks.FALLEN_ALTAR =====
    public static final RegistryObject<Item> FALLEN_ALTAR = registerItem("fallen_altar",
            () -> new BlockItem(ModBlocks.FALLEN_ALTAR.get(), new Item.Properties()));

    // ===== 272-286：堕落十字架 / 浸染液 / 深染系列（273、274 为待定占位，不进创造标签页） =====

    // 编码 272 = 堕落十字架（Fallen Cross）
    public static final RegistryObject<Item> FALLEN_CROSS = registerItem("fallen_cross",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 273 = 待定占位：不进创造标签页（ModCreativeTabs 过滤 placeholder_ 前缀），仅锁定编码
    public static final RegistryObject<Item> PLACEHOLDER_273 = registerItem("placeholder_273",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 274 = 待定占位：不进创造标签页（ModCreativeTabs 过滤 placeholder_ 前缀），仅锁定编码
    public static final RegistryObject<Item> PLACEHOLDER_274 = registerItem("placeholder_274",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 275 = 浸染液（Dyeing Solution）：深染系列材料的浸染原料
    public static final RegistryObject<Item> DYEING_SOLUTION = registerItem("dyeing_solution",
            () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 276 = 深染残天翼（Deep-Dyed Remnant Heaven Wing）：鞘翅类装备，
    // 直接继承原版 ElytraItem——胸甲槽穿戴、空中跳跃即可滑翔，耐久 432（滑翔每秒损耗1点），
    // .durability(432) 自动锁定最大堆叠为 1，无需也不能再链 .stacksTo(1)
    public static final RegistryObject<Item> DEEP_DYED_REMNANT_HEAVEN_WING =
            registerItem("deep_dyed_remnant_heaven_wing",
                    () -> new ElytraItem(new Item.Properties().durability(432)));

    // 编码 277 = 深染虚无之触（Deep-Dyed Void Touch）
    public static final RegistryObject<Item> DEEP_DYED_VOID_TOUCH =
            registerItem("deep_dyed_void_touch",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 278 = 深染残花枝（Deep-Dyed Faded Flower Branch）
    public static final RegistryObject<Item> DEEP_DYED_FADED_FLOWER_BRANCH =
            registerItem("deep_dyed_faded_flower_branch",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 279 = 深染天凤之喙（Deep-Dyed Celestial Phoenix Beak）
    public static final RegistryObject<Item> DEEP_DYED_CELESTIAL_PHOENIX_BEAK =
            registerItem("deep_dyed_celestial_phoenix_beak",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 280 = 深染残堕米精（Deep-Dyed Remnant Fallen Rice Essence）
    public static final RegistryObject<Item> DEEP_DYED_REMNANT_FALLEN_RICE_ESSENCE =
            registerItem("deep_dyed_remnant_fallen_rice_essence",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 281 = 深染鲛泪（Deep-Dyed Mermaid Tear）
    public static final RegistryObject<Item> DEEP_DYED_MERMAID_TEAR =
            registerItem("deep_dyed_mermaid_tear",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 282 = 深染蛟龙爪（Deep-Dyed Jiaolong Talon）
    public static final RegistryObject<Item> DEEP_DYED_JIAOLONG_TALON =
            registerItem("deep_dyed_jiaolong_talon",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 283 = 深染蜃楼骨（Deep-Dyed Mirage Bone）
    public static final RegistryObject<Item> DEEP_DYED_MIRAGE_BONE =
            registerItem("deep_dyed_mirage_bone",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 284 = 深染精灵帽（Deep-Dyed Elf Hat）：普通物品【非装备】，不可穿戴
    public static final RegistryObject<Item> DEEP_DYED_ELF_HAT =
            registerItem("deep_dyed_elf_hat",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 285 = 深染噬月狼爪（Deep-Dyed Moon Devourer Wolf Claw）
    public static final RegistryObject<Item> DEEP_DYED_MOON_DEVOURER_WOLF_CLAW =
            registerItem("deep_dyed_moon_devourer_wolf_claw",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // 编码 286 = 粗制残堕世界精华（Coarse Remnant Fallen World Essence）
    public static final RegistryObject<Item> COARSE_REMNANT_FALLEN_WORLD_ESSENCE =
            registerItem("coarse_remnant_fallen_world_essence",
                    () -> new Item(new Item.Properties().stacksTo(64)));

    // ============================ 新物品写在这行以上（末尾追加） ============================

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
