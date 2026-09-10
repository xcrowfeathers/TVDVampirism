package org.kuro.tvdvampirism.player;

import de.teamlapen.lib.HelperLib;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.*;
import org.kuro.tvdvampirism.compat.SpeciesCompatibility;
import org.kuro.tvdvampirism.config.ServerConfig;
import org.kuro.tvdvampirism.faction.VampireFamily;
import org.kuro.tvdvampirism.registry.DaggerContent;

@EventBusSubscriber(modid = "tvdvampirism")
public final class DaggerManager {
    private DaggerManager() {}

    public static boolean isDaggered(Player player) { return SpeciesManager.getData(player).isDaggered(); }

    private static boolean enabled(ItemStack stack) {
        return stack.is(DaggerContent.ELDER_DAGGER.get()) && ServerConfig.ELDER_DAGGER_ENABLED.get()
                || stack.is(DaggerContent.CURSED_ELDER_DAGGER.get()) && ServerConfig.CURSED_ELDER_DAGGER_ENABLED.get();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onMobAttack(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer target && event.getSource().getDirectEntity() instanceof Mob mob
                && event.getAmount() > 0 && mob.distanceToSqr(target) <= 9 && mob.hasLineOfSight(target)
                && enabled(mob.getMainHandItem()) && SpeciesRules.hasOriginalImmortality(target)
                && VampireFamily.isVampireDerived(mob)) {
            DeathPolicy.killDaggerAttacker(mob);
            event.setCanceled(true);
        }
    }

    /** Vanilla ray selection is limited to the melee segment, including blocking terrain. */
    public static ServerPlayer aimedTarget(ServerPlayer attacker) {
        double reach = attacker.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ENTITY_INTERACTION_RANGE);
        var start = attacker.getEyePosition();
        var rayEnd = start.add(attacker.calculateViewVector(attacker.getXRot(), attacker.getYRot()).scale(reach));
        var end = attacker.level().clip(new net.minecraft.world.level.ClipContext(start, rayEnd,
                net.minecraft.world.level.ClipContext.Block.OUTLINE,
                net.minecraft.world.level.ClipContext.Fluid.NONE, attacker)).getLocation();
        var hit = net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(attacker, start, end,
                attacker.getBoundingBox().expandTowards(end.subtract(start)).inflate(1),
                entity -> !entity.isSpectator() && entity.isPickable(), start.distanceToSqr(end));
        return hit != null && hit.getEntity() instanceof ServerPlayer target ? target : null;
    }

    public static boolean validCharge(ServerPlayer attacker, ServerPlayer target, ItemStack held) {
        if (target == null || !enabled(held) || !attacker.isAlive() || attacker.isSpectator() || !target.isAlive()
                || attacker == target || attacker.level() != target.level() || !attacker.canInteractWithEntity(target, 0)
                || !attacker.hasLineOfSight(target) || !attacker.server.isPvpAllowed() || !attacker.canHarmPlayer(target)
                || SpeciesCompatibility.rawVampire(attacker).isDBNO()) return false;
        return SpeciesManager.getSpecies(target) == (held.is(DaggerContent.CURSED_ELDER_DAGGER.get())
                ? Species.ORIGINAL_HYBRID : Species.ORIGINAL) && !isDaggered(target);
    }

    public static boolean beginCharge(ServerPlayer attacker, ItemStack held) {
        SpeciesManager.getData(attacker).setDaggerChargeTarget(null);
        if (!enabled(held) || !attacker.isAlive() || attacker.isSpectator()
                || SpeciesCompatibility.rawVampire(attacker).isDBNO()) return false;
        var target = aimedTarget(attacker);
        // Let vanilla synchronize the use pose even without a valid lock. Only a
        // valid target can be committed when the use duration completes.
        if (target == null || !validCharge(attacker, target, held)) return true;
        if (VampireFamily.isVampireDerived(attacker) && !SpeciesRules.hasOriginalImmortality(attacker)) {
            DeathPolicy.killDaggerAttacker(attacker);
            return false;
        }
        SpeciesManager.getData(attacker).setDaggerChargeTarget(target.getUUID());
        return true;
    }

    public static ServerPlayer chargedTarget(ServerPlayer attacker, ItemStack held) {
        var targetId = SpeciesManager.getData(attacker).getDaggerChargeTarget();
        if (targetId == null) return null;
        var target = attacker.server.getPlayerList().getPlayer(targetId);
        return validCharge(attacker, target, held) ? target : null;
    }

    public static boolean hasChargeTarget(ServerPlayer attacker) {
        return SpeciesManager.getData(attacker).getDaggerChargeTarget() != null;
    }

    public static boolean finishCharge(ServerPlayer attacker, ItemStack held) {
        var target = chargedTarget(attacker, held);
        SpeciesManager.getData(attacker).setDaggerChargeTarget(null);
        if (target == null || !attacker.isUsingItem()
                || !ItemStack.isSameItemSameComponents(attacker.getUseItem(), held)
                || attacker.getTicksUsingItem() < 60) return false;
        if (VampireFamily.isVampireDerived(attacker) && !SpeciesRules.hasOriginalImmortality(attacker)) {
            DeathPolicy.killDaggerAttacker(attacker);
            return false;
        }
        boolean cursed = held.is(DaggerContent.CURSED_ELDER_DAGGER.get());
        if (!OriginalImmortalityManager.isDown(target)
                && !OriginalImmortalityManager.enter(target, target.damageSources().mobAttack(attacker))) return false;
        ItemStack embedded = held.copyWithCount(1);
        held.shrink(1);
        var targetData = SpeciesManager.getData(target);
        targetData.setEmbeddedDagger(embedded);
        targetData.delayDaggerRemovalUntil(target.level().getGameTime()
                + ServerConfig.DAGGER_REMOVAL_DELAY_TICKS.get());
        sync(target);
        target.serverLevel().playSound(null, target.blockPosition(), net.minecraft.sounds.SoundEvents.TRIDENT_HIT,
                net.minecraft.sounds.SoundSource.PLAYERS, 1, 0.8F);
        if (ServerConfig.DAGGER_BROADCAST_ENABLED.get()) {
            var message = Component.literal(ServerConfig.DAGGER_BROADCAST_MESSAGE.get()
                    .replace("%victim%", target.getName().getString()).replace("%attacker%", attacker.getName().getString())
                    .replace("%species%", cursed ? "Original Hybrid" : "Original Vampire"));
            for (var online : target.server.getPlayerList().getPlayers())
                if (SpeciesManager.isVampirismVampire(online) || SpeciesRules.hasOriginalImmortality(online)) online.sendSystemMessage(message);
        }
        return true;
    }

    public static boolean remove(ServerPlayer remover, ServerPlayer target) {
        var data = SpeciesManager.getData(target);
        if (remover == target || !remover.isAlive() || remover.isSpectator() || !target.isAlive()
                || remover.level() != target.level() || !remover.canInteractWithEntity(target, 0)
                || !remover.hasLineOfSight(target) || SpeciesCompatibility.rawVampire(remover).isDBNO()
                || !OriginalImmortalityManager.isDown(target) || !data.isDaggered()
                || !data.canRemoveDagger(target.level().getGameTime())) return false;
        var dagger = data.getEmbeddedDagger().copy();
        data.setEmbeddedDagger(ItemStack.EMPTY);
        sync(target);
        if (!remover.getInventory().add(dagger)) remover.drop(dagger, false);
        return true;
    }

    @SubscribeEvent public static void interact(PlayerInteractEvent.EntityInteract event) { interactTarget(event, event.getTarget()); }
    @SubscribeEvent public static void interactAt(PlayerInteractEvent.EntityInteractSpecific event) { interactTarget(event, event.getTarget()); }

    private static void interactTarget(PlayerInteractEvent event, net.minecraft.world.entity.Entity entity) {
        if (event.getHand() != InteractionHand.MAIN_HAND || !(entity instanceof Player target) || !isDaggered(target)) return;
        boolean removed = event.getEntity().level().isClientSide
                || event.getEntity() instanceof ServerPlayer remover && target instanceof ServerPlayer victim && remove(remover, victim);
        if (removed) {
            if (event instanceof PlayerInteractEvent.EntityInteract interaction) {
                interaction.setCanceled(true);
                interaction.setCancellationResult(InteractionResult.SUCCESS);
            } else if (event instanceof PlayerInteractEvent.EntityInteractSpecific interaction) {
                interaction.setCanceled(true);
                interaction.setCancellationResult(InteractionResult.SUCCESS);
            }
        }
    }

    private static void sync(ServerPlayer player) {
        var custom = SpeciesCompatibility.customPlayer(player);
        if (custom == null) return;
        var tag = new CompoundTag();
        tag.put("EmbeddedDagger", SpeciesManager.getData(player).getEmbeddedDagger().saveOptional(player.registryAccess()));
        HelperLib.sync(custom, tag, player, true);
    }

    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) sync(player);
    }
    @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            SpeciesManager.getData(player).setDaggerChargeTarget(null);
            sync(player);
        }
    }
    @SubscribeEvent public static void tracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof ServerPlayer player) sync(player);
    }
}
