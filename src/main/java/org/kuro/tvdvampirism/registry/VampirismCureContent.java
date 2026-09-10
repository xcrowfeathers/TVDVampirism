package org.kuro.tvdvampirism.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.kuro.tvdvampirism.Tvdvampirism;
import org.kuro.tvdvampirism.effect.VampirismCureEffect;
import org.kuro.tvdvampirism.item.VampirismCureItem;

public final class VampirismCureContent {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Tvdvampirism.MODID);
    private static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, Tvdvampirism.MODID);

    public static final DeferredItem<VampirismCureItem> VAMPIRISM_CURE = ITEMS.register(
            "vampirism_cure",
            () -> new VampirismCureItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC))
    );
    public static final DeferredHolder<MobEffect, VampirismCureEffect> VAMPIRISM_CURE_EFFECT =
            EFFECTS.register("vampirism_cure", VampirismCureEffect::new);

    private VampirismCureContent() {}

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
        EFFECTS.register(bus);
    }
}
