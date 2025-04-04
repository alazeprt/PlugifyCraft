package top.alazeprt.plugifycraft.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import top.alazeprt.pclib.util.Plugin;
import top.alazeprt.pclib.util.SpigotPlugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static top.alazeprt.plugifycraft.PlugifyCraft.hangarRepo;
import static top.alazeprt.plugifycraft.PlugifyCraft.spigotRepo;

public class CacheManager {

    private static List<CachePlugin> cachePluginList = new ArrayList<>();

    public static boolean download(Plugin plugin, int version, File path) throws IOException {
        String platform = plugin instanceof SpigotPlugin ? "Spigot" : "Hangar";
        for (CachePlugin cachePlugin : cachePluginList) {
            if (cachePlugin.id() == plugin.id && cachePlugin.platform().equals(platform) &&
                    cachePlugin.version() == version) {
                Files.copy(cachePlugin.cacheFile().toPath(), new File(path, cachePlugin.cacheFile().getName()).toPath());
                return true;
            }
        }
        File cacheDir = new File(".plugifycraft/cache");
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        if (!cacheDir.isDirectory()) {
            cacheDir.delete();
            cacheDir.mkdirs();
        }
        File file;
        if (platform.equals("Spigot")) {
            file = spigotRepo.download(plugin.id, version, 8, cacheDir);
        } else {
            file = hangarRepo.download(plugin.id, version, 8, cacheDir);
        }
        Files.copy(file.toPath(), new File(path, file.getName()).toPath());
        cachePluginList.add(new CachePlugin(platform, plugin.id, version, file));
        return false;
    }

    public static void load() throws FileNotFoundException {
        File file = new File(".plugifycraft/cache");
        if (!file.exists()) {
            file.mkdirs();
            return;
        }
        if (!file.isDirectory()) {
            file.delete();
            file.mkdirs();
            return;
        }
        File cacheConfig = new File(file, "cache.json");
        if (!cacheConfig.exists()) {
            return;
        }
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(new InputStreamReader(new FileInputStream(cacheConfig), StandardCharsets.UTF_8), JsonObject.class);
        jsonObject.entrySet().forEach(entry -> {
            String key = entry.getKey();
            JsonObject cacheObject = entry.getValue().getAsJsonObject();
            if (Files.exists(new File(cacheObject.get("path").getAsString()).toPath())) {
                String platform = cacheObject.get("platform").getAsString();
                int id = cacheObject.get("id").getAsInt();
                int version = cacheObject.get("version").getAsInt();
                File cacheFile = new File(file, key);
                cachePluginList.add(new CachePlugin(platform, id, version, cacheFile));
            }
        });
    }

    public static void save() throws IOException {
        File file = new File(".plugifycraft/cache");
        if (!file.exists()) {
            file.mkdirs();
        }
        if (!file.isDirectory()) {
            file.delete();
            file.mkdirs();
        }
        File cacheConfig = new File(file, "cache.json");
        if (!cacheConfig.exists()) {
            cacheConfig.createNewFile();
        }
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        for (CachePlugin cachePlugin : cachePluginList) {
            JsonObject cacheObject = new JsonObject();
            cacheObject.addProperty("platform", cachePlugin.platform());
            cacheObject.addProperty("id", cachePlugin.id());
            cacheObject.addProperty("version", cachePlugin.version());
            cacheObject.addProperty("path", cachePlugin.cacheFile().getAbsolutePath());
            jsonObject.add(cachePlugin.cacheFile().getName(), cacheObject);
        }
        Files.write(cacheConfig.toPath(), gson.toJson(jsonObject).getBytes(StandardCharsets.UTF_8));
    }
}
