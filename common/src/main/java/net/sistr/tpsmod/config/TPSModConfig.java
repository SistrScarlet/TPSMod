package net.sistr.tpsmod.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.architectury.platform.Platform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TPSModConfig {
    public static float CAMERA_POS_YAW = 15;
    public static float CAMERA_POS_PITCH = 5;
    public static float CAMERA_YAW = 0;
    public static float CAMERA_PITCH = 0;
    public static float CAMERA_DISTANCE = 2.5f;

    public static class ConfigSaveLoad {
        public static final ConfigSaveLoad INSTANCE = new ConfigSaveLoad();
        public float cameraPosYaw;
        public float cameraPosPitch;
        public float cameraYaw;
        public float cameraPitch;
        public float cameraDistance;

        public void load() {
            Path path = Paths.get(Platform.getConfigFolder().toString(), "tpsmod.json");
            //ファイル無かったら死ぬ
            if (Files.notExists(path)) {
                return;
            }

            try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                ConfigSaveLoad configSaveLoad = gson.fromJson(reader, ConfigSaveLoad.class);
                this.cameraPosYaw = configSaveLoad.cameraPosYaw;
                this.cameraPosPitch = configSaveLoad.cameraPosPitch;
                this.cameraYaw = configSaveLoad.cameraYaw;
                this.cameraPitch = configSaveLoad.cameraPitch;
                this.cameraDistance = configSaveLoad.cameraDistance;
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

        }

        public void save() {
            Path path = Paths.get(Platform.getConfigFolder().toString(), "tpsmod.json");
            //ファイルなかったら作る
            if (Files.notExists(path)) {
                try {
                    Files.createFile(path);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }

            try (OutputStream os = Files.newOutputStream(path)) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String setting = gson.toJson(this);
                os.write(setting.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }

    }
}
