package org.kuro.tvdvampirism.faction;

import de.teamlapen.vampirism.api.VReference;
import de.teamlapen.vampirism.api.VampirismAPI;
import de.teamlapen.vampirism.api.entity.factions.IFaction;
import de.teamlapen.vampirism.api.entity.factions.IFactionVillage;
import de.teamlapen.vampirism.api.entity.factions.IPlayableFaction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/** A playable species can be different from the faction used for villages and other world features */
public final class VampireFamily {

    public static final Set<ResourceLocation> IDS = Set.of(
            VReference.VAMPIRE_FACTION_ID,
            SpeciesFactions.AUGUSTINE_VAMPIRE_ID,
            SpeciesFactions.HYBRID_ID,
            SpeciesFactions.ORIGINAL_VAMPIRE_ID,
            SpeciesFactions.ORIGINAL_HYBRID_ID
    );

    private static final Set<ResourceLocation> VAMPIRE_SIDE_IDS = Set.of(
            VReference.VAMPIRE_FACTION_ID,
            SpeciesFactions.AUGUSTINE_VAMPIRE_ID,
            SpeciesFactions.ORIGINAL_VAMPIRE_ID
    );

    private static final Set<ResourceLocation> HYBRID_SIDE_IDS = Set.of(
            SpeciesFactions.HYBRID_ID,
            SpeciesFactions.ORIGINAL_HYBRID_ID
    );

    private static final Set<ResourceLocation> TECHNICAL_SPECIES_IDS = Set.of(
            SpeciesFactions.AUGUSTINE_VAMPIRE_ID,
            SpeciesFactions.HYBRID_ID,
            SpeciesFactions.ORIGINAL_VAMPIRE_ID,
            SpeciesFactions.ORIGINAL_HYBRID_ID
    );

    private VampireFamily() {
    }

    public static boolean isVampireDerived(@Nullable IFaction<?> faction) {
        return faction != null && IDS.contains(faction.getID());
    }

    public static boolean isVampireDerived(@Nullable Entity entity) {
        return entity != null
                && isVampireDerived(
                        VampirismAPI.factionRegistry().getFaction(entity)
                );
    }

    public static boolean isVampireSide(@Nullable IFaction<?> faction) {
        return faction != null
                && VAMPIRE_SIDE_IDS.contains(faction.getID());
    }

    public static boolean isHybridSide(@Nullable IFaction<?> faction) {
        return faction != null
                && HYBRID_SIDE_IDS.contains(faction.getID());
    }

    public static boolean isTechnicalSpecies(@Nullable IFaction<?> faction) {
        return faction != null
                && TECHNICAL_SPECIES_IDS.contains(faction.getID());
    }

    public static boolean hasOwnershipOnlyVillage(
            @Nullable IFaction<?> faction
    ) {
        return faction != null
                && SpeciesFactions.HYBRID_ID.equals(faction.getID());
    }

    /**
     * The Hybrid needs a registered totem for ownership changes; its other village data stays
     * empty.
     */
    public static IFactionVillage getVillagePresentationData(
            IFaction<?> worldFaction
    ) {
        return hasOwnershipOnlyVillage(worldFaction)
                ? VReference.VAMPIRE_FACTION.getVillageData()
                : worldFaction.getVillageData();
    }

    @Nullable
    public static IFaction<?> getWorldFaction(@Nullable IFaction<?> faction) {
        if (isVampireSide(faction)) {
            return VReference.VAMPIRE_FACTION;
        }
        if (isHybridSide(faction)) {
            return SpeciesFactions.hybrid();
        }
        return faction;
    }

    @Nullable
    public static IFaction<?> getWorldFaction(@Nullable Entity entity) {
        if (entity == null) {
            return null;
        }
        return getWorldFaction(
                VampirismAPI.factionRegistry().getFaction(entity)
        );
    }

    @Nullable
    public static IPlayableFaction<?> getWorldPlayableFaction(
            @Nullable IPlayableFaction<?> faction
    ) {
        IFaction<?> worldFaction = getWorldFaction(faction);
        return worldFaction instanceof IPlayableFaction<?> playableFaction
                ? playableFaction
                : faction;
    }

    /** The Hybrid uses the Vampire totem block, but still owns the village as a Hybrid. */
    public static boolean worldFactionMatchesTotemMarker(
            ResourceLocation markerFactionId,
            Object expectedFactionId
    ) {
        if (SpeciesFactions.HYBRID_ID.equals(expectedFactionId)) {
            return VReference.VAMPIRE_FACTION_ID.equals(markerFactionId);
        }
        return markerFactionId.equals(expectedFactionId);
    }

    /** Hunter filters for normal Vampires should also find the other vampire species. */
    public static boolean matchesVampiricTargetFilter(
            @Nullable IFaction<?> requested,
            @Nullable IFaction<?> actual
    ) {
        return requested != null
                && VReference.VAMPIRE_FACTION_ID.equals(requested.getID())
                && isVampireDerived(actual);
    }
}
