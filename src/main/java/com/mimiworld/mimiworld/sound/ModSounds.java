package com.mimiworld.mimiworld.sound;

import com.mimiworld.mimiworld.MimiWorld;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 自定义音效注册：8 个 Boss 各有一套 ambient / hurt / death / roar，
 * 另有米魂（rice_soul）一套 ambient / hurt / death / roar。
 * 音频文件位于 assets/mimiworld/sounds/boss/&lt;boss&gt;/{ambient,hurt,death,roar}.ogg
 * 与 assets/mimiworld/sounds/mob/rice_soul/{ambient,hurt,death,roar}.ogg，
 * 事件与文件的对应关系见 assets/mimiworld/sounds.json。
 */
public final class ModSounds {

    private ModSounds() {
    }

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MimiWorld.MODID);

    private static RegistryObject<SoundEvent> register(String id) {
        return SOUND_EVENTS.register(id, () ->
                SoundEvent.createVariableRangeEvent(new ResourceLocation(MimiWorld.MODID, id)));
    }

    // ---------- 米米猴王 ----------
    public static final RegistryObject<SoundEvent> MIMI_MONKEY_KING_AMBIENT = register("boss.mimi_monkey_king.ambient");
    public static final RegistryObject<SoundEvent> MIMI_MONKEY_KING_HURT = register("boss.mimi_monkey_king.hurt");
    public static final RegistryObject<SoundEvent> MIMI_MONKEY_KING_DEATH = register("boss.mimi_monkey_king.death");
    public static final RegistryObject<SoundEvent> MIMI_MONKEY_KING_ROAR = register("boss.mimi_monkey_king.roar");

    // ---------- 饭晶虎王 ----------
    public static final RegistryObject<SoundEvent> RICE_CRYSTAL_TIGER_KING_AMBIENT = register("boss.rice_crystal_tiger_king.ambient");
    public static final RegistryObject<SoundEvent> RICE_CRYSTAL_TIGER_KING_HURT = register("boss.rice_crystal_tiger_king.hurt");
    public static final RegistryObject<SoundEvent> RICE_CRYSTAL_TIGER_KING_DEATH = register("boss.rice_crystal_tiger_king.death");
    public static final RegistryObject<SoundEvent> RICE_CRYSTAL_TIGER_KING_ROAR = register("boss.rice_crystal_tiger_king.roar");

    // ---------- 超晶牛王 ----------
    public static final RegistryObject<SoundEvent> SUPER_CRYSTAL_COW_KING_AMBIENT = register("boss.super_crystal_cow_king.ambient");
    public static final RegistryObject<SoundEvent> SUPER_CRYSTAL_COW_KING_HURT = register("boss.super_crystal_cow_king.hurt");
    public static final RegistryObject<SoundEvent> SUPER_CRYSTAL_COW_KING_DEATH = register("boss.super_crystal_cow_king.death");
    public static final RegistryObject<SoundEvent> SUPER_CRYSTAL_COW_KING_ROAR = register("boss.super_crystal_cow_king.roar");

    // ---------- 重晶兔王 ----------
    public static final RegistryObject<SoundEvent> HEAVY_CRYSTAL_RABBIT_KING_AMBIENT = register("boss.heavy_crystal_rabbit_king.ambient");
    public static final RegistryObject<SoundEvent> HEAVY_CRYSTAL_RABBIT_KING_HURT = register("boss.heavy_crystal_rabbit_king.hurt");
    public static final RegistryObject<SoundEvent> HEAVY_CRYSTAL_RABBIT_KING_DEATH = register("boss.heavy_crystal_rabbit_king.death");
    public static final RegistryObject<SoundEvent> HEAVY_CRYSTAL_RABBIT_KING_ROAR = register("boss.heavy_crystal_rabbit_king.roar");

    // ---------- 复晶天青蟒 ----------
    public static final RegistryObject<SoundEvent> COMPOUND_AZURE_PYTHON_AMBIENT = register("boss.compound_azure_python.ambient");
    public static final RegistryObject<SoundEvent> COMPOUND_AZURE_PYTHON_HURT = register("boss.compound_azure_python.hurt");
    public static final RegistryObject<SoundEvent> COMPOUND_AZURE_PYTHON_DEATH = register("boss.compound_azure_python.death");
    public static final RegistryObject<SoundEvent> COMPOUND_AZURE_PYTHON_ROAR = register("boss.compound_azure_python.roar");

    // ---------- 清洁长命夔 ----------
    public static final RegistryObject<SoundEvent> CLEANSING_LONGEVITY_KUI_AMBIENT = register("boss.cleansing_longevity_kui.ambient");
    public static final RegistryObject<SoundEvent> CLEANSING_LONGEVITY_KUI_HURT = register("boss.cleansing_longevity_kui.hurt");
    public static final RegistryObject<SoundEvent> CLEANSING_LONGEVITY_KUI_DEATH = register("boss.cleansing_longevity_kui.death");
    public static final RegistryObject<SoundEvent> CLEANSING_LONGEVITY_KUI_ROAR = register("boss.cleansing_longevity_kui.roar");

    // ---------- 最懒终末龙 ----------
    public static final RegistryObject<SoundEvent> LAZIEST_FINAL_DRAGON_AMBIENT = register("boss.laziest_final_dragon.ambient");
    public static final RegistryObject<SoundEvent> LAZIEST_FINAL_DRAGON_HURT = register("boss.laziest_final_dragon.hurt");
    public static final RegistryObject<SoundEvent> LAZIEST_FINAL_DRAGON_DEATH = register("boss.laziest_final_dragon.death");
    public static final RegistryObject<SoundEvent> LAZIEST_FINAL_DRAGON_ROAR = register("boss.laziest_final_dragon.roar");

    // ---------- 第八宗罪龙 ----------
    public static final RegistryObject<SoundEvent> DIBAZONGZUILONG_AMBIENT = register("boss.dibazongzuilong.ambient");
    public static final RegistryObject<SoundEvent> DIBAZONGZUILONG_HURT = register("boss.dibazongzuilong.hurt");
    public static final RegistryObject<SoundEvent> DIBAZONGZUILONG_DEATH = register("boss.dibazongzuilong.death");
    public static final RegistryObject<SoundEvent> DIBAZONGZUILONG_ROAR = register("boss.dibazongzuilong.roar");

    // ---------- 米魂 ----------
    public static final RegistryObject<SoundEvent> RICE_SOUL_AMBIENT = register("mob.rice_soul.ambient");
    public static final RegistryObject<SoundEvent> RICE_SOUL_HURT = register("mob.rice_soul.hurt");
    public static final RegistryObject<SoundEvent> RICE_SOUL_DEATH = register("mob.rice_soul.death");
    public static final RegistryObject<SoundEvent> RICE_SOUL_ROAR = register("mob.rice_soul.roar");

    public static void register(IEventBus bus) {
        SOUND_EVENTS.register(bus);
    }
}
