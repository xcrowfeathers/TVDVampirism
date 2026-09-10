package org.kuro.tvdvampirism.registry;

import de.teamlapen.lib.HelperRegistry;
import de.teamlapen.lib.lib.entity.IPlayerEventListener;
import de.teamlapen.lib.lib.storage.IAttachedSyncable;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.kuro.tvdvampirism.Tvdvampirism;
import org.kuro.tvdvampirism.faction.player.AugustineVampirePlayer;
import org.kuro.tvdvampirism.faction.player.CustomFactionPlayer;
import org.kuro.tvdvampirism.faction.player.HybridPlayer;
import org.kuro.tvdvampirism.faction.player.IAugustineVampirePlayer;
import org.kuro.tvdvampirism.faction.player.IHybridPlayer;
import org.kuro.tvdvampirism.faction.player.IOriginalHybridPlayer;
import org.kuro.tvdvampirism.faction.player.IOriginalVampirePlayer;
import org.kuro.tvdvampirism.faction.player.OriginalHybridPlayer;
import org.kuro.tvdvampirism.faction.player.OriginalVampirePlayer;
import org.kuro.tvdvampirism.player.PlayerData;

import java.util.function.Supplier;

public final class ModAttachments {

    private ModAttachments() {
    }

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(
                    NeoForgeRegistries.ATTACHMENT_TYPES,
                    Tvdvampirism.MODID
            );

    public static final Supplier<AttachmentType<PlayerData>> PLAYER_DATA =
            ATTACHMENTS.register(
                    "player_data",
                    () -> AttachmentType
                            .serializable(PlayerData::new)
                            .copyOnDeath()
                            .build()
            );

    public static final Supplier<AttachmentType<AugustineVampirePlayer>> AUGUSTINE_VAMPIRE_PLAYER =
            ATTACHMENTS.register(
                    Keys.AUGUSTINE_VAMPIRE_PLAYER.getPath(),
                    () -> AttachmentType
                            .builder(CustomFactionPlayer.factory(
                                    AugustineVampirePlayer::new,
                                    "Augustine Vampire"
                            ))
                            .serialize(CustomFactionPlayer.serializer(
                                    AugustineVampirePlayer::new,
                                    "Augustine Vampire"
                            ))
                            .copyOnDeath()
                            .build()
            );

    public static final Supplier<AttachmentType<HybridPlayer>> HYBRID_PLAYER =
            ATTACHMENTS.register(
                    Keys.HYBRID_PLAYER.getPath(),
                    () -> AttachmentType
                            .builder(CustomFactionPlayer.factory(
                                    HybridPlayer::new,
                                    "Hybrid"
                            ))
                            .serialize(CustomFactionPlayer.serializer(
                                    HybridPlayer::new,
                                    "Hybrid"
                            ))
                            .copyOnDeath()
                            .build()
            );

    public static final Supplier<AttachmentType<OriginalVampirePlayer>> ORIGINAL_VAMPIRE_PLAYER =
            ATTACHMENTS.register(
                    Keys.ORIGINAL_VAMPIRE_PLAYER.getPath(),
                    () -> AttachmentType
                            .builder(CustomFactionPlayer.factory(
                                    OriginalVampirePlayer::new,
                                    "Original Vampire"
                            ))
                            .serialize(CustomFactionPlayer.serializer(
                                    OriginalVampirePlayer::new,
                                    "Original Vampire"
                            ))
                            .copyOnDeath()
                            .build()
            );

    public static final Supplier<AttachmentType<OriginalHybridPlayer>> ORIGINAL_HYBRID_PLAYER =
            ATTACHMENTS.register(
                    Keys.ORIGINAL_HYBRID_PLAYER.getPath(),
                    () -> AttachmentType
                            .builder(CustomFactionPlayer.factory(
                                    OriginalHybridPlayer::new,
                                    "Original Hybrid"
                            ))
                            .serialize(CustomFactionPlayer.serializer(
                                    OriginalHybridPlayer::new,
                                    "Original Hybrid"
                            ))
                            .copyOnDeath()
                            .build()
            );

    public static void register(IEventBus modBus) {
        ATTACHMENTS.register(modBus);
    }

    public static void registerVampirismIntegration() {
        registerFactionPlayerAttachment(
                AUGUSTINE_VAMPIRE_PLAYER.get(),
                AugustineVampirePlayer.class
        );
        registerFactionPlayerAttachment(
                HYBRID_PLAYER.get(),
                HybridPlayer.class
        );
        registerFactionPlayerAttachment(
                ORIGINAL_VAMPIRE_PLAYER.get(),
                OriginalVampirePlayer.class
        );
        registerFactionPlayerAttachment(
                ORIGINAL_HYBRID_PLAYER.get(),
                OriginalHybridPlayer.class
        );
    }

    @SuppressWarnings("unchecked")
    public static AttachmentType<IAugustineVampirePlayer> augustineVampirePlayerType() {
        return (AttachmentType<IAugustineVampirePlayer>) (AttachmentType<?>) AUGUSTINE_VAMPIRE_PLAYER.get();
    }

    @SuppressWarnings("unchecked")
    public static AttachmentType<IHybridPlayer> hybridPlayerType() {
        return (AttachmentType<IHybridPlayer>) (AttachmentType<?>) HYBRID_PLAYER.get();
    }

    @SuppressWarnings("unchecked")
    public static AttachmentType<IOriginalVampirePlayer> originalVampirePlayerType() {
        return (AttachmentType<IOriginalVampirePlayer>) (AttachmentType<?>) ORIGINAL_VAMPIRE_PLAYER.get();
    }

    @SuppressWarnings("unchecked")
    public static AttachmentType<IOriginalHybridPlayer> originalHybridPlayerType() {
        return (AttachmentType<IOriginalHybridPlayer>) (AttachmentType<?>) ORIGINAL_HYBRID_PLAYER.get();
    }

    @SuppressWarnings("unchecked")
    private static <T extends CustomFactionPlayer<?>> void registerFactionPlayerAttachment(
            AttachmentType<T> attachmentType,
            Class<T> playerClass
    ) {
        HelperRegistry.registerPlayerEventReceivingCapability(
                (AttachmentType<IPlayerEventListener>) (AttachmentType<?>) attachmentType,
                playerClass
        );
        HelperRegistry.registerSyncablePlayerCapability(
                (AttachmentType<IAttachedSyncable>) (AttachmentType<?>) attachmentType,
                playerClass
        );
    }

    public static final class Keys {

        public static final ResourceLocation AUGUSTINE_VAMPIRE_PLAYER =
                id("augustine_vampire_player");
        public static final ResourceLocation HYBRID_PLAYER =
                id("hybrid_player");
        public static final ResourceLocation ORIGINAL_VAMPIRE_PLAYER =
                id("original_vampire_player");
        public static final ResourceLocation ORIGINAL_HYBRID_PLAYER =
                id("original_hybrid_player");

        private Keys() {
        }

        private static ResourceLocation id(String path) {
            return ResourceLocation.fromNamespaceAndPath(
                    Tvdvampirism.MODID,
                    path
            );
        }
    }
}
