package org.kuro.tvdvampirism.compat;

import de.teamlapen.vampirism.api.VReference;
import de.teamlapen.vampirism.api.entity.factions.IFaction;
import de.teamlapen.vampirism.api.entity.player.vampire.IVampirePlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.kuro.tvdvampirism.faction.VampireFamily;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Boundary between logical vampire classification and Vampirism's concrete
 * normal-vampire player implementation.
 */
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

    /**
     * Normal VampirePlayer/BloodStats behavior remains opt-in. Custom species
     * will receive their own mechanics later instead of accidentally running
     * the complete normal-vampire tick/feeding implementation.
     */
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
