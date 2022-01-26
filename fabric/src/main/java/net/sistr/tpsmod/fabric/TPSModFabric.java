package net.sistr.tpsmod.fabric;

import net.fabricmc.api.ModInitializer;
import net.sistr.tpsmod.TPSMod;

public class TPSModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        TPSMod.init();
    }
}
