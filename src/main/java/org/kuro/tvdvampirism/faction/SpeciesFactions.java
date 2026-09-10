package org.kuro.tvdvampirism.faction;

import de.teamlapen.vampirism.api.VReference;
import de.teamlapen.vampirism.api.VampirismAPI;
import de.teamlapen.vampirism.api.entity.factions.IFaction;
import de.teamlapen.vampirism.api.entity.factions.IPlayableFaction;
import de.teamlapen.vampirism.items.VampireRefinementItem;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import org.kuro.tvdvampirism.Tvdvampirism;
import org.kuro.tvdvampirism.faction.player.IAugustineVampirePlayer;
import org.kuro.tvdvampirism.faction.player.IHybridPlayer;
import org.kuro.tvdvampirism.faction.player.IOriginalHybridPlayer;
import org.kuro.tvdvampirism.faction.player.IOriginalVampirePlayer;
import org.kuro.tvdvampirism.registry.ModAttachments;
import org.kuro.tvdvampirism.player.Species;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class SpeciesFactions {

    public static final ResourceLocation AUGUSTINE_VAMPIRE_ID = id("augustine_vampire");
    public static final ResourceLocation HYBRID_ID = id("hybrid");
    public static final ResourceLocation ORIGINAL_VAMPIRE_ID = id("original_vampire");
    public static final ResourceLocation ORIGINAL_HYBRID_ID = id("original_hybrid");

    private static IPlayableFaction<IAugustineVampirePlayer> augustineVampire;
    private static IPlayableFaction<IHybridPlayer> hybrid;
    private static IPlayableFaction<IOriginalVampirePlayer> originalVampire;
    private static IPlayableFaction<IOriginalHybridPlayer> originalHybrid;

    private SpeciesFactions() {
    }

    public static void register() {
        if (augustineVampire != null) {
            return;
        }

        int highestLevel = Objects.requireNonNull(
                VReference.VAMPIRE_FACTION,
                "Vampirism's vampire faction must be registered first"
        ).getHighestReachableLevel();

        augustineVampire = VampirismAPI.factionRegistry()
                .createPlayableFaction(
                        AUGUSTINE_VAMPIRE_ID,
                        IAugustineVampirePlayer.class,
                        ModAttachments::augustineVampirePlayerType
                )
                .color(0x8B1E3F)
                .chatColor(ChatFormatting.RED)
                .name("text.tvdvampirism.augustine_vampire")
                .namePlural("text.tvdvampirism.augustine_vampires")
                .hostileTowardsNeutral()
                .highestLevel(highestLevel)
                .refinementItems(VampireRefinementItem::getItemForType)
                .register();

        hybrid = VampirismAPI.factionRegistry()
                .createPlayableFaction(
                        HYBRID_ID,
                        IHybridPlayer.class,
                        ModAttachments::hybridPlayerType
                )
                .color(0x8C5A2B)
                .chatColor(ChatFormatting.GOLD)
                .name("text.tvdvampirism.hybrid")
                .namePlural("text.tvdvampirism.hybrids")
                .hostileTowardsNeutral()
                .highestLevel(highestLevel)
                .refinementItems(VampireRefinementItem::getItemForType)
                .register();

        originalVampire = VampirismAPI.factionRegistry()
                .createPlayableFaction(
                        ORIGINAL_VAMPIRE_ID,
                        IOriginalVampirePlayer.class,
                        ModAttachments::originalVampirePlayerType
                )
                .color(0x722ABE)
                .chatColor(ChatFormatting.DARK_PURPLE)
                .name("text.tvdvampirism.original_vampire")
                .namePlural("text.tvdvampirism.original_vampires")
                .hostileTowardsNeutral()
                .highestLevel(highestLevel)
                .refinementItems(VampireRefinementItem::getItemForType)
                .register();

        originalHybrid = VampirismAPI.factionRegistry()
                .createPlayableFaction(
                        ORIGINAL_HYBRID_ID,
                        IOriginalHybridPlayer.class,
                        ModAttachments::originalHybridPlayerType
                )
                .color(0xC9461E)
                .chatColor(ChatFormatting.GOLD)
                .name("text.tvdvampirism.original_hybrid")
                .namePlural("text.tvdvampirism.original_hybrids")
                .hostileTowardsNeutral()
                .highestLevel(highestLevel)
                .refinementItems(VampireRefinementItem::getItemForType)
                .register();
    }

    public static IPlayableFaction<IAugustineVampirePlayer> augustineVampire() {
        return Objects.requireNonNull(augustineVampire, "Augustine Vampire faction is not registered");
    }

    public static IPlayableFaction<IHybridPlayer> hybrid() {
        return Objects.requireNonNull(hybrid, "Hybrid faction is not registered");
    }

    public static IPlayableFaction<IOriginalVampirePlayer> originalVampire() {
        return Objects.requireNonNull(originalVampire, "Original Vampire faction is not registered");
    }

    public static IPlayableFaction<IOriginalHybridPlayer> originalHybrid() {
        return Objects.requireNonNull(originalHybrid, "Original Hybrid faction is not registered");
    }

    @Nullable
    public static IPlayableFaction<?> forSpecies(Species species) {
        return switch (species) {
            case NONE -> null;
            case NORMAL -> VReference.VAMPIRE_FACTION;
            case AUGUSTINE -> augustineVampire();
            case HYBRID -> hybrid();
            case ORIGINAL -> originalVampire();
            case ORIGINAL_HYBRID -> originalHybrid();
        };
    }

    public static Species getSpecies(@Nullable IFaction<?> faction) {
        if (faction == null) {
            return Species.NONE;
        }

        ResourceLocation id = faction.getID();
        if (VReference.VAMPIRE_FACTION_ID.equals(id)) {
            return Species.NORMAL;
        }
        if (AUGUSTINE_VAMPIRE_ID.equals(id)) {
            return Species.AUGUSTINE;
        }
        if (HYBRID_ID.equals(id)) {
            return Species.HYBRID;
        }
        if (ORIGINAL_VAMPIRE_ID.equals(id)) {
            return Species.ORIGINAL;
        }
        if (ORIGINAL_HYBRID_ID.equals(id)) {
            return Species.ORIGINAL_HYBRID;
        }
        return Species.NONE;
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(
                Tvdvampirism.MODID,
                path
        );
    }
}
