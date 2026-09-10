package org.kuro.tvdvampirism.faction;

import de.teamlapen.vampirism.api.VReference;
import de.teamlapen.vampirism.api.VampirismAPI;
import de.teamlapen.vampirism.api.entity.factions.IFaction;
import de.teamlapen.vampirism.api.entity.factions.IFactionEntity;
import de.teamlapen.vampirism.api.entity.player.IFactionPlayer;
import de.teamlapen.vampirism.entity.factions.FactionPlayerHandler;
import de.teamlapen.werewolves.api.WReference;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Set;

public final class FactionRelations {

    public static final Set<ResourceLocation> VAMPIRE_FAMILY =
            VampireFamily.IDS;

    private FactionRelations() {
    }

    public static Relationship between(
            @Nullable IFaction<?> source,
            @Nullable IFaction<?> target
    ) {
        if (source == null || target == null) {
            return source != null && source.isHostileTowardsNeutral()
                    ? Relationship.HOSTILE
                    : Relationship.NEUTRAL;
        }

        ResourceLocation sourceId = source.getID();
        ResourceLocation targetId = target.getID();
        if (sourceId.equals(targetId)) {
            return Relationship.ALLIED;
        }

        boolean sourceIsVampireFamily =
                VampireFamily.isVampireDerived(source);
        boolean targetIsVampireFamily =
                VampireFamily.isVampireDerived(target);
        if ((VampireFamily.isVampireSide(source)
                && VampireFamily.isVampireSide(target))
                || (VampireFamily.isHybridSide(source)
                && VampireFamily.isHybridSide(target))) {
            return Relationship.ALLIED;
        }

        if (sourceIsVampireFamily && targetIsVampireFamily) {
            return Relationship.NEUTRAL;
        }

        if (sourceIsVampireFamily || targetIsVampireFamily) {
            ResourceLocation otherId = sourceIsVampireFamily
                    ? targetId
                    : sourceId;
            if (VReference.HUNTER_FACTION_ID.equals(otherId)
                    || isWerewolfFaction(otherId)) {
                return Relationship.HOSTILE;
            }
        }

        // Vampirism's default is that distinct, non-neutral factions are hostile.
        return Relationship.HOSTILE;
    }

    public static boolean areAllied(
            @Nullable IFaction<?> source,
            @Nullable IFaction<?> target
    ) {
        return between(source, target) == Relationship.ALLIED;
    }

    public static boolean areNeutral(
            @Nullable IFaction<?> source,
            @Nullable IFaction<?> target
    ) {
        return between(source, target) == Relationship.NEUTRAL;
    }

    public static boolean areHostile(
            @Nullable IFaction<?> source,
            @Nullable IFaction<?> target
    ) {
        return between(source, target) == Relationship.HOSTILE;
    }

    public static boolean isVampireFamily(@Nullable IFaction<?> faction) {
        return VampireFamily.isVampireDerived(faction);
    }

    private static boolean isWerewolfFaction(ResourceLocation factionId) {
        return WReference.WEREWOLF_FACTION != null
                && WReference.WEREWOLF_FACTION.getID().equals(factionId);
    }

    public static boolean isHostileTarget(
            IFaction<?> source,
            LivingEntity target,
            boolean ignoreDisguise
    ) {
        return shouldProactivelyTarget(
                source,
                resolveFaction(target, ignoreDisguise)
        );
    }

    public static boolean shouldProactivelyTarget(
            @Nullable IFaction<?> source,
            @Nullable IFaction<?> target
    ) {
        return between(source, target) == Relationship.HOSTILE;
    }

    @Nullable
    public static IFaction<?> resolveFaction(
            LivingEntity entity,
            boolean ignoreDisguise
    ) {
        if (entity instanceof Player player) {
            FactionPlayerHandler handler = FactionPlayerHandler.get(player);
            if (ignoreDisguise) {
                return handler.getCurrentFaction();
            }
            Optional<? extends IFactionPlayer<?>> factionPlayer =
                    handler.getCurrentFactionPlayer();
            return factionPlayer.isPresent()
                    ? factionPlayer.get().getDisguisedAs()
                    : handler.getCurrentFaction();
        }

        if (entity instanceof IFactionEntity factionEntity) {
            return factionEntity.getFaction();
        }
        return VampirismAPI.factionRegistry().getFaction(entity);
    }

    public enum Relationship {
        ALLIED,
        NEUTRAL,
        HOSTILE
    }
}
