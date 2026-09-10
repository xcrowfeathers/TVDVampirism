package org.kuro.tvdvampirism.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.kuro.tvdvampirism.player.DaggerManager;
import org.kuro.tvdvampirism.player.SpeciesManager;

/** Uses vanilla held-item duration/animation; only a transient target identity is addon state. */
public final class ElderDaggerItem extends Item {
    private final boolean enchantedGlint;

    public ElderDaggerItem(Properties properties) { this(properties, false); }
    public ElderDaggerItem(Properties properties, boolean enchantedGlint) {
        super(properties);
        this.enchantedGlint = enchantedGlint;
    }

    @Override public boolean isFoil(ItemStack stack) {
        return enchantedGlint || super.isFoil(stack);
    }

    @Override public int getUseDuration(ItemStack stack, LivingEntity entity) { return 60; }
    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.SPEAR; }

    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var held = player.getItemInHand(hand);
        if (player instanceof ServerPlayer server && !DaggerManager.beginCharge(server, held)) return InteractionResultHolder.fail(held);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(held);
    }

    @Override public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remaining) {
        if (entity instanceof ServerPlayer player && DaggerManager.hasChargeTarget(player)
                && DaggerManager.chargedTarget(player, stack) == null) {
            SpeciesManager.getData(player).setDaggerChargeTarget(null);
            player.stopUsingItem();
        }
    }

    @Override public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remaining) {
        if (entity instanceof ServerPlayer player) SpeciesManager.getData(player).setDaggerChargeTarget(null);
    }

    @Override public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof ServerPlayer player) DaggerManager.finishCharge(player, stack);
        return stack;
    }
}
