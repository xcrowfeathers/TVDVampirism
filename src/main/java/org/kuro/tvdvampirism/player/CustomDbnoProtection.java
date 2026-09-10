package org.kuro.tvdvampirism.player;

import de.teamlapen.vampirism.util.Helper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import org.kuro.tvdvampirism.Tvdvampirism;
import org.kuro.tvdvampirism.compat.SpeciesCompatibility;

/** Bridges Vampirism's stock DBNO damage immunity to the two mortal custom vampire species. */
@EventBusSubscriber(modid = Tvdvampirism.MODID)
public final class CustomDbnoProtection {
    private CustomDbnoProtection() {}

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Species species = SpeciesManager.getSpecies(player);
        if ((species != Species.AUGUSTINE && species != Species.HYBRID)
                || !SpeciesCompatibility.rawVampire(player).isDBNO()
                || Helper.canKillVampires(event.getSource())) return;

        event.setCanceled(true);
        if (event.getSource().getEntity() instanceof Mob mob && mob.getTarget() == player) {
            mob.setTarget(null);
        }
    }
}
