package org.kuro.tvdvampirism.player;

import de.teamlapen.vampirism.api.VReference;
import de.teamlapen.vampirism.entity.factions.FactionPlayerHandler;
import net.minecraft.world.entity.player.Player;
import org.kuro.tvdvampirism.faction.SpeciesFactions;
import org.kuro.tvdvampirism.registry.ModAttachments;

public final class SpeciesManager {

    private SpeciesManager() {
    }


    // ---------------------------------------------------------
    // Data
    // ---------------------------------------------------------

    public static PlayerData getData(Player player) {
        return player.getData(
                ModAttachments.PLAYER_DATA.get()
        );
    }


    // ---------------------------------------------------------
    // Species resolution
    // ---------------------------------------------------------

    public static Species getSpecies(Player player) {
        return SpeciesFactions.getSpecies(
                FactionPlayerHandler
                        .get(player)
                        .getCurrentFaction()
        );
    }


    // ---------------------------------------------------------
    // Vampirism
    // ---------------------------------------------------------

    public static boolean isVampirismVampire(Player player) {

        return VReference.VAMPIRE_FACTION != null
                && FactionPlayerHandler
                .get(player)
                .isInFaction(
                        VReference.VAMPIRE_FACTION
                );
    }

    public static int getSpeciesLevel(Player player) {

        FactionPlayerHandler handler =
                FactionPlayerHandler.get(player);

        if (getSpecies(player) == Species.NONE) {
            return 0;
        }

        return handler.getCurrentLevel();
    }

    public static int getLordLevel(Player player) {

        return FactionPlayerHandler
                .get(player)
                .getLordLevel();
    }
}
