package org.kuro.tvdvampirism.compat;

import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;
import org.kuro.tvdvampirism.config.ServerConfig;
import org.kuro.tvdvampirism.player.Species;
import org.kuro.tvdvampirism.player.SpeciesManager;

import java.util.function.Predicate;

/** Optional, central access boundary for Daylight Ring equipment checks. */
public final class DaylightRingAccess {
    private static final boolean CURIOS_LOADED = ModList.get().isLoaded("curios");
    private static Predicate<Player> equippedLookup = player -> false;

    private DaylightRingAccess() {
    }

    /** Called only from the guarded Curios branch in the mod bootstrap. */
    public static void initializeCurios() {
        equippedLookup = CuriosDaylightRingAccess::isEquipped;
    }

    public static boolean isCuriosLoaded() {
        return CURIOS_LOADED;
    }

    public static boolean protectsFromSun(Player player) {
        if (!ServerConfig.DAYLIGHT_RING_ENABLED.get()) {
            return false;
        }

        Species species = SpeciesManager.getSpecies(player);
        if (species != Species.NORMAL
                && species != Species.AUGUSTINE
                && species != Species.ORIGINAL) {
            return false;
        }

        return equippedLookup.test(player);
    }
}
