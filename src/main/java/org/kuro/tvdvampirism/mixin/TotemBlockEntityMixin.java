package org.kuro.tvdvampirism.mixin;

import de.teamlapen.vampirism.api.entity.factions.IFaction;
import de.teamlapen.vampirism.api.entity.factions.IFactionRegistry;
import de.teamlapen.vampirism.api.entity.factions.IFactionVillage;
import de.teamlapen.vampirism.api.entity.factions.IPlayableFaction;
import de.teamlapen.vampirism.blockentity.TotemBlockEntity;
import de.teamlapen.vampirism.entity.player.VampirismPlayerAttributes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.kuro.tvdvampirism.faction.VampireFamily;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(TotemBlockEntity.class)
public abstract class TotemBlockEntityMixin {
    @org.spongepowered.asm.mixin.Shadow private IFaction<?> controllingFaction;

    @Redirect(method="spawnTaskMaster",at=@At(value="INVOKE",
            target="Lde/teamlapen/vampirism/api/entity/factions/IFactionVillage;getTaskMasterEntity()Lnet/minecraft/world/entity/EntityType;"))
    private net.minecraft.world.entity.EntityType<? extends de.teamlapen.vampirism.api.entity.ITaskMasterEntity> tvd$hybridQuestGiver(IFactionVillage village) {
        return VampireFamily.hasOwnershipOnlyVillage(controllingFaction)
                ? org.kuro.tvdvampirism.mastery.MasteryContent.HYBRID_QUEST_GIVER.get() : village.getTaskMasterEntity();
    }

    @org.spongepowered.asm.mixin.injection.ModifyArg(method="serverTickSecondNonCapture",at=@At(value="INVOKE",
            target="Lnet/minecraft/world/level/Level;getEntitiesOfClass(Ljava/lang/Class;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;"),index=2)
    private java.util.function.Predicate<? super Entity> tvd$matchingQuestGiver(java.util.function.Predicate<? super Entity> predicate) {
        // A quest giver left over from previous village ownership must not block the Hybrid NPC.
        return VampireFamily.hasOwnershipOnlyVillage(controllingFaction)
                ? entity -> entity instanceof org.kuro.tvdvampirism.mastery.HybridQuestGiver : predicate;
    }

    @Redirect(
            method = {
                    "canPlayerRemoveBlock",
                    "initiateCapture(Lnet/minecraft/world/entity/player/Player;)V",
                    "ringBell"
            },
            at = @At(
                    value = "FIELD",
                    target = "Lde/teamlapen/vampirism/entity/player/"
                            + "VampirismPlayerAttributes;faction:"
                            + "Lde/teamlapen/vampirism/api/entity/factions/"
                            + "IPlayableFaction;"
            )
    )
    private IPlayableFaction<?> useWorldFactionForPlayerVillageActions(
            VampirismPlayerAttributes attributes
    ) {
        return VampireFamily.getWorldPlayableFaction(attributes.faction);
    }

    @Redirect(
            method = {
                    "ringBell",
                    "serverTickSecondCapture",
                    "applyVictoryBonus"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lde/teamlapen/vampirism/api/entity/factions/"
                            + "IFactionRegistry;getFaction("
                            + "Lnet/minecraft/world/entity/Entity;)"
                            + "Lde/teamlapen/vampirism/api/entity/factions/"
                            + "IFaction;"
            )
    )
    private IFaction<?> useWorldFactionForVillageParticipation(
            IFactionRegistry registry,
            Entity entity
    ) {
        return VampireFamily.getWorldFaction(registry.getFaction(entity));
    }

    @Redirect(
            method = "serverTickSecondNonCapture",
            at = @At(
                    value = "INVOKE",
                    target = "Lde/teamlapen/vampirism/api/entity/factions/"
                            + "IFactionRegistry;getFactions()["
                            + "Lde/teamlapen/vampirism/api/entity/factions/"
                            + "IFaction;"
            )
    )
    private IFaction<?>[] excludeTechnicalSpeciesFromRandomRaids(
            IFactionRegistry registry
    ) {
        return Arrays.stream(registry.getFactions())
                .filter(faction -> !VampireFamily.isTechnicalSpecies(faction))
                .toArray(IFaction<?>[]::new);
    }

    @Redirect(
            method = "updateTileStatus",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/resources/ResourceLocation;"
                            + "equals(Ljava/lang/Object;)Z"
            )
    )
    private boolean keepMappedWorldFactionOwnership(
            ResourceLocation markerFactionId,
            Object expectedFactionId
    ) {
        return VampireFamily.worldFactionMatchesTotemMarker(
                markerFactionId,
                expectedFactionId
        );
    }

    @Redirect(
            method = "setControllingFaction",
            at = @At(
                    value = "INVOKE",
                    target = "Lde/teamlapen/vampirism/api/entity/factions/"
                            + "IFaction;getVillageData()"
                            + "Lde/teamlapen/vampirism/api/entity/factions/"
                            + "IFactionVillage;"
            )
    )
    private IFactionVillage useExistingTotemPresentation(
            IFaction<?> worldFaction
    ) {
        return VampireFamily.getVillagePresentationData(worldFaction);
    }

    @Inject(
            method = "spawnCaptureEntity",
            at = @At("HEAD"),
            cancellable = true
    )
    private void doNotSpawnOwnershipOnlyFactionEntities(
            IFaction<?> faction,
            CallbackInfo callback
    ) {
        if (VampireFamily.hasOwnershipOnlyVillage(faction)) {
            callback.cancel();
        }
    }
}
