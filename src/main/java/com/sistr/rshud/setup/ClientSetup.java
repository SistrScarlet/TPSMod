package com.sistr.rshud.setup;

import com.sistr.rshud.RSHUDMod;
import com.sistr.rshud.block.ModificationWorkbenchScreen;
import net.minecraft.client.gui.ScreenManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = RSHUDMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {

    public static void init(FMLClientSetupEvent event) {
        ScreenManager.registerFactory(Registration.MOD_WORKBENCH_CONTAINER.get(), ModificationWorkbenchScreen::new);
    }
}
