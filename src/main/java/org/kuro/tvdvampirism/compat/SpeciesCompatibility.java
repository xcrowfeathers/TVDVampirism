package org.kuro.tvdvampirism.compat;

import de.teamlapen.vampirism.api.VReference;
import de.teamlapen.vampirism.api.entity.factions.IFaction;
import de.teamlapen.vampirism.api.entity.factions.IFactionPlayerHandler;
import de.teamlapen.vampirism.api.entity.factions.IPlayableFaction;
import de.teamlapen.vampirism.api.entity.player.skills.ISkill;
import de.teamlapen.vampirism.api.items.IFactionLevelItem;
import de.teamlapen.vampirism.entity.factions.FactionPlayerHandler;
import de.teamlapen.vampirism.entity.player.vampire.VampirePlayer;
import de.teamlapen.werewolves.entities.player.werewolf.WerewolfPlayer;
import de.teamlapen.werewolves.api.WReference;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.kuro.tvdvampirism.faction.player.CustomFactionPlayer;
import org.kuro.tvdvampirism.faction.SpeciesFactions;
import org.kuro.tvdvampirism.player.Species;


/**
 * Central compatibility boundary between TVD custom species and stock
 * Vampirism code that semantically expects a vampire player.
 *
 * This class never changes the player's real faction. CustomFactionPlayer
 * remains authoritative; the Vampire bridge is only an adapter for stock APIs.
 */
public final class SpeciesCompatibility {

    private SpeciesCompatibility() {
    }

    public static @Nullable CustomFactionPlayer<?> customPlayer(Player player) {
        if (player == null) {
            return null;
        }

        // Do not resolve the stock Vampire capability while deciding whether to
        // adapt that same capability (normal vampires would recurse).
        if (!org.kuro.tvdvampirism.faction.VampireFamily.isTechnicalSpecies(
                FactionPlayerHandler.get(player).getCurrentFaction())) {
            return null;
        }

        return FactionPlayerHandler.getCurrentFactionPlayer(player)
                .filter(CustomFactionPlayer.class::isInstance)
                .map(value -> (CustomFactionPlayer<?>) value)
                .filter(value -> value.getLevel() > 0)
                .orElse(null);
    }

    public static boolean isCustomVampireLike(Player player) {
        CustomFactionPlayer<?> custom = customPlayer(player);
        return custom != null && custom.hasVampireSkillBridge();
    }

    public static boolean isCustomVampireLike(Entity entity) {
        return entity instanceof Player player && isCustomVampireLike(player);
    }

    public static boolean isCustomWerewolfLike(Player player) {
        CustomFactionPlayer<?> custom = customPlayer(player);
        return custom != null && custom.hasWerewolfSkillBridge();
    }

    public static boolean isCustomWerewolfLike(Entity entity) {
        return entity instanceof Player player && isCustomWerewolfLike(player);
    }

    /**
     * Returns the stock-compatible VampirePlayer facade for a player.
     * For custom species this is the existing bridge backed by the canonical
     * CustomFactionPlayer state; normal vampires keep their stock attachment.
     */
    public static VampirePlayer vampire(Player player) {
        CustomFactionPlayer<?> custom = customPlayer(player);
        return custom != null && custom.hasVampireSkillBridge()
                ? custom.getVampireSkillBridge()
                : rawVampire(player);
    }

    /** Persistence and bridge delegation must bypass the public facade lookup. */
    public static VampirePlayer rawVampire(Player player) {
        return player.getData(de.teamlapen.vampirism.core.ModAttachments.VAMPIRE_PLAYER);
    }

    /** Stock-compatible Werewolf facade backed by the custom player's canonical state. */
    public static WerewolfPlayer werewolf(Player player) {
        CustomFactionPlayer<?> custom = customPlayer(player);
        return custom != null && custom.hasWerewolfSkillBridge()
                ? custom.getWerewolfSkillBridge()
                : rawWerewolf(player);
    }

    /** Persistence and bridge delegation must bypass the public facade lookup. */
    public static WerewolfPlayer rawWerewolf(Player player) {
        return player.getData(de.teamlapen.werewolves.core.ModAttachments.WEREWOLF_PLAYER);
    }

    /**
     * Extends stock refinement ownership only for TVD custom factions. Normal
     * factions retain Vampirism's exact-faction validation.
     */
    public static boolean acceptsRefinementFaction(
            IPlayableFaction<?> playerFaction,
            Object refinementFaction
    ) {
        if (playerFaction.equals(refinementFaction)) {
            return true;
        }
        if (!(refinementFaction instanceof IFaction<?> exclusiveFaction)) {
            return false;
        }

        Species species = SpeciesFactions.getSpecies(playerFaction);
        if (species == Species.AUGUSTINE || species == Species.ORIGINAL) {
            return VReference.VAMPIRE_FACTION.equals(exclusiveFaction);
        }
        if (species == Species.HYBRID || species == Species.ORIGINAL_HYBRID) {
            return VReference.VAMPIRE_FACTION.equals(exclusiveFaction)
                    || WReference.WEREWOLF_FACTION.equals(exclusiveFaction);
        }
        return false;
    }

    /** Reuse ambient garlic without running the stock VampirePlayer tick twice. */
    public static void tickVampireEnvironment(CustomFactionPlayer<?> custom) {
        var player = custom.asEntity();
        if (!player.isAlive()) return;
        var vampire = custom.getVampireSkillBridge();
        var strength = vampire.isGettingGarlicDamage(player.level(), player.tickCount % 40 == 0);
        if (strength != de.teamlapen.vampirism.api.EnumStrength.NONE) {
            de.teamlapen.vampirism.util.DamageHandler.affectVampireGarlicAmbient(
                    vampire, strength, player.tickCount);
        }
    }

    /**
     * Handles Vampirism's generic IFactionLevelItem gate for inherited
     * Vampire items. Returns null when stock logic should remain authoritative.
     */
    public static @Nullable Boolean canUseInheritedFactionItem(
            ItemStack stack,
            IFactionLevelItem<?> item,
            IFactionPlayerHandler handler
    ) {
        var current = handler.getCurrentFactionPlayer().orElse(null);

        if (!(current instanceof CustomFactionPlayer<?> custom)
                || custom.getLevel() <= 0) {
            return null;
        }

        IFaction<?> exclusiveFaction = item.getExclusiveFaction(stack);

        if (!VReference.VAMPIRE_FACTION.equals(exclusiveFaction)) {
            return null;
        }

        if (!custom.hasVampireSkillBridge()
                || custom.getLevel() < item.getMinLevel(stack)) {
            return false;
        }

        ISkill<?> requiredSkill = item.getRequiredSkill(stack);
        return requiredSkill == null || isSkillEnabled(custom, requiredSkill);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static boolean isSkillEnabled(
            CustomFactionPlayer<?> player,
            ISkill<?> skill
    ) {
        return player.getSkillHandler().isSkillEnabled((ISkill) skill);
    }
}
