package org.kuro.tvdvampirism.blood;

import de.teamlapen.vampirism.entity.factions.FactionPlayerHandler;
import net.minecraft.world.entity.player.Player;
import org.kuro.tvdvampirism.faction.player.CustomFactionPlayer;
import de.teamlapen.vampirism.entity.vampire.DrinkBloodContext;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/** Central access point for blood owned by custom vampire-derived factions. */
public final class BloodManager {

    private BloodManager() {
    }

    public static Optional<CustomFactionPlayer<?>> getCustomVampire(Player player) {
        return FactionPlayerHandler.getCurrentFactionPlayer(player)
                .filter(CustomFactionPlayer.class::isInstance)
                .map(CustomFactionPlayer.class::cast);
    }

    public static Optional<BloodData> getBloodData(Player player) {
        return getCustomVampire(player).map(CustomFactionPlayer::getBloodData);
    }

    public static boolean setBlood(Player player, int amount) {
        return getCustomVampire(player).map(customPlayer -> {
            customPlayer.getBloodData().setBloodLevel(amount);
            customPlayer.syncBlood(false);
            return true;
        }).orElse(false);
    }

    public static boolean drain(Player player, int amount) {
        return getCustomVampire(player).map(customPlayer -> {
            customPlayer.getBloodData().removeBlood(amount, true);
            customPlayer.syncBlood(false);
            return true;
        }).orElse(false);
    }

    public static boolean refill(Player player) {
        return getCustomVampire(player).map(customPlayer -> {
            BloodData blood = customPlayer.getBloodData();
            blood.setBloodLevel(blood.getMaxBlood());
            customPlayer.syncBlood(false);
            return true;
        }).orElse(false);
    }

    public static boolean drinkFood(
            Player player,
            ItemStack stack,
            int amount,
            float saturationModifier
    ) {
        return getCustomVampire(player).map(customPlayer -> {
            if (!player.level().isClientSide) {
                customPlayer.drinkBlood(
                        amount,
                        saturationModifier,
                        new DrinkBloodContext(stack)
                );
                customPlayer.syncBlood(false);
            }

            return true;
        }).orElse(false);
    }
}
