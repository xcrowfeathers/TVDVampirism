package org.kuro.tvdvampirism.player;

import de.teamlapen.vampirism.api.entity.factions.IPlayableFaction;
import de.teamlapen.vampirism.entity.factions.FactionPlayerHandler;
import net.minecraft.server.level.ServerPlayer;
import org.kuro.tvdvampirism.Tvdvampirism;
import org.kuro.tvdvampirism.faction.SpeciesFactions;

public final class SpeciesTransitionManager {

    private SpeciesTransitionManager() {
    }


    /**
     * Development/debug helper.
     *
     * Umgeht absichtlich spätere Gameplay-Voraussetzungen wie
     * Serum, Hybrid Blood oder Curse Potion.
     */
    public static boolean forceSpecies(
            ServerPlayer player,
            Species species
    ) {

        FactionPlayerHandler handler =
                FactionPlayerHandler.get(player);

        IPlayableFaction<?> targetFaction =
                SpeciesFactions.forSpecies(species);
        if (targetFaction == null) {
            return handler.setFactionAndLevel(null, 0);
        }

        int targetLevel = Math.max(
                1,
                Math.min(
                        handler.getCurrentLevel(),
                        targetFaction.getHighestReachableLevel()
                )
        );
        return handler.setFactionAndLevel(
                targetFaction,
                targetLevel
        );
    }

    public static void migrateLegacySpecies(ServerPlayer player) {
        PlayerData data = SpeciesManager.getData(player);
        data.getLegacySpeciesMigration().ifPresent(species -> {
            if (forceSpecies(player, species)) {
                data.clearLegacySpeciesMigration();
                Tvdvampirism.LOGGER.info(
                        "Migrated {} from legacy species override to faction {}",
                        player.getGameProfile().getName(),
                        SpeciesFactions.forSpecies(species).getID()
                );
            } else {
                Tvdvampirism.LOGGER.warn(
                        "Could not migrate legacy species {} for {}",
                        species,
                        player.getGameProfile().getName()
                );
            }
        });
    }
}
