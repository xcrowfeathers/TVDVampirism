package org.kuro.tvdvampirism.compat;

import de.teamlapen.vampirism.config.VampirismConfig;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import org.kuro.tvdvampirism.Tvdvampirism;
import org.kuro.tvdvampirism.config.ServerConfig;

/** Uses Vampirism's own action toggle instead of maintaining a parallel bat-form restriction. */
@EventBusSubscriber(modid = Tvdvampirism.MODID)
public final class BatFormConfigBridge {
    private static Boolean originalBatEnabled;

    private BatFormConfigBridge() {}

    @SubscribeEvent
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        restoreLoadedValue();
        if (!ServerConfig.DISABLE_BAT_FORM.get()) return;

        var setting = VampirismConfig.BALANCE.vaBatEnabled;
        originalBatEnabled = setting.get();
        setting.set(false);
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        restoreLoadedValue();
    }

    private static void restoreLoadedValue() {
        if (originalBatEnabled == null) return;

        VampirismConfig.BALANCE.vaBatEnabled.set(originalBatEnabled);
        originalBatEnabled = null;
    }
}
