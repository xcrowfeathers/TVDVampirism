package org.kuro.tvdvampirism.config;

public final class ServerConfigAccess {

    private ServerConfigAccess() {
    }

    public static boolean customSpeciesCanGainLordLevels() {

        return ServerConfig
                .CUSTOM_SPECIES_CAN_GAIN_LORD_LEVELS
                .get();
    }
}