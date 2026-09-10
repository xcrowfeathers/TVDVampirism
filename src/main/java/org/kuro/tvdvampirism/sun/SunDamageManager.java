package org.kuro.tvdvampirism.sun;

import de.teamlapen.vampirism.REFERENCE;
import de.teamlapen.vampirism.config.VampirismConfig;
import de.teamlapen.vampirism.core.ModAttributes;
import de.teamlapen.vampirism.core.ModDamageTypes;
import de.teamlapen.vampirism.core.ModEffects;
import de.teamlapen.vampirism.core.ModItems;
import de.teamlapen.vampirism.entity.player.vampire.VampirePlayer;
import de.teamlapen.vampirism.util.DamageHandler;
import de.teamlapen.vampirism.util.Helper;
import de.teamlapen.vampirism.world.ModDamageSources;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.kuro.tvdvampirism.Tvdvampirism;
import org.kuro.tvdvampirism.blood.BloodManager;
import org.kuro.tvdvampirism.faction.player.AugustineVampirePlayer;
import org.kuro.tvdvampirism.faction.player.CustomFactionPlayer;
import org.kuro.tvdvampirism.faction.player.OriginalVampirePlayer;

import java.util.Map;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

@EventBusSubscriber(modid = Tvdvampirism.MODID)
public final class SunDamageManager {

    private static final int FULL_EXPOSURE = 100;
    private static final int SUN_FIRE_TICKS = 80;

    private static final Map<Player, SunState> STATES = new WeakHashMap<>();
    private static final Set<Player> SUN_IGNITED =
            Collections.newSetFromMap(new WeakHashMap<>());

    private SunDamageManager() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide && !player.isOnFire()) {
            SUN_IGNITED.remove(player);
        }
        CustomFactionPlayer<?> custom = BloodManager.getCustomVampire(player).orElse(null);

        if (custom == null) {
            STATES.remove(player);

            if (!player.level().isClientSide) {
                tickNormalVampire(player);
            }
            return;
        }

        // Stock Vampirism suspends sunlight processing while DBNO. Without this
        // guard custom species can be re-ignited even though DBNO rejects fire damage.
        if (org.kuro.tvdvampirism.compat.SpeciesCompatibility.rawVampire(player).isDBNO()) {
            STATES.remove(player);
            if (!player.level().isClientSide && player.isOnFire()) {
                player.clearFire();
            }
            return;
        }

        if (!isSunSensitive(custom) || custom.getLevel() <= 0) {
            STATES.remove(player);
            return;
        }

        if (org.kuro.tvdvampirism.compat.DaylightRingAccess.protectsFromSun(player)) {
            suppressSunlight(player);
            return;
        }

        SunState state = STATES.computeIfAbsent(player, ignored -> new SunState());
        updateExposure(player, state);

        if (player.level().isClientSide) {
            return;
        }

        ensureCustomSunDamage(player);

        if (!state.inSun) {
            return;
        }

        if (state.ticksInSun >= FULL_EXPOSURE) {
            ignite(player);
        }

        applyCustomSunEffects(player, custom, state);
    }


    public static int getTicksInSun(Player player) {
        SunState state = STATES.get(player);
        return state == null ? 0 : state.ticksInSun;
    }

    public static boolean isCustomSunSensitive(Player player) {
        return BloodManager.getCustomVampire(player)
                .map(SunDamageManager::isSunSensitive)
                .orElse(false);
    }

    private static void tickNormalVampire(Player player) {
        VampirePlayer vampire = VampirePlayer.get(player);

        if (vampire.getLevel() <= 0
                || vampire.getTicksInSun() < FULL_EXPOSURE
                || !vampire.isGettingSundamage(player.level(), false)) {
            return;
        }

        ignite(player);
    }

    private static void ignite(Player player) {
        if (!player.isOnFire()) {
            player.igniteForTicks(SUN_FIRE_TICKS);
            SUN_IGNITED.add(player);
        }
    }

    /** Clears only fire that this sunlight handler previously applied. */
    public static void suppressSunlight(Player player) {
        STATES.remove(player);
        if (!player.level().isClientSide
                && SUN_IGNITED.remove(player)
                && player.isOnFire()) {
            player.clearFire();
        }
    }

    private static void updateExposure(Player player, SunState state) {
        if (!state.initialized
                || player.tickCount % REFERENCE.REFRESH_SUNDAMAGE_TICKS == 0) {
            state.inSun = Helper.gettingSundamge(
                    player,
                    player.level(),
                    player.level().getProfiler()
            ) && player.getMainHandItem().getItem() != ModItems.UMBRELLA.get();

            state.initialized = true;
        }

        if (!state.inSun) {
            state.ticksInSun = Math.max(0, state.ticksInSun - 1);
            return;
        }

        MobEffectInstance sunscreen = player.getEffect(ModEffects.SUNSCREEN);
        int amplifier = sunscreen == null ? -1 : sunscreen.getAmplifier();

        if (state.ticksInSun < FULL_EXPOSURE) {
            state.ticksInSun++;
        }

        if (state.ticksInSun > 50
                && (amplifier >= 4
                || VampirismConfig.BALANCE.vpSunscreenBuff.get()
                && amplifier >= 0)) {
            state.ticksInSun = 50;
        }
    }

    private static void applyCustomSunEffects(
            Player player,
            CustomFactionPlayer<?> custom,
            SunState state
    ) {
        if (!player.isAlive()
                || player.getAbilities().instabuild
                || player.getAbilities().invulnerable) {
            return;
        }

        boolean original = custom instanceof OriginalVampirePlayer;
        int level = Math.max(1, custom.getLevel());

        MobEffectInstance sunscreen = player.getEffect(ModEffects.SUNSCREEN);
        int sunscreenLevel = sunscreen == null ? -1 : sunscreen.getAmplifier();

        if (!original
                && state.ticksInSun == FULL_EXPOSURE
                && VampirismConfig.BALANCE.vpSundamageInstantDeath.get()) {
            DamageHandler.kill(player, 100000);
            return;
        }

        if (VampirismConfig.BALANCE.vpSundamageNausea.get()
                && level >= VampirismConfig.BALANCE.vpSundamageNauseaMinLevel.get()
                && state.ticksInSun > 50
                && sunscreenLevel == -1
                && player.tickCount % 300 == 1) {
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 180));
        }

        if (level >= VampirismConfig.BALANCE.vpSundamageWeaknessMinLevel.get()
                && sunscreenLevel < 5
                && player.tickCount % 150 == 3) {
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 152));
        }

        if (level < VampirismConfig.BALANCE.vpSundamageMinLevel.get()
                || state.ticksInSun < FULL_EXPOSURE
                || player.tickCount % 40 != 5) {
            return;
        }

        AttributeInstance attribute = player.getAttribute(ModAttributes.SUNDAMAGE);

        if (attribute != null && attribute.getValue() > 0.0D) {
            DamageHandler.hurtModded(
                    player,
                    ModDamageSources::sunDamage,
                    (float) attribute.getValue()
            );
        }
    }

    private static void ensureCustomSunDamage(Player player) {
        AttributeInstance attribute = player.getAttribute(ModAttributes.SUNDAMAGE);

        if (attribute == null) {
            return;
        }

        double value = VampirismConfig.BALANCE.vpSundamage.get();

        if (Double.compare(attribute.getBaseValue(), value) != 0) {
            attribute.setBaseValue(value);
        }
    }

    private static boolean isSunSensitive(CustomFactionPlayer<?> player) {
        return player instanceof AugustineVampirePlayer
                || player instanceof OriginalVampirePlayer;
    }


    private static final class SunState {
        private boolean initialized;
        private boolean inSun;
        private int ticksInSun;
    }
}
