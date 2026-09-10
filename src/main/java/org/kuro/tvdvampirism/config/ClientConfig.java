package org.kuro.tvdvampirism.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ClientConfig {

    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue SHOW_VAMPIRE_VEINS;
    public static final ModConfigSpec.BooleanValue SHOW_GLOWING_EYES;

    public static final ModConfigSpec.BooleanValue WOLF_BITE_SCREEN_EFFECTS;

    public static final ModConfigSpec.DoubleValue WOLF_BITE_SCREEN_EFFECT_INTENSITY;


    static {

        ModConfigSpec.Builder builder =
                new ModConfigSpec.Builder();


        builder.push("appearance");

        SHOW_VAMPIRE_VEINS = builder
                .define(
                        "show_vampire_veins",
                        true
                );

        SHOW_GLOWING_EYES = builder
                .define(
                        "show_glowing_eyes",
                        true
                );

        builder.pop();


        builder.push("effects");

        WOLF_BITE_SCREEN_EFFECTS = builder
                .define(
                        "wolf_bite_screen_effects",
                        true
                );

        WOLF_BITE_SCREEN_EFFECT_INTENSITY = builder
                .defineInRange(
                        "wolf_bite_screen_effect_intensity",
                        1.0D,
                        0D,
                        2D
                );

        builder.pop();


        SPEC = builder.build();
    }


    private ClientConfig() {
    }
}