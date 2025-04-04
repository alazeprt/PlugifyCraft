package top.alazeprt.plugifycraft.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import top.alazeprt.pclib.util.Author;
import top.alazeprt.pclib.util.HangarPlugin;
import top.alazeprt.pclib.util.Plugin;
import top.alazeprt.pclib.util.SpigotPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.DosFileAttributeView;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static top.alazeprt.plugifycraft.PlugifyCraft.hangarRepo;
import static top.alazeprt.plugifycraft.PlugifyCraft.spigotRepo;

public class StarManager {

    // instance

    private final GridPane starPane;
    private final Label label; // 提示消息
    private final ChoiceBox<String> starFolders; // 收藏夹选择框
    private final PluginPaneManager pluginPaneManager;

    public StarManager(GridPane starPane, Label label, ChoiceBox<String> starFolders, PluginPaneManager pluginPaneManager) {
        this.starPane = starPane;
        this.label = label;
        this.starFolders = starFolders;
        this.pluginPaneManager = pluginPaneManager;
    }

    public void addAllDataToPane() {
        if (starFolders.getValue() == null || starFolders.getValue().isBlank()) {
            return;
        }
        List<Plugin> stars = StarManager.getFolder(starFolders.getValue());
        int maxCol = 3;
        int maxRow = stars.size() % maxCol == 0 ? stars.size() / maxCol : stars.size() / maxCol + 1;
        if (maxRow > starPane.getRowCount()) {
            for (int i = 1; i <= maxRow - starPane.getRowCount(); i++) {
                addLine(starPane);
            }
        }
        int col = 0, row = 0;
        for (Plugin plugin : stars) {
            starPane.add(pluginPaneManager.getPluginPane(plugin), col, row);
            col++;
            if (col == maxCol) {
                col = 0;
                row++;
            }
        }
    }

    public void addSearchDataToPane(String keyword) {
        List<Plugin> oldStars = StarManager.getFolder(starFolders.getValue());
        List<Plugin> stars = new ArrayList<>();
        for (Plugin plugin : oldStars) {
            if (plugin.name.toLowerCase().contains(keyword.toLowerCase())) {
                stars.add(plugin);
            }
        }
        starPane.getChildren().clear();
        int maxCol = 3;
        int maxRow = stars.size() % maxCol == 0 ? stars.size() / maxCol : stars.size() / maxCol + 1;
        if (maxRow > starPane.getRowCount()) {
            for (int i = 1; i <= maxRow - starPane.getRowCount(); i++) {
                addLine(starPane);
            }
        }
        int col = 0, row = 0;
        for (Plugin plugin : stars) {
            starPane.add(pluginPaneManager.getPluginPane(plugin), col, row);
            col++;
            if (col == maxCol) {
                col = 0;
                row++;
            }
        }
    }

    public void addLine(GridPane pane) {
        pane.setPrefHeight(pane.getPrefHeight() + 20 + 415/4.0);
        pane.addRow(pane.getRowCount());
    }

    // static

    public static Map<String, List<Plugin>> starMap = new ConcurrentHashMap<>();
    public static Thread thread;

    public static void handle(String folder, Plugin plugin) {
        if (!starMap.containsKey(folder) || starMap.get(folder) == null || starMap.get(folder).isEmpty()) {
            List<Plugin> list = new ArrayList<>();
            list.add(plugin);
            starMap.put(folder, list);
        } else {
            for (Plugin p : starMap.get(folder)) {
                if (p.id == plugin.id) {
                    starMap.get(folder).remove(p);
                    return;
                }
            }
            starMap.get(folder).add(plugin);
        }
    }

    public static List<Plugin> getFolder(String folder) {
        return starMap.get(folder);
    }

    public static boolean isStarred(String folder, Plugin plugin) {
        return starMap.get(folder).contains(plugin);
    }

    public static void createFolder(String folder) {
        if (!starMap.containsKey(folder)) {
            starMap.put(folder, new ArrayList<>());
        }
    }

    public static void clear() {
        starMap.clear();
    }

    public static void save() throws IOException {
        if (thread != null && !thread.isInterrupted()) thread.interrupt();
        File folder = new File(".plugifycraft");
        folder.mkdirs();
        DosFileAttributeView attributes = Files.getFileAttributeView(folder.toPath(), DosFileAttributeView.class);
        attributes.setHidden(true);
        File file = new File(folder, "stars.json");
        folder.createNewFile();
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        for (Map.Entry<String, List<Plugin>> entry : starMap.entrySet()) {
            List<JsonObject> plugins = new ArrayList<>();
            for (Plugin plugin : entry.getValue()) {
                JsonObject jsonObject1 = new JsonObject();
                jsonObject1.addProperty("id", plugin.id);
                jsonObject1.addProperty("name", plugin.name);
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                jsonObject1.addProperty("updateDate", dateFormat.format(plugin.updateDate));
                jsonObject1.addProperty("releaseDate", dateFormat.format(plugin.releaseDate));
                jsonObject1.addProperty("author", plugin.author.name);
                jsonObject1.addProperty("category", plugin.category);
                jsonObject1.addProperty("downloads", plugin.downloads);
                jsonObject1.addProperty("image", plugin.image);
                jsonObject1.addProperty("platform", plugin instanceof SpigotPlugin ? "Spigot" : "Hangar");
                plugins.add(jsonObject1);
            }
            jsonObject.add(entry.getKey(), gson.toJsonTree(plugins));
        }
        Files.writeString(file.toPath(), gson.toJson(jsonObject), StandardCharsets.UTF_8);
    }

    public static void load() throws IOException, ParseException {
        File folder = new File(".plugifycraft");
        File file = new File(folder, "stars.json");
        if (file.exists()) {
            String json = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                List<Plugin> plugins = new ArrayList<>();
                for (JsonElement jsonElement : entry.getValue().getAsJsonArray()) {
                    JsonObject jsonObject1 = jsonElement.getAsJsonObject();
                    int id = jsonObject1.get("id").getAsInt();
                    String platform = jsonObject1.get("platform").getAsString();
                    String name = jsonObject1.get("name").getAsString();
                    String author = jsonObject1.get("author").getAsString();
                    String category = jsonObject1.get("category").getAsString();
                    String updateDate = jsonObject1.get("updateDate").getAsString();
                    String releaseDate = jsonObject1.get("releaseDate").getAsString();
                    String image = jsonObject1.get("image").getAsString();
                    int downloads = jsonObject1.get("downloads").getAsInt();
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Plugin plugin = platform.equals("Spigot") ? new SpigotPlugin(spigotRepo, id) : new HangarPlugin(hangarRepo, id);
                    plugin.name = name;
                    plugin.updateDate = dateFormat.parse(updateDate);
                    plugin.releaseDate = dateFormat.parse(releaseDate);
                    plugin.downloads = downloads;
                    int authorId = -1;
                    try {
                        authorId = Integer.parseInt(author);
                    } catch (NumberFormatException ignored) {}
                    plugin.author = new Author(spigotRepo, authorId, author, "");
                    plugin.category = category;
                    plugin.description = "";
                    plugin.image = image;
                    plugins.add(plugin);
                }
                starMap.put(entry.getKey(), plugins);
            }
            thread = new Thread(() -> {
                for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                    List<Plugin> plugins = new ArrayList<>();
                    for (JsonElement jsonElement : entry.getValue().getAsJsonArray()) {
                        JsonObject jsonObject1 = jsonElement.getAsJsonObject();
                        int id = jsonObject1.get("id").getAsInt();
                        String platform = jsonObject1.get("platform").getAsString();
                        try {
                            Plugin plugin = platform.equals("Spigot") ? spigotRepo.getPlugin(id) : hangarRepo.getPlugin(id);
                            for (Plugin p : plugins) {
                                if (p.id == id) {
                                    plugins.remove(p);
                                    break;
                                }
                            }
                            plugins.add(plugin);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    starMap.put(entry.getKey(), plugins);
                }
            });
            thread.start();
        } else {
            starMap.put("默认收藏夹", new ArrayList<>());
        }
    }

    public static boolean contains(String folder) {
        return starMap.containsKey(folder);
    }
}
