package org.kuro.tvdvampirism.registry;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.kuro.tvdvampirism.Tvdvampirism;
import org.kuro.tvdvampirism.item.DaylightRingItem;

public final class DaylightRingContent {
    private static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(Tvdvampirism.MODID);

    public static final DeferredItem<Item> DAYLIGHT_RING = ITEMS.register(
            "daylight_ring",
            () -> new DaylightRingItem(
                    new Item.Properties().stacksTo(1).rarity(Rarity.RARE)
            )
    );

    private DaylightRingContent() {
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
