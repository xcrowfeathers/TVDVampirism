package org.kuro.tvdvampirism.registry;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;
import org.kuro.tvdvampirism.Tvdvampirism;
import org.kuro.tvdvampirism.item.TransformationItem;
import org.kuro.tvdvampirism.player.TransformationManager.Reagent;
import org.kuro.tvdvampirism.player.TransformationManager.Type;

/** Minimal command-accessible content; no recipes, loot or custom rendering. */
public final class TransformationContent {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Tvdvampirism.MODID);
    private static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, Tvdvampirism.MODID);
    public static final DeferredItem<TransformationItem> AUGUSTINE_SYRINGE =
            ITEMS.register("augustine_syringe", () -> new TransformationItem(Reagent.AUGUSTINE_SYRINGE, new Item.Properties().stacksTo(16).rarity(Rarity.RARE)));
    public static final DeferredItem<TransformationItem> ORIGINAL_HYBRID_BLOOD =
            ITEMS.register("original_hybrid_blood", () -> new TransformationItem(Reagent.ORIGINAL_HYBRID_BLOOD, new Item.Properties().stacksTo(16).rarity(Rarity.RARE)));
    public static final DeferredItem<TransformationItem> INVINCIBILITY_CURSE_POTION =
            ITEMS.register("invincibility_curse_potion", () -> new TransformationItem(Reagent.INVINCIBILITY_CURSE, new Item.Properties().stacksTo(16).rarity(Rarity.EPIC)));

    private static final DeferredHolder<MobEffect, MobEffect> AUGUSTINE = effect("augustine_transformation");
    private static final DeferredHolder<MobEffect, MobEffect> HYBRID = effect("hybrid_transformation");
    private static final DeferredHolder<MobEffect, MobEffect> ORIGINAL = effect("original_vampire_transformation");
    private static final DeferredHolder<MobEffect, MobEffect> ORIGINAL_HYBRID = effect("original_hybrid_transformation");

    private TransformationContent() {}
    private static DeferredHolder<MobEffect, MobEffect> effect(String name) {
        return EFFECTS.register(name, () -> new MobEffect(MobEffectCategory.HARMFUL, 0x762438) {});
    }
    public static Holder<MobEffect> effect(Type type) {
        return switch (type) {
            case AUGUSTINE -> AUGUSTINE;
            case HYBRID -> HYBRID;
            case ORIGINAL -> ORIGINAL;
            case ORIGINAL_HYBRID -> ORIGINAL_HYBRID;
        };
    }
    public static void register(IEventBus bus) {
        ITEMS.register(bus);
        EFFECTS.register(bus);
    }
}
