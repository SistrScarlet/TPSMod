package net.sistr.tpsmod.fabric;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.text.TranslatableText;
import net.sistr.tpsmod.config.TPSModConfig;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            TPSModConfig.ConfigSaveLoad.INSTANCE.load();
            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(new TranslatableText("title.mobspawnlimit.config"));
            builder.setSavingRunnable(() -> {
                TPSModConfig.ConfigSaveLoad.INSTANCE.save();
            });
            ConfigCategory general = builder.getOrCreateCategory(new TranslatableText("category.mobspawnlimit.general"));
            ConfigEntryBuilder entryBuilder = builder.entryBuilder();

            general.addEntry(entryBuilder.startFloatField(new TranslatableText("option.tpsmod.camera_pos_yaw"), TPSModConfig.CAMERA_POS_YAW)
                    .setDefaultValue(15)
                    .setTooltip(new TranslatableText("option.tpsmod.camera_pos_yaw_tooltip"))
                    .setSaveConsumer(newValue -> TPSModConfig.CAMERA_POS_YAW = newValue)
                    .build());
            general.addEntry(entryBuilder.startFloatField(new TranslatableText("option.tpsmod.camera_pos_pitch"), TPSModConfig.CAMERA_POS_PITCH)
                    .setDefaultValue(5)
                    .setTooltip(new TranslatableText("option.tpsmod.camera_pos_pitch_tooltip"))
                    .setSaveConsumer(newValue -> TPSModConfig.CAMERA_POS_PITCH = newValue)
                    .build());
            general.addEntry(entryBuilder.startFloatField(new TranslatableText("option.tpsmod.camera_yaw"), TPSModConfig.CAMERA_YAW)
                    .setDefaultValue(0)
                    .setTooltip(new TranslatableText("option.tpsmod.camera_yaw_tooltip"))
                    .setSaveConsumer(newValue -> TPSModConfig.CAMERA_YAW = newValue)
                    .build());
            general.addEntry(entryBuilder.startFloatField(new TranslatableText("option.tpsmod.camera_pitch"), TPSModConfig.CAMERA_PITCH)
                    .setDefaultValue(0)
                    .setTooltip(new TranslatableText("option.tpsmod.camera_pitch_tooltip"))
                    .setSaveConsumer(newValue -> TPSModConfig.CAMERA_PITCH = newValue)
                    .build());
            general.addEntry(entryBuilder.startFloatField(new TranslatableText("option.tpsmod.camera_distance"), TPSModConfig.CAMERA_DISTANCE)
                    .setDefaultValue(2.5f)
                    .setTooltip(new TranslatableText("option.tpsmod.camera_distance_tooltip"))
                    .setSaveConsumer(newValue -> TPSModConfig.CAMERA_DISTANCE = newValue)
                    .build());

            return builder.build();
        };
    }

}
