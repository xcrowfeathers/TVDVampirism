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


/** This shit adapts custom species for Vampirism without changing their real faction or player data */
public final class SpeciesCompatibility {

    private SpeciesCompatibility() {
    }

    public static @Nullable CustomFactionPlayer<?> customPlayer(Player player) {
        if (player == null) {
            return null;
        }

        // Looking up the Vampire capability here would recurse for normal vampires.
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

    public static VampirePlayer vampire(Player player) {
        CustomFactionPlayer<?> custom = customPlayer(player);
        return custom != null && custom.hasVampireSkillBridge()
                ? custom.getVampireSkillBridge()
                : rawVampire(player);
    }

    /** Use the real attachment here; the public lookup would return this adapter again. */
    public static VampirePlayer rawVampire(Player player) {
        return player.getData(de.teamlapen.vampirism.core.ModAttachments.VAMPIRE_PLAYER);
    }

    public static WerewolfPlayer werewolf(Player player) {
        CustomFactionPlayer<?> custom = customPlayer(player);
        return custom != null && custom.hasWerewolfSkillBridge()
                ? custom.getWerewolfSkillBridge()
                : rawWerewolf(player);
    }

    /** Use the real attachment here; the public lookup would return this adapter again. */
    public static WerewolfPlayer rawWerewolf(Player player) {
        return player.getData(de.teamlapen.werewolves.core.ModAttachments.WEREWOLF_PLAYER);
    }

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

    /** Keep garlic effects without running the stock VampirePlayer tick twice. */
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

    /** Check inherited Vampire items here. Return null to let Vampirism make the decision. */
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
