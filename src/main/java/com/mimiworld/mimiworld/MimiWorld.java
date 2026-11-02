package com.mimiworld.mimiworld;

import com.mimiworld.mimiworld.block.ModBlocks;
import com.mimiworld.mimiworld.client.ConfigScreenRegistrar;
import com.mimiworld.mimiworld.block.entity.ModBlockEntities;
import com.mimiworld.mimiworld.effect.ModEffects;
import com.mimiworld.mimiworld.enchantment.ModEnchantments;
import com.mimiworld.mimiworld.entity.ModEntities;
import com.mimiworld.mimiworld.menu.ModMenuTypes;
import com.mimiworld.mimiworld.network.ModNetwork;
import com.mimiworld.mimiworld.fluid.ModFluids;
import com.mojang.logging.LogUtils;
import software.bernie.geckolib.GeckoLib;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// 米米世界主类，MODID 与 META-INF/mods.toml 中的条目一致
@Mod(MimiWorld.MODID)
public class MimiWorld
{
    public static final String MODID = "mimiworld";
    private static final Logger LOGGER = LogUtils.getLogger();

    public MimiWorld(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();

        // 数值配置文件：config/mimiworld-common.toml（怪物/防具/武器/战斗/掉落）
        ModConfig.register();

        // 可选的 Cloth Config 图形界面：仅在客户端且安装了 cloth_config 时注册模组列表的「配置」按钮
        if (FMLEnvironment.dist.isClient()) {
            ConfigScreenRegistrar.register();
        }

        // GeckoLib 初始化：内部完成网络注册与客户端渲染/重载监听等事件总线订阅，
        // 是后续自定义 Geo 生物、立体 Geo 盔甲、动画方块/物品的必要前置（有 hasInitialized 防重入）
        GeckoLib.initialize();

        // 网络通道：猩红竖眼的按键请求（C2S）
        ModNetwork.register();

        // 挂载各注册器到 mod 事件总线
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModFluids.register(modEventBus); // 米汤流体类型/源流体/流动流体/流体方块
        ModCreativeTabs.register(modEventBus);
        ModEntities.register(modEventBus);
        ModBlockEntities.register(modEventBus); // 造米台方块实体类型
        ModMenuTypes.register(modEventBus);     // 造米台 GUI 菜单类型
        ModEffects.register(modEventBus);       // 自定义药水效果（腥红烛照 / 蟒蛇缠绕）
        ModEnchantments.register(modEventBus);  // 自定义附魔（泣血）
        // 米树 configured_feature 是动态注册表内容，1.20.1 只能用数据包 JSON 注册（data/mimiworld/worldgen/configured_feature/rice_tree.json），无 DeferredRegister

        // commonSetup 主线程阶段注册米桶↔空炼药锅交互（需等物品/方块注册表填充完成）
        modEventBus.addListener(this::commonSetup);

        // 注册到 Forge 事件总线，供后续服务端/游戏事件监听使用
        MinecraftForge.EVENT_BUS.register(this);

        LOGGER.info("MimiWorld mod loaded");
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(ModFluids::registerCauldronInteractions);
        // 工作方块（锤米台）即时合成配方，需在注册表冻结后注册
        event.enqueueWork(ModMachineRecipes::register);
        // 贪米鱼：水下生成规则（同原版鳕鱼——海平面附近、上方为水源的水中），
        // 具体生物群系（河流/海洋）由 data 下的 forge biome_modifier JSON 添加
        event.enqueueWork(() -> SpawnPlacements.register(
                ModEntities.TANMI_FISH.get(),
                SpawnPlacements.Type.IN_WATER,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                WaterAnimal::checkSurfaceWaterAnimalSpawnRules));
        // 喷米鱼：水下生成规则（同贪米鱼/原版鳕鱼），生物群系由 forge biome_modifier JSON 添加
        event.enqueueWork(() -> SpawnPlacements.register(
                ModEntities.PENMI_FISH.get(),
                SpawnPlacements.Type.IN_WATER,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                WaterAnimal::checkSurfaceWaterAnimalSpawnRules));
        // ===== 残堕界（mimiworld:remnant_fallen_realm）敌对生物的生成放置规则：按各生物属性设定 =====
        // 地面 / 扎根类：贴地生成（ON_GROUND，同原版地狱地面怪）
        // 其中堕落米凤、哮噬米狼鹫为陆空两栖，贴地生成后自行起飞
        event.enqueueWork(() -> {
            SpawnPlacements.register(ModEntities.BLIGHTED_THORN_FLOWER.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
            SpawnPlacements.register(ModEntities.CHAOTIC_VOID_TOUCHER.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
            SpawnPlacements.register(ModEntities.RICE_MIRAGE.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
            SpawnPlacements.register(ModEntities.LAZY_ELF.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
            SpawnPlacements.register(ModEntities.FALLEN_RICE_PHOENIX.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
            SpawnPlacements.register(ModEntities.RICE_WOLF_VULTURE.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
        });
        // 纯空中悬浮类：不限制位置（NO_RESTRICTIONS，同原版恼鬼/幻翼，可在空中生成）
        event.enqueueWork(() -> {
            SpawnPlacements.register(ModEntities.JIETIAN_RICE.get(), SpawnPlacements.Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
            SpawnPlacements.register(ModEntities.HUALING_RICE.get(), SpawnPlacements.Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
        });
        // 水生类：仅在水中生成（IN_WATER）。残堕界的流体是饭汤（占据原版熔岩位），
        // 已通过 data/minecraft/tags/fluids/water.json 将饭汤并入水（minecraft:water）流体标签，
        // 故两种鲛可在饭汤海中生成，并让 isInWater()/水下 AI 正常工作
        event.enqueueWork(() -> {
            SpawnPlacements.register(ModEntities.PALE_MERMAID.get(), SpawnPlacements.Type.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
            SpawnPlacements.register(ModEntities.WANGMI_WOUNDED_MERMAID.get(), SpawnPlacements.Type.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
        });
        // ===== 米米维度（mimiworld:mimi_dimension，唯一生物群系 mimiworld:mimi_plains）自然生成放置规则 =====
        // 规律对齐原版主世界：陆生动物贴地 + 下方为可生成地表 + 光照充足；敌对怪物贴地 + 黑暗处；
        // 水生鱼仅在水中——贪米鱼/喷米鱼的 IN_WATER 规则已在上面注册，此处不重复。
        // 陆生友好/中立（CREATURE）：本项目这些实体继承 PathfinderMob 而非 Animal，不能直接复用
        // Animal::checkAnimalSpawnRules，改用等价判定 checkRiceLandAnimalSpawnRules
        // （下方方块属于 minecraft:animals_spawnable_on 标签 + 亮度 > 8）。
        // 米米维度地表是米质地表（mimiworld:mud_rice / mimiworld:hardened_rice），
        // 已通过 data/minecraft/tags/blocks/animals_spawnable_on.json 并入该标签，否则动物类无法刷出。
        event.enqueueWork(() -> {
            SpawnPlacements.register(ModEntities.ZHUOMI_CHICKEN.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, MimiWorld::checkRiceLandAnimalSpawnRules);
            SpawnPlacements.register(ModEntities.MIMOO_COW.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, MimiWorld::checkRiceLandAnimalSpawnRules);
            SpawnPlacements.register(ModEntities.MIMI_SHEEP.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, MimiWorld::checkRiceLandAnimalSpawnRules);
            SpawnPlacements.register(ModEntities.MIHENG_PIG.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, MimiWorld::checkRiceLandAnimalSpawnRules);
            // 中立两只同样走陆生动物规则：嗥米狼行为对齐原版狼、米傀儡属性对齐原版铁傀儡
            SpawnPlacements.register(ModEntities.HAOMI_WOLF.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, MimiWorld::checkRiceLandAnimalSpawnRules);
            SpawnPlacements.register(ModEntities.RICE_GOLEM.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, MimiWorld::checkRiceLandAnimalSpawnRules);
        });
        // 敌对陆生（MONSTER）：这些实体继承 Monster，直接复用 Monster::checkMonsterSpawnRules（黑暗 + 非和平）
        event.enqueueWork(() -> {
            SpawnPlacements.register(ModEntities.HUMI_CORPSE.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
            SpawnPlacements.register(ModEntities.PAMI_CRAWLER.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
            SpawnPlacements.register(ModEntities.RICE_WITCH.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
            SpawnPlacements.register(ModEntities.EXPLOSIVE_RICE_SPIRIT.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
            SpawnPlacements.register(ModEntities.CARVED_RICE_SKELETON.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
            // 米莱马：史莱姆类（MilaimaEntity extends Slime，原型类不在 Monster 分支下）无法引用
            // Monster::checkMonsterSpawnRules；且原版史莱姆只在史莱姆区块/沼泽刷出、米米维度无沼泽，
            // 故与米米骷髅共用等价的怪物黑暗判定
            SpawnPlacements.register(ModEntities.MILAI_HORSE.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, MimiWorld::checkRiceMonsterSpawnRules);
            SpawnPlacements.register(ModEntities.STICKY_RICE_SPIDER.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
            SpawnPlacements.register(ModEntities.MIHENG_PIG_CORPSE.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
            SpawnPlacements.register(ModEntities.RICE_STEALER.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
            SpawnPlacements.register(ModEntities.RICE_SOUL.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
            // 没米入：末影人系（MONSTER 类别、平时中立），生成判定与原版末影人一致
            SpawnPlacements.register(ModEntities.MOMI_ENTRANT.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules);
            // 米米骷髅：类别已按敌对改为 MONSTER，但实体继承 PathfinderMob 而非 Monster，
            // 无法引用 Monster::checkMonsterSpawnRules，改用等价判定 checkRiceMonsterSpawnRules
            SpawnPlacements.register(ModEntities.MIMI_SKELETON.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, MimiWorld::checkRiceMonsterSpawnRules);
        });
    }

    // 米米维度陆生友好/中立生物的生成判定：等价于 Animal.checkAnimalSpawnRules
    // （下方方块属于 minecraft:animals_spawnable_on 标签 + 局部亮度 > 8），
    // 但不要求实体是 Animal 子类，以适配本项目继承 PathfinderMob 的陆生生物
    private static boolean checkRiceLandAnimalSpawnRules(EntityType<? extends Mob> type, ServerLevelAccessor level,
                                                         MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return level.getBlockState(pos.below()).is(BlockTags.ANIMALS_SPAWNABLE_ON)
                && level.getRawBrightness(pos, 0) > 8;
    }

    // 米米骷髅专用生成判定：等价于 Monster.checkMonsterSpawnRules（非和平 + 黑暗判定 + Mob 通用判定），
    // 仅因该实体继承 PathfinderMob 而非 Monster，无法直接引用 Monster::checkMonsterSpawnRules 才单独实现
    private static boolean checkRiceMonsterSpawnRules(EntityType<? extends Mob> type, ServerLevelAccessor level,
                                                      MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return level.getDifficulty() != Difficulty.PEACEFUL
                && Monster.isDarkEnoughToSpawn(level, pos, random)
                && Mob.checkMobSpawnRules(type, level, spawnType, pos, random);
    }

    // ===== 临时诊断（验证后删除）：确认运行时 mimi_plains 生物群系是否含自定义生成项 =====
    @net.minecraftforge.eventbus.api.SubscribeEvent
    public void onServerStartedForDiagnosis(net.minecraftforge.event.server.ServerStartedEvent event) {
        var biome = event.getServer().registryAccess()
                .registryOrThrow(net.minecraft.core.registries.Registries.BIOME)
                .get(new net.minecraft.resources.ResourceLocation(MODID, "mimi_plains"));
        if (biome == null) {
            LOGGER.error("DIAG mimi_plains biome NOT FOUND in registry");
            return;
        }
        for (net.minecraft.world.entity.MobCategory cat : net.minecraft.world.entity.MobCategory.values()) {
            var spawns = biome.getMobSettings().getMobs(cat).unwrap();
            if (!spawns.isEmpty()) {
                LOGGER.error("DIAG biome=mimi_plains cat={} spawns={}", cat.getName(), spawns);
            }
        }
        net.minecraft.server.level.ServerLevel mimi = null;
        for (net.minecraft.server.level.ServerLevel l : event.getServer().getAllLevels()) {
            if (l.dimension().location().equals(new net.minecraft.resources.ResourceLocation(MODID, "mimi_dimension"))) {
                mimi = l;
            }
        }
        LOGGER.error("DIAG mimi_dimension level found = {}", mimi != null);
        if (mimi != null) {
            mimi.getChunk(0, 0);
            BlockPos pos = new BlockPos(0, mimi.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, 0, 0), 0);
            LOGGER.error("DIAG surface pos={} stateAt={} stateBelow={} animalsSpawnableOn={} rawBrightness={}",
                    pos, mimi.getBlockState(pos), mimi.getBlockState(pos.below()),
                    mimi.getBlockState(pos.below()).is(BlockTags.ANIMALS_SPAWNABLE_ON), mimi.getRawBrightness(pos, 0));
            mimi.setDayTime(6000L);
            diagTest(mimi, ModEntities.MIMI_SHEEP.get(), "day-animal", pos);
            diagTest(mimi, ModEntities.MIMI_SKELETON.get(), "day-monster", pos);
            mimi.setDayTime(18000L);
            mimi.setDayTime(18000L);
            LOGGER.error("DIAG night rawBrightness={}", mimi.getRawBrightness(pos, 0));
            diagTest(mimi, ModEntities.MIMI_SKELETON.get(), "night-monster", pos);
            diagTest(mimi, ModEntities.HUMI_CORPSE.get(), "night-monster", pos);
        }
        LOGGER.error("DIAG placement mimi_sheep={} milai_horse={} mimi_skeleton={} humi_corpse={}",
                SpawnPlacements.getPlacementType(ModEntities.MIMI_SHEEP.get()),
                SpawnPlacements.getPlacementType(ModEntities.MILAI_HORSE.get()),
                SpawnPlacements.getPlacementType(ModEntities.MIMI_SKELETON.get()),
                SpawnPlacements.getPlacementType(ModEntities.HUMI_CORPSE.get()));
        LOGGER.error("DIAG category mimi_sheep={} mimi_skeleton={} momi_entrant={} tanmi_fish={}",
                ModEntities.MIMI_SHEEP.get().getCategory(),
                ModEntities.MIMI_SKELETON.get().getCategory(),
                ModEntities.MOMI_ENTRANT.get().getCategory(),
                ModEntities.TANMI_FISH.get().getCategory());
        LOGGER.error("DIAG canSummon mimi_sheep={} mimi_skeleton={} (vanilla) sheep={} zombie={}",
                ModEntities.MIMI_SHEEP.get().canSummon(), ModEntities.MIMI_SKELETON.get().canSummon(),
                net.minecraft.world.entity.EntityType.SHEEP.canSummon(), net.minecraft.world.entity.EntityType.ZOMBIE.canSummon());
        if (mimi != null) {
            BlockPos p = new BlockPos(0, mimi.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, 0, 0), 0);
            LOGGER.error("DIAG biomeAtSurface={} creatureProbability={}",
                    mimi.getBiome(p).unwrapKey().map(Object::toString).orElse("?"),
                    mimi.getBiome(p).value().getMobSettings().getCreatureProbability());
            // 区块生成阶段自然刷怪实测：先强制生成 24 个从未生成过的区块，再对这些区块重复调用原版
            // 区块生成刷怪入口 NaturalSpawner.spawnMobsForChunkGeneration（等价 ChunkStatus.SPAWN 阶段），
            // 放大样本量，统计刷出的实体，判定新探索区域能否刷出原版/自定义生物
            final int bcx = 900;
            final int bcz = 900;
            mimi.setDayTime(6000L);
            for (int i = 0; i < 24; i++) {
                mimi.getChunk(bcx + (i % 6) * 8, bcz + (i / 6) * 8);
            }
            java.util.Map<String, Integer> tally = new java.util.TreeMap<>();
            for (int i = 0; i < 200; i++) {
                int cx = bcx + (i % 6) * 8;
                int cz = bcz + ((i / 6) % 4) * 8;
                mimi.getChunk(cx, cz);
                net.minecraft.world.level.NaturalSpawner.spawnMobsForChunkGeneration(mimi,
                        mimi.getBiome(new BlockPos(cx * 16, 71, cz * 16)),
                        new net.minecraft.world.level.ChunkPos(cx, cz), mimi.getRandom());
            }
            net.minecraft.world.phys.AABB box = new net.minecraft.world.phys.AABB(
                    bcx * 16.0, mimi.getMinBuildHeight(), bcz * 16.0,
                    (bcx + 48) * 16.0, mimi.getMaxBuildHeight(), (bcz + 32) * 16.0);
            for (Mob mob : mimi.getEntitiesOfClass(Mob.class, box)) {
                tally.merge(net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE
                        .getKey(mob.getType()).toString(), 1, Integer::sum);
            }
            LOGGER.error("DIAG chunkgen mob tally (fresh area {} ,{}) = {}", bcx, bcz, tally);
        }
    }

    private static <T extends Mob> void diagTest(net.minecraft.server.level.ServerLevel level, EntityType<T> type,
                                                 String label, BlockPos pos) {
        LOGGER.error("DIAG test[{}] {} at {} posOk={} checkRules={}", label, type, pos,
                net.minecraft.world.level.NaturalSpawner.isSpawnPositionOk(SpawnPlacements.getPlacementType(type), level, pos, type),
                SpawnPlacements.checkSpawnRules(type, level, MobSpawnType.NATURAL, pos, level.random));
    }
}
