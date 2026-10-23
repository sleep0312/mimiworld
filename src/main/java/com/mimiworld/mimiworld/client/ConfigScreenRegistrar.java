package com.mimiworld.mimiworld.client;

import com.mimiworld.mimiworld.MimiWorld;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;

/**
 * 在模组列表里为米米世界启用「配置」按钮。
 *
 * <p>Cloth Config 属于可选依赖：未安装时不注册该扩展点，mod 照常运行（按钮保持灰色），
 * 数值仍可通过 config/mimiworld-common.toml 修改。</p>
 */
public final class ConfigScreenRegistrar {

    private ConfigScreenRegistrar() {
    }

    public static void register() {
        if (!ModList.get().isLoaded("cloth_config")) {
            return;
        }
        ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (minecraft, parent) -> ClothConfigScreen.create(parent)));
    }
}
