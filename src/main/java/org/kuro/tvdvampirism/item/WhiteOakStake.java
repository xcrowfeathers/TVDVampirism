package org.kuro.tvdvampirism.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import org.kuro.tvdvampirism.compat.SpeciesCompatibility;
import org.kuro.tvdvampirism.player.DeathPolicy;

@EventBusSubscriber(modid = "tvdvampirism")
public final class WhiteOakStake {
    private WhiteOakStake() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onAttack(AttackEntityEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer attacker)
                || !(event.getTarget() instanceof ServerPlayer target)
                || !attacker.isAlive() || attacker.isSpectator() || attacker == target
                || attacker.level() != target.level() || !attacker.canInteractWithEntity(target, 0)
                || !attacker.hasLineOfSight(target) || !attacker.server.isPvpAllowed()
                || !attacker.canHarmPlayer(target) || SpeciesCompatibility.rawVampire(attacker).isDBNO()) return;
        if (finish(attacker, target, attacker.getMainHandItem())) event.setCanceled(true);
    }

    /** Shared by a validated melee attack and the operator's self-test. */
    public static boolean finish(ServerPlayer attacker, ServerPlayer target, ItemStack stake) {
        if (!DeathPolicy.tryWhiteOakKill(attacker, target, stake)) return false;
        target.setRemainingFireTicks(100);
        var lightning = EntityType.LIGHTNING_BOLT.create(target.serverLevel());
        if (lightning != null) {
            lightning.moveTo(target.position());
            lightning.setVisualOnly(true);
            target.serverLevel().addFreshEntity(lightning);
        }
        return true;
    }
}
