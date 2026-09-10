package org.kuro.tvdvampirism.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.kuro.tvdvampirism.Tvdvampirism;

import java.util.function.Supplier;

public final class CreativeTab {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Tvdvampirism.MODID);

    public static final Supplier<CreativeModeTab> MAIN = TABS.register("main", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.tvdvampirism.main"))
                    .icon(() -> new ItemStack(TransformationContent.ORIGINAL_HYBRID_BLOOD.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(TransformationContent.AUGUSTINE_SYRINGE.get());
                        output.accept(TransformationContent.ORIGINAL_HYBRID_BLOOD.get());
                        output.accept(TransformationContent.INVINCIBILITY_CURSE_POTION.get());
                        output.accept(VampirismCureContent.VAMPIRISM_CURE.get());
                        output.accept(DaylightRingContent.DAYLIGHT_RING.get());
                        output.accept(WhiteOakContent.WHITE_OAK_STAKE.get());
                        output.accept(DaggerContent.ELDER_DAGGER.get());
                        output.accept(DaggerContent.CURSED_ELDER_DAGGER.get());
                    })
                    .build());

    private CreativeTab() {}
}
