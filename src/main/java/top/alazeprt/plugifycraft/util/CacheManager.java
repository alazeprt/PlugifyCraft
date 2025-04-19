package top.alazeprt.plugifycraft.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.alazeprt.pclib.util.Plugin;
import top.alazeprt.pclib.util.SpigotPlugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

import static top.alazeprt.plugifycraft.PlugifyCraft.hangarRepo;
import static top.alazeprt.plugifycraft.PlugifyCraft.spigotRepo;

public class CacheManager {

    private static final List<CachePlugin> cachePluginList = new ArrayList<>();
    protected static final Queue<DownloadInfo> downloadQueue = new ArrayDeque<>();
    protected static final Queue<DownloadInfo> completedQueue = new ArrayDeque<>();
    protected static DownloadInfo currentDownloadInfo;
    protected static Thread downloadThread;
    private static long lastDownloadTime = -1;
    final static Logger logger = LoggerFactory.getLogger(CacheManager.class);

    public static String getStatus(DownloadInfo downloadInfo) {
        if (downloadQueue.contains(downloadInfo)) return "等待中";
        if (Objects.equals(currentDownloadInfo, downloadInfo)) return "下载中";
        if (completedQueue.contains(downloadInfo)) return "已完成";
        return "未知";
    }

    public static boolean download(Plugin plugin, int version, File path, int threadCount) throws IOException {
        logger.info("Preparing to download plugin {} of version {}", plugin.name, version);
        String platform = plugin instanceof SpigotPlugin ? "Spigot" : "Hangar";
        DownloadInfo downloadInfo = new DownloadInfo(plugin, platform, plugin.id, version, path, threadCount);
        for (CachePlugin cachePlugin : cachePluginList) {
            if (cachePlugin.id() == plugin.id && cachePlugin.platform().equals(platform) &&
                    cachePlugin.version() == version && cachePlugin.cacheFile() != null && cachePlugin.cacheFile().exists()) {
                logger.info("Found downloaded plugin in cache (plugin named {} of version {}), cache path: {}", plugin.name, version, cachePlugin.cacheFile().getAbsolutePath());
                Files.copy(cachePlugin.cacheFile().toPath(), new File(path, cachePlugin.cacheFile().getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
                completedQueue.add(downloadInfo);
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
        logger.info("Adding plugin download task to queue (plugin named {} of version {})", plugin.name, version);
        downloadQueue.add(downloadInfo);
        return false;
    }

    public static void load() throws FileNotFoundException {
        logger.info("Loading cache...");
        File file = new File(".plugifycraft/cache");
        downloadThread = new Thread(() -> {
            while (true) {
                if (downloadQueue.isEmpty()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignored) {}
                    continue;
                } else if (lastDownloadTime != -1 && System.currentTimeMillis() - lastDownloadTime < 1000 * 20) {
                    try {
                        Thread.sleep(1000 * 20 - (System.currentTimeMillis() - lastDownloadTime));
                    } catch (InterruptedException ignored) {}
                }
                DownloadInfo downloadInfo = downloadQueue.poll();
                logger.info("Receiving new download task: " + downloadInfo.plugin().name + " of version " + downloadInfo.version());
                currentDownloadInfo = downloadInfo;
                File cacheDir = new File(".plugifycraft/cache");
                File f = null;
                boolean success = false;
                try {
                    if (downloadInfo.platform().equals("Spigot")) {
                        f = spigotRepo.download(downloadInfo.pluginId(), downloadInfo.version(), 8, cacheDir);
                    } else {
                        f = hangarRepo.download(downloadInfo.pluginId(), downloadInfo.version(), 8, cacheDir);
                    }
                    lastDownloadTime = System.currentTimeMillis();
                    Files.copy(f.toPath(), new File(downloadInfo.path(), f.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
                    success = true;
                } catch (IOException e) {
                    logger.error("Failed to download plugin " + downloadInfo.plugin().name + " of version " + downloadInfo.version(), e);
                }
                if (success) cachePluginList.add(new CachePlugin(downloadInfo.platform(), downloadInfo.pluginId(), downloadInfo.version(), f));
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
        if (jsonObject == null) return;
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
        logger.info("Saving cache...");
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
            logger.debug("Saving plugin {} of version {} to cache", cachePlugin.platform(), cachePlugin.version());
            JsonObject cacheObject = new JsonObject();
            cacheObject.addProperty("platform", cachePlugin.platform());
            cacheObject.addProperty("id", cachePlugin.id());
            cacheObject.addProperty("version", cachePlugin.version());
            cacheObject.addProperty("path", cachePlugin.cacheFile().getAbsolutePath());
            jsonObject.add(cachePlugin.cacheFile().getName(), cacheObject);
        }
        Files.writeString(cacheConfig.toPath(), gson.toJson(jsonObject));
    }
}
