package org.kuro.tvdvampirism.registry;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;

public final class WhiteOakContent {
    private WhiteOakContent() {}
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("tvdvampirism");
    public static final DeferredItem<Item> WHITE_OAK_STAKE = ITEMS.registerSimpleItem("white_oak_stake", new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    public static void register(IEventBus bus) { ITEMS.register(bus); }
}
