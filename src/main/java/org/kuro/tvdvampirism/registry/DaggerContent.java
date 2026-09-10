package org.kuro.tvdvampirism.registry;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;

public final class DaggerContent {
    private DaggerContent() {}
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("tvdvampirism");
    public static final DeferredItem<Item> ELDER_DAGGER = ITEMS.register("elder_dagger", () -> new org.kuro.tvdvampirism.item.ElderDaggerItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
    public static final DeferredItem<Item> CURSED_ELDER_DAGGER = ITEMS.register("cursed_elder_dagger", () -> new org.kuro.tvdvampirism.item.ElderDaggerItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), true));
    public static void register(IEventBus bus) { ITEMS.register(bus); }
}
