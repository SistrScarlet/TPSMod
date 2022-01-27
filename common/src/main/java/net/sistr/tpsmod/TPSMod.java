package net.sistr.tpsmod;

import net.sistr.tpsmod.config.TPSModConfig;

public class TPSMod {
    public static final String MOD_ID = "tpsmod";

    public static void init() {
        TPSModConfig.ConfigSaveLoad.INSTANCE.load();
        TPSModConfig.ConfigSaveLoad.INSTANCE.save();
    }

}
