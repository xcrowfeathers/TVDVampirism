package org.kuro.tvdvampirism.compat;

import de.teamlapen.vampirism.api.VReference;
import de.teamlapen.vampirism.api.entity.factions.IFaction;
import de.teamlapen.vampirism.api.entity.player.vampire.IVampirePlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.kuro.tvdvampirism.faction.VampireFamily;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class VampireCompat {

    private VampireCompat() {
    }

    public static boolean isVampireDerived(@Nullable Entity entity) {
        return VampireFamily.isVampireDerived(entity);
    }

    public static boolean isVampireDerived(@Nullable IFaction<?> faction) {
        return VampireFamily.isVampireDerived(faction);
    }

    public static boolean isVampiricEnemyForHunters(
            @Nullable Entity entity
    ) {
        return isVampireDerived(entity);
    }

    /** Do not run the full stock VampirePlayer tick for custom species */
    public static boolean shouldUseNormalVampireMechanics(
            @Nullable IFaction<?> faction
    ) {
        return faction != null
                && VReference.VAMPIRE_FACTION_ID.equals(faction.getID());
    }

    public static Optional<IVampirePlayer> getNormalVampirePlayer(
            Player player
    ) {
        IFaction<?> faction = de.teamlapen.vampirism.api.VampirismAPI
                .factionRegistry()
                .getFaction(player);
        if (!shouldUseNormalVampireMechanics(faction)
                || VReference.VAMPIRE_FACTION == null) {
            return Optional.empty();
        }
        return VReference.VAMPIRE_FACTION.getPlayerCapability(player);
    }
}
