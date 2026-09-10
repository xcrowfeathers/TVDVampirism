package org.kuro.tvdvampirism.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.kuro.tvdvampirism.Tvdvampirism;
import org.kuro.tvdvampirism.config.ServerConfig;
import org.kuro.tvdvampirism.registry.DaggerContent;
import org.kuro.tvdvampirism.registry.TransformationContent;
import org.kuro.tvdvampirism.registry.VampirismCureContent;
import org.kuro.tvdvampirism.registry.WhiteOakContent;
import org.kuro.tvdvampirism.registry.DaylightRingContent;

import java.util.List;
import java.util.function.Supplier;

/** Additive, config-backed rolls for existing TVD items. */
public final class WorldLootModifier extends LootModifier {
    private static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Tvdvampirism.MODID);

    public static final MapCodec<WorldLootModifier> CODEC = RecordCodecBuilder.mapCodec(instance ->
            codecStart(instance).apply(instance, WorldLootModifier::new));

    private static final ResourceLocation WOODLAND_MANSION = table("minecraft", "chests/woodland_mansion");
    private static final ResourceLocation BURIED_TREASURE = table("minecraft", "chests/buried_treasure");
    private static final ResourceLocation ANCIENT_CITY = table("minecraft", "chests/ancient_city");
    private static final ResourceLocation OMINOUS_VAULT = table("minecraft", "chests/trial_chambers/reward_ominous");
    private static final ResourceLocation HUNTER_OUTPOST_TENT = table("vampirism", "chests/hunter_outpost_tent");

    private static final List<Entry> ENTRIES = List.of(
            new Entry(WOODLAND_MANSION, WhiteOakContent.WHITE_OAK_STAKE,
                    ServerConfig.WHITE_OAK_STAKE_LOOT_ENABLED, ServerConfig.WHITE_OAK_STAKE_LOOT_CHANCE),
            new Entry(BURIED_TREASURE, DaggerContent.ELDER_DAGGER,
                    ServerConfig.ELDER_DAGGER_LOOT_ENABLED, ServerConfig.ELDER_DAGGER_LOOT_CHANCE),
            new Entry(ANCIENT_CITY, DaggerContent.CURSED_ELDER_DAGGER,
                    ServerConfig.CURSED_ELDER_DAGGER_LOOT_ENABLED, ServerConfig.CURSED_ELDER_DAGGER_LOOT_CHANCE),
            new Entry(OMINOUS_VAULT, VampirismCureContent.VAMPIRISM_CURE,
                    ServerConfig.VAMPIRISM_CURE_LOOT_ENABLED, ServerConfig.VAMPIRISM_CURE_LOOT_CHANCE),
            new Entry(HUNTER_OUTPOST_TENT, TransformationContent.AUGUSTINE_SYRINGE,
                    ServerConfig.AUGUSTINE_SYRINGE_LOOT_ENABLED, ServerConfig.AUGUSTINE_SYRINGE_LOOT_CHANCE),
            new Entry(WOODLAND_MANSION, TransformationContent.ORIGINAL_HYBRID_BLOOD,
                    ServerConfig.ORIGINAL_HYBRID_BLOOD_LOOT_ENABLED, ServerConfig.ORIGINAL_HYBRID_BLOOD_LOOT_CHANCE),
            new Entry(BURIED_TREASURE, TransformationContent.INVINCIBILITY_CURSE_POTION,
                    ServerConfig.IMMORTALITY_SERUM_LOOT_ENABLED, ServerConfig.IMMORTALITY_SERUM_LOOT_CHANCE),
            new Entry(WOODLAND_MANSION, DaylightRingContent.DAYLIGHT_RING,
                    ServerConfig.DAYLIGHT_RING_LOOT_ENABLED, ServerConfig.DAYLIGHT_RING_LOOT_CHANCE)
    );

    static {
        SERIALIZERS.register("world_items", () -> CODEC);
    }

    private WorldLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        ResourceLocation table = context.getQueriedLootTableId();
        for (Entry entry : ENTRIES) {
            if (entry.table().equals(table)
                    && entry.enabled().get()
                    && context.getRandom().nextDouble() < entry.chance().get()) {
                generatedLoot.add(new ItemStack(entry.item().get()));
            }
        }
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }

    public static void register(IEventBus bus) {
        SERIALIZERS.register(bus);
    }

    private static ResourceLocation table(String namespace, String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }

    private record Entry(
            ResourceLocation table,
            Supplier<? extends Item> item,
            net.neoforged.neoforge.common.ModConfigSpec.BooleanValue enabled,
            net.neoforged.neoforge.common.ModConfigSpec.DoubleValue chance
    ) {
    }
}
