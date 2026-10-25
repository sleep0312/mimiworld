package com.mimiworld.mimiworld.mixin;

import com.mimiworld.mimiworld.enchantment.ModEnchantments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 「完美无缺」附魔的耐久保护。
 *
 * <p>原版物品扣除耐久的最终公共入口是 {@code ItemStack#hurt}：{@code hurtAndBreak}
 * 以及各类工具/盔甲/盾牌/鞘翅的耐久扣除最终都会走到这里。这里在 HEAD 处检查
 * 物品是否带有完美无缺附魔，若带有则直接返回 {@code false}（表示没有损坏），
 * 不写入任何耐久损耗，也不会触发损坏回调。</p>
 *
 * <p>不处理创造模式，因为原版 {@code hurtAndBreak} 本身已对创造模式做了豁免。</p>
 */
@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void mimiworld$preventFlawlessDurabilityLoss(int amount, RandomSource random,
                                                         ServerPlayer player,
                                                         CallbackInfoReturnable<Boolean> cir) {
        ItemStack self = (ItemStack) (Object) this;
        if (amount > 0 && EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.FLAWLESS.get(), self) > 0) {
            cir.setReturnValue(false);
        }
    }
}
