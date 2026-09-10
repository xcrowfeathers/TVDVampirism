package org.kuro.tvdvampirism.client.visual;

import org.kuro.tvdvampirism.compat.SpeciesCompatibility;
import de.teamlapen.werewolves.entities.player.werewolf.WerewolfPlayer;
import de.teamlapen.werewolves.util.Helper;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.kuro.tvdvampirism.faction.player.CustomFactionPlayer;

/** Read-only routing for stock client visuals, including remote rendered players. */
public final class SpeciesClientVisualAccess {
    private SpeciesClientVisualAccess() {}

    private static @Nullable CustomFactionPlayer<?> custom(Player player) {
        return SpeciesCompatibility.customPlayer(player);
    }

    public static WerewolfPlayer werewolf(Player player) {
        CustomFactionPlayer<?> custom = custom(player);
        return custom != null && custom.hasWerewolfSkillBridge()
                ? custom.getWerewolfSkillBridge() : WerewolfPlayer.get(player);
    }

    public static boolean hasWerewolfVisuals(Player player) {
        if (player == null) return false;
        CustomFactionPlayer<?> custom = custom(player);
        return custom != null && custom.hasWerewolfSkillBridge() || Helper.isWerewolf(player);
    }

    public static int vampireVisualLevel(Player player, int stockLevel) {
        CustomFactionPlayer<?> custom = custom(player);
        return custom != null && custom.hasVampireSkillBridge() ? custom.getLevel() : stockLevel;
    }
}
