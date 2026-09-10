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

/**
 * Central identity mapping for the playable factions that are derived from
 * vampires. A playable faction is a species identity; its world faction is
 * the identity used by village ownership and other world-facing systems.
 */
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
     * Supplies only the already-registered totem representation required by
     * TotemBlockEntity when it changes ownership. All other Hybrid village
     * data remains the empty default from its technical faction registration.
     */
    public static IFactionVillage getVillagePresentationData(
            IFaction<?> worldFaction
    ) {
        return hasOwnershipOnlyVillage(worldFaction)
                ? VReference.VAMPIRE_FACTION.getVillageData()
                : worldFaction.getVillageData();
    }

    /**
     * Maps playable species identity to world/village ownership identity.
     */
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

    /**
     * Hybrid ownership reuses Vampirism's existing vampire totem block as a
     * visual marker. Ownership itself remains the distinct Hybrid faction.
     */
    public static boolean worldFactionMatchesTotemMarker(
            ResourceLocation markerFactionId,
            Object expectedFactionId
    ) {
        if (SpeciesFactions.HYBRID_ID.equals(expectedFactionId)) {
            return VReference.VAMPIRE_FACTION_ID.equals(markerFactionId);
        }
        return markerFactionId.equals(expectedFactionId);
    }

    /**
     * Treats the normal vampire faction as a logical target filter for every
     * vampire-derived species. Used for Hunter selectors such as Awareness.
     */
    public static boolean matchesVampiricTargetFilter(
            @Nullable IFaction<?> requested,
            @Nullable IFaction<?> actual
    ) {
        return requested != null
                && VReference.VAMPIRE_FACTION_ID.equals(requested.getID())
                && isVampireDerived(actual);
    }
}
