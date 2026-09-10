package org.kuro.tvdvampirism.ability;

import de.teamlapen.vampirism.api.entity.factions.IPlayableFaction;
import de.teamlapen.vampirism.api.entity.player.IFactionPlayer;
import de.teamlapen.vampirism.api.entity.player.actions.DefaultAction;
import de.teamlapen.vampirism.core.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundEvents;
import org.kuro.tvdvampirism.compat.SpeciesCompatibility;
import org.kuro.tvdvampirism.config.ServerConfig;
import org.kuro.tvdvampirism.player.DeathPolicy;
import org.kuro.tvdvampirism.skill.CustomSkills;
import org.kuro.tvdvampirism.skill.CustomSkills.Definition;
import org.kuro.tvdvampirism.skill.CustomSkills.Kind;
import org.kuro.tvdvampirism.skill.SpeciesSkillProfile;
import java.util.Optional;

/** Stock action cooldowns/networking with one implementation per ability kind. */
public final class SpeciesAbility<T extends IFactionPlayer<T>> extends DefaultAction<T> {
    public final Definition definition;
    public SpeciesAbility(Definition definition) { this.definition=definition; }
    @Override public Optional<IPlayableFaction<?>> getFaction() { return Optional.empty(); }
    @Override public boolean isEnabled() { return definition.kind!=Kind.COMPULSION || ServerConfig.COMPULSION_ENABLED.get(); }
    @Override public int getCooldown(T owner) {
        return switch(definition.kind) {
            case LEAP -> (definition.species==SpeciesSkillProfile.HYBRID ? ServerConfig.HYBRID_LEAP_COOLDOWN : ServerConfig.ORIGINAL_HYBRID_LEAP_COOLDOWN).get()*20;
            case COMPULSION -> ServerConfig.COMPULSION_COOLDOWN.get()*20;
            case RIP_HEART -> ServerConfig.RIP_HEART_COOLDOWN.get()*20;
            default -> 0;
        };
    }
    @Override public boolean canBeUsedBy(T owner) {
        var player=owner.asEntity();
        return isEnabled() && CustomSkills.has(player,definition) && player.isAlive() && !player.isSpectator()
                && !SpeciesCompatibility.rawVampire(player).isDBNO() && !player.hasEffect(CustomSkills.LEAP);
    }
    @Override protected boolean activate(T owner, ActivationContext context) {
        if (!(owner.asEntity() instanceof ServerPlayer player) || !canBeUsedBy(owner)) return false;
        double range=switch(definition.kind) { case LEAP -> ServerConfig.LEAP_RANGE.get(); case COMPULSION -> 6; case INVENTORY -> 4; default -> 3; };
        var target=AbilityTargets.lookedAt(player,range);
        if (target==null || player.distanceToSqr(target)>range*range) return fail(player);
        if (definition.kind==Kind.INVENTORY) {
            if (!(target instanceof ServerPlayer other) || player.distanceToSqr(other)>16) return fail(player);
            InventorySightMenu.open(player,other);
            return true;
        }
        if (!AbilityTargets.canHarm(player,target)) return fail(player);
        boolean activated=switch(definition.kind) {
            case LEAP -> LeapEffect.start(player,target,definition);
            case COMPULSION -> {
                boolean compelled = AbilityTargets.canCompel(target)
                        && target.addEffect(new MobEffectInstance(CustomSkills.COMPELLED,
                        ServerConfig.COMPULSION_SECONDS.get()*20,0,false,false,false));
                if (compelled) AbilityTargets.immobilize(target);
                yield compelled;
            }
            case RIP_HEART -> {
                if (target.getHealth()>ServerConfig.RIP_HEART_HEALTH.get()) yield false;
                boolean hit=DeathPolicy.hurtWithHeartRip(player,target,Float.MAX_VALUE);
                if (hit) {
                    target.spawnAtLocation(new ItemStack(ModItems.HUMAN_HEART.get()));
                    target.playSound(SoundEvents.SLIME_SQUISH,1F,0.6F);
                }
                yield hit;
            }
            default -> false;
        };
        return activated || fail(player);
    }

    private static boolean fail(ServerPlayer player) {
        player.displayClientMessage(Component.translatable("text.vampirism.action.disallowed"),true);
        return false;
    }
}
