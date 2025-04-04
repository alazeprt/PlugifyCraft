package top.alazeprt.plugifycraft.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import top.alazeprt.pclib.util.Plugin;
import top.alazeprt.pclib.util.SpigotPlugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

import static top.alazeprt.plugifycraft.PlugifyCraft.hangarRepo;
import static top.alazeprt.plugifycraft.PlugifyCraft.spigotRepo;

public class CacheManager {

    private static final List<CachePlugin> cachePluginList = new ArrayList<>();
    protected static final Queue<DownloadInfo> downloadQueue = new ArrayDeque<>();
    protected static final Queue<DownloadInfo> completedQueue = new ArrayDeque<>();
    protected static DownloadInfo currentDownloadInfo;
    private static Thread downloadThread;

    public static String getStatus(DownloadInfo downloadInfo) {
        if (downloadQueue.contains(downloadInfo)) return "等待中";
        if (Objects.equals(currentDownloadInfo, downloadInfo)) return "下载中";
        if (completedQueue.contains(downloadInfo)) return "已完成";
        return "未知";
    }

    public static boolean download(Plugin plugin, int version, File path) throws IOException {
        String platform = plugin instanceof SpigotPlugin ? "Spigot" : "Hangar";
        for (CachePlugin cachePlugin : cachePluginList) {
            if (cachePlugin.id() == plugin.id && cachePlugin.platform().equals(platform) &&
                    cachePlugin.version() == version && cachePlugin.cacheFile() != null && cachePlugin.cacheFile().exists()) {
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
        DownloadInfo downloadInfo = new DownloadInfo(plugin, platform, plugin.id, version, path, 8);
        downloadQueue.add(downloadInfo);
        return false;
    }

    public static void load() throws FileNotFoundException {
        File file = new File(".plugifycraft/cache");
        downloadThread = new Thread(() -> {
            while (true) {
                if (downloadQueue.isEmpty()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignored) {}
                    continue;
                }
                DownloadInfo downloadInfo = downloadQueue.poll();
                currentDownloadInfo = downloadInfo;
                File cacheDir = new File(".plugifycraft/cache");
                File f = null;
                try {
                    if (downloadInfo.platform().equals("Spigot")) {
                        f = spigotRepo.download(downloadInfo.pluginId(), downloadInfo.version(), 8, cacheDir);
                    } else {
                        f = hangarRepo.download(downloadInfo.pluginId(), downloadInfo.version(), 8, cacheDir);
                    }
                    Files.copy(f.toPath(), new File(downloadInfo.path(), file.getName()).toPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                cachePluginList.add(new CachePlugin(downloadInfo.platform(), downloadInfo.pluginId(), downloadInfo.version(), f));
                completedQueue.add(downloadInfo);
                if (completedQueue.size() > 10) {
                    completedQueue.poll();
                }
                currentDownloadInfo = null;
            }
        });
        downloadThread.start();
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
        if (downloadThread != null && downloadThread.isAlive()) {
            downloadThread.interrupt();
        }
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
