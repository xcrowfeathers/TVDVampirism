package org.kuro.tvdvampirism.skill;

import com.mojang.datafixers.util.Either;
import de.teamlapen.vampirism.api.VampirismRegistries;
import de.teamlapen.vampirism.api.entity.factions.*;
import de.teamlapen.vampirism.api.entity.player.IFactionPlayer;
import de.teamlapen.vampirism.api.entity.player.actions.IAction;
import de.teamlapen.vampirism.api.entity.player.skills.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.*;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;
import org.kuro.tvdvampirism.ability.SpeciesAbility;
import org.kuro.tvdvampirism.ability.LeapEffect;
import org.kuro.tvdvampirism.compat.SpeciesCompatibility;
import org.kuro.tvdvampirism.faction.player.CustomFactionPlayer;
import java.util.*;

/** One catalog owns species membership, point cost and the shared implementation selected by each skill. */
public final class CustomSkills {
    public enum Kind { ROOT, GARLIC, HOLY_WATER, RIP_HEART, LEAP, COMPULSION, INVENTORY }
    public enum Definition {
        AUGUSTINE_ROOT("augustine_root", SpeciesSkillProfile.AUGUSTINE, Kind.ROOT),
        GARLIC("garlic_resistance", SpeciesSkillProfile.AUGUSTINE, Kind.GARLIC),
        HOLY_WATER("holy_water_resistance", SpeciesSkillProfile.AUGUSTINE, Kind.HOLY_WATER),
        RIP_HEART("rip_heart", SpeciesSkillProfile.AUGUSTINE, Kind.RIP_HEART),
        HYBRID_ROOT("hybrid_root", SpeciesSkillProfile.HYBRID, Kind.ROOT),
        HYBRID_LEAP("hybrid_leap", SpeciesSkillProfile.HYBRID, Kind.LEAP),
        ORIGINAL_ROOT("original_root", SpeciesSkillProfile.ORIGINAL_VAMPIRE, Kind.ROOT),
        ORIGINAL_COMPULSION("original_compulsion", SpeciesSkillProfile.ORIGINAL_VAMPIRE, Kind.COMPULSION),
        INVENTORY_SIGHT("inventory_sight", SpeciesSkillProfile.ORIGINAL_VAMPIRE, Kind.INVENTORY),
        ORIGINAL_HYBRID_ROOT("original_hybrid_root", SpeciesSkillProfile.ORIGINAL_HYBRID, Kind.ROOT),
        ORIGINAL_HYBRID_LEAP("original_hybrid_leap", SpeciesSkillProfile.ORIGINAL_HYBRID, Kind.LEAP),
        ORIGINAL_HYBRID_COMPULSION("original_hybrid_compulsion", SpeciesSkillProfile.ORIGINAL_HYBRID, Kind.COMPULSION);

        public final String id;
        public final SpeciesSkillProfile species;
        public final Kind kind;
        public DeferredHolder<ISkill<?>, Skill<?>> skill;
        public DeferredHolder<IAction<?>, SpeciesAbility<?>> action;
        Definition(String id, SpeciesSkillProfile species, Kind kind) { this.id=id;this.species=species;this.kind=kind; }
        public boolean active() { return kind.ordinal() >= Kind.RIP_HEART.ordinal(); }
    }
    private static final DeferredRegister<ISkill<?>> SKILLS = DeferredRegister.create(VampirismRegistries.Keys.SKILL,"tvdvampirism");
    private static final DeferredRegister<IAction<?>> ACTIONS = DeferredRegister.create(VampirismRegistries.Keys.ACTION,"tvdvampirism");
    private static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT,"tvdvampirism");
    private static final DeferredRegister<net.minecraft.world.inventory.MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU,"tvdvampirism");
    public static final DeferredHolder<net.minecraft.world.inventory.MenuType<?>,net.minecraft.world.inventory.MenuType<org.kuro.tvdvampirism.ability.InventorySightMenu>> INVENTORY_MENU = MENUS.register("inventory_sight", () ->
            new net.minecraft.world.inventory.MenuType<>(org.kuro.tvdvampirism.ability.InventorySightMenu::new,net.minecraft.world.flag.FeatureFlags.DEFAULT_FLAGS));
    public static final DeferredHolder<MobEffect, LeapEffect> LEAP = EFFECTS.register("leaping", LeapEffect::new);
    public static final DeferredHolder<MobEffect, MobEffect> COMPELLED = EFFECTS.register("compelled", () ->
            new MobEffect(MobEffectCategory.HARMFUL,0x8065A0) {}
                    .addAttributeModifier(Attributes.MOVEMENT_SPEED, ResourceLocation.fromNamespaceAndPath("tvdvampirism","compelled"), -1,
                            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                    .addAttributeModifier(Attributes.KNOCKBACK_RESISTANCE, ResourceLocation.fromNamespaceAndPath("tvdvampirism","compelled_knockback"), 1,
                            AttributeModifier.Operation.ADD_VALUE));
    static {
        for (var definition : Definition.values()) {
            if (definition.active()) definition.action = ACTIONS.register(definition.id, () -> new SpeciesAbility<>(definition));
            definition.skill = SKILLS.register(definition.id, () -> definition.active() ? new ActiveSkill<>(definition) : new Skill<>(definition));
        }
    }
    private CustomSkills() {}
    public static void register(IEventBus bus) { SKILLS.register(bus); ACTIONS.register(bus); EFFECTS.register(bus); MENUS.register(bus); }
    public static String speciesId(SpeciesSkillProfile species) {
        return switch(species) { case AUGUSTINE -> "augustine_vampire"; case HYBRID -> "hybrid";
            case ORIGINAL_VAMPIRE -> "original_vampire"; case ORIGINAL_HYBRID -> "original_hybrid"; };
    }
    public static ResourceKey<ISkillTree> tree(SpeciesSkillProfile species) {
        return ResourceKey.create(VampirismRegistries.Keys.SKILL_TREE, ResourceLocation.fromNamespaceAndPath("tvdvampirism","custom/"+speciesId(species)));
    }
    public static boolean eligible(CustomFactionPlayer<?> owner, Definition definition) {
        return owner.getLevel()>0 && owner.getMasteryLevel()>=1 && owner.getSkillProfile()==definition.species;
    }
    public static boolean has(net.minecraft.world.entity.Entity entity, Definition definition) {
        if (!(entity instanceof net.minecraft.world.entity.player.Player player)) return false;
        var owner=SpeciesCompatibility.customPlayer(player);
        return owner!=null && eligible(owner,definition) && owner.getSkillHandler().isSkillEnabled(definition.skill.get());
    }
    public static class Skill<T extends IFactionPlayer<T>> extends DefaultSkill<T> {
        public final Definition definition;
        Skill(Definition definition) { super(definition.kind==Kind.ROOT?0:2); this.definition=definition; }
        @Override public Optional<IPlayableFaction<?>> getFaction() { return Optional.empty(); }
        @Override public Either<ResourceKey<ISkillTree>,TagKey<ISkillTree>> allowedSkillTrees() { return Either.left(tree(definition.species)); }
        @Override public Component getDescription() { return Component.translatable(getTranslationKey()+".desc"); }
        @Override @SuppressWarnings({"rawtypes","unchecked"}) protected void getActions(Collection<IAction<T>> actions) {
            if (definition.active()) actions.add((IAction)definition.action.get());
        }
        @Override protected void onEnabled(T owner) {
            if (definition==Definition.GARLIC) owner.asEntity().removeEffect(de.teamlapen.vampirism.core.ModEffects.GARLIC);
        }
    }
    public static final class ActiveSkill<T extends IFactionPlayer<T>> extends Skill<T> implements IActionSkill<T> {
        ActiveSkill(Definition definition) { super(definition); }
        @Override @SuppressWarnings({"rawtypes","unchecked"}) public IAction<T> action() { return (IAction)definition.action.get(); }
    }
}
