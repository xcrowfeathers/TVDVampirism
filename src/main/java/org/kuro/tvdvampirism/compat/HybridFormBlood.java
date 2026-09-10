package org.kuro.tvdvampirism.compat;

import de.teamlapen.werewolves.api.entities.player.IWerewolfPlayer;
import org.kuro.tvdvampirism.config.ServerConfig;
import org.kuro.tvdvampirism.player.Species;
import org.kuro.tvdvampirism.player.SpeciesManager;
import org.kuro.tvdvampirism.player.SpeciesRules;

/** Called only by the stock active form action; returning true uses its normal deactivation. */
public final class HybridFormBlood {
    private HybridFormBlood() {}

    public static boolean shouldEndForm(IWerewolfPlayer wolf) {
        var player = wolf.asEntity();
        if (player.level().isClientSide || !SpeciesRules.canUseHumanWolfBite(player)
                || wolf.getForm().isHumanLike()) return false;
        var custom = SpeciesCompatibility.customPlayer(player);
        boolean original = SpeciesManager.getSpecies(player) == Species.ORIGINAL_HYBRID;
        int cost = (original ? ServerConfig.ORIGINAL_HYBRID_FORM_BLOOD_COST : ServerConfig.HYBRID_FORM_BLOOD_COST).get();
        int interval = (original ? ServerConfig.ORIGINAL_HYBRID_FORM_BLOOD_INTERVAL : ServerConfig.HYBRID_FORM_BLOOD_INTERVAL).get() * 20;
        var blood = custom.getBloodData();
        if (blood.getBloodLevel() == 0) return true;
        if (player.tickCount % interval != 0) return false;
        boolean paid = blood.removeBlood(cost, false);
        if (paid && cost > 0) custom.syncBlood(false);
        return !paid || blood.getBloodLevel() == 0;
    }
}
