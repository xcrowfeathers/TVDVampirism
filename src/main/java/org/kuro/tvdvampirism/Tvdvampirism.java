package org.kuro.tvdvampirism;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import org.kuro.tvdvampirism.config.ClientConfig;
import org.kuro.tvdvampirism.config.ServerConfig;
import org.kuro.tvdvampirism.faction.SpeciesFactions;
import org.kuro.tvdvampirism.registry.CreativeTab;
import org.kuro.tvdvampirism.registry.ModAttachments;
import org.slf4j.Logger;

@Mod(Tvdvampirism.MODID)
public final class Tvdvampirism {

    public static final String MODID = "tvdvampirism";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Tvdvampirism(IEventBus modBus, ModContainer modContainer) {

        ModAttachments.register(modBus);
        org.kuro.tvdvampirism.registry.TransformationContent.register(modBus);
        org.kuro.tvdvampirism.registry.BiteContent.register(modBus);
        org.kuro.tvdvampirism.registry.VampirismCureContent.register(modBus);
        org.kuro.tvdvampirism.registry.DaylightRingContent.register(modBus);
        org.kuro.tvdvampirism.registry.WhiteOakContent.register(modBus);
        org.kuro.tvdvampirism.registry.DaggerContent.register(modBus);
        org.kuro.tvdvampirism.loot.WorldLootModifier.register(modBus);
        org.kuro.tvdvampirism.mastery.MasteryContent.register(modBus);
        org.kuro.tvdvampirism.skill.CustomSkills.register(modBus);
        CreativeTab.TABS.register(modBus);
        modBus.addListener(this::enqueueInterModCompatibility);

        if (ModList.get().isLoaded("curios")) {
            org.kuro.tvdvampirism.compat.DaylightRingAccess.initializeCurios();
        }

        modContainer.registerConfig(
                ModConfig.Type.SERVER,
                ServerConfig.SPEC,
                "tvdvampirism-server.toml"
        );

        modContainer.registerConfig(
                ModConfig.Type.CLIENT,
                ClientConfig.SPEC,
                "tvdvampirism-client.toml"
        );

        LOGGER.info("TVD Vampirism initialized");
    }

    private void enqueueInterModCompatibility(InterModEnqueueEvent event) {
        SpeciesFactions.register();
        ModAttachments.registerVampirismIntegration();
    }
}
