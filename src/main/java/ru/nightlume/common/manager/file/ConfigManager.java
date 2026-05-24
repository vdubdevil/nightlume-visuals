package ru.nightlume.common.manager.file;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import ru.nightlume.Nightlume;
import ru.nightlume.module.Module;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class ConfigManager {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public void saveConfig(String name) {
        File file = new File(FileManager.CONFIG_DIR, name + ".cfg");
        JsonObject root = new JsonObject();

        for (Module module : Nightlume.getInstance().getModuleManager().getModules()) {
            JsonObject modJson = new JsonObject();
            modJson.addProperty("enabled", module.isEnabled());
            modJson.addProperty("bind", module.getKey());
            root.add(module.getName(), modJson);
        }

        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(root, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadConfig(String name) {
        File file = new File(FileManager.CONFIG_DIR, name + ".cfg");
        if (!file.exists()) return;

        try (FileReader reader = new FileReader(file)) {
            JsonObject root = gson.fromJson(reader, JsonObject.class);

            for (Module module : Nightlume.getInstance().getModuleManager().getModules()) {
                if (root.has(module.getName())) {
                    JsonObject modJson = root.getAsJsonObject(module.getName());

                    if (modJson.has("enabled")) {
                        boolean enabled = modJson.get("enabled").getAsBoolean();
                        if (module.isEnabled() != enabled) module.toggle();
                    }
                    if (modJson.has("bind")) {
                        module.setKey(modJson.get("bind").getAsInt());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}