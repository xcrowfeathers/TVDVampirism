package org.kuro.tvdvampirism.compat;

import de.teamlapen.vampirism.config.VampirismConfig;
import de.teamlapen.werewolves.config.WerewolvesConfig;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import org.kuro.tvdvampirism.Tvdvampirism;
import org.kuro.tvdvampirism.config.ServerConfig;

/**
 * Changes only the dependencies' loaded balance values. Their own entity-join
 * handlers remain responsible for deciding whether to modify vanilla goals.
 */
@EventBusSubscriber(modid = Tvdvampirism.MODID)
public final class HostileTargetingConfigBridge {
    private record VampireSettings(boolean zombie, boolean creeper, boolean skeleton) {}

    private static VampireSettings originalVampireSettings;
    private static Boolean originalWerewolfSetting;

    private HostileTargetingConfigBridge() {}

    @SubscribeEvent
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        restoreLoadedValues();

        if (ServerConfig.OVERRIDE_VAMPIRISM_HOSTILE_IGNORE.get()) {
            var balance = VampirismConfig.BALANCE;
            originalVampireSettings = new VampireSettings(
                    balance.zombieIgnoreVampire.get(),
                    balance.creeperIgnoreVampire.get(),
                    balance.skeletonIgnoreVampire.get()
            );
            balance.zombieIgnoreVampire.set(false);
            balance.creeperIgnoreVampire.set(false);
            balance.skeletonIgnoreVampire.set(false);
        }

        if (ServerConfig.OVERRIDE_WEREWOLVES_HOSTILE_IGNORE.get()) {
            var setting = WerewolvesConfig.BALANCE.UTIL.skeletonIgnoreWerewolves;
            originalWerewolfSetting = setting.get();
            setting.set(false);
        }

        Tvdvampirism.LOGGER.info(
                "Hostile mob targeting config bridge applied (Vampirism: {}, Werewolves: {})",
                ServerConfig.OVERRIDE_VAMPIRISM_HOSTILE_IGNORE.get(),
                ServerConfig.OVERRIDE_WEREWOLVES_HOSTILE_IGNORE.get()
        );
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        restoreLoadedValues();
    }

    private static void restoreLoadedValues() {
        if (originalVampireSettings != null) {
            var balance = VampirismConfig.BALANCE;
            balance.zombieIgnoreVampire.set(originalVampireSettings.zombie());
            balance.creeperIgnoreVampire.set(originalVampireSettings.creeper());
            balance.skeletonIgnoreVampire.set(originalVampireSettings.skeleton());
            originalVampireSettings = null;
        }

        if (originalWerewolfSetting != null) {
            WerewolvesConfig.BALANCE.UTIL.skeletonIgnoreWerewolves.set(originalWerewolfSetting);
            originalWerewolfSetting = null;
        }
    }
}
