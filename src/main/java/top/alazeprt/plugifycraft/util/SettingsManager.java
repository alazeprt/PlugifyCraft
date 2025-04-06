package top.alazeprt.plugifycraft.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class SettingsManager {

    private static JsonObject jsonObject = new JsonObject();

    public static void load() throws IOException {
        File settings = checkFile();
        InputStreamReader reader = new InputStreamReader(new FileInputStream(settings), StandardCharsets.UTF_8);
        Gson gson = new Gson();
        JsonObject jsonObject1 = gson.fromJson(reader, JsonObject.class);
        if (jsonObject1 != null) {
            jsonObject = jsonObject1;
        }
    }

    public static void save() throws IOException {
        File settings = checkFile();
        Gson gson = new Gson();
        String json = gson.toJson(jsonObject);
        Files.write(settings.toPath(), json.getBytes(StandardCharsets.UTF_8));
    }

    private static File checkFile() throws IOException {
        File file = new File(".plugifycraft");
        if (!file.exists()) {
            file.mkdirs();
        }
        if (!file.isDirectory()) {
            file.delete();
            file.mkdirs();
        }
        File settings = new File(file, "settings.json");
        if (!settings.exists()) {
            settings.createNewFile();
        }
        return settings;
    }

    public static JsonElement get(String node) {
        try {
            String[] nodes = node.split("\\.");
            JsonObject current = jsonObject;
            for (int i = 0; i < nodes.length - 1; i++) {
                current = current.getAsJsonObject(nodes[i]);
            }
            return current.get(nodes[nodes.length - 1]);
        } catch (Exception e) {
            return null;
        }
    }

    public static void setString(String node, String value) {
        String[] nodes = node.split("\\.");
        JsonObject current = jsonObject;
        for (int i = 0; i < nodes.length - 1; i++) {
            current.add(nodes[i], new JsonObject());
            current = current.getAsJsonObject(nodes[i]);
        }
        current.addProperty(nodes[nodes.length - 1], value);
    }

    public static void setInt(String node, int value) {
        String[] nodes = node.split("\\.");
        JsonObject current = jsonObject;
        for (int i = 0; i < nodes.length - 1; i++) {
            current.add(nodes[i], new JsonObject());
            current = current.getAsJsonObject(nodes[i]);
        }
        current.addProperty(nodes[nodes.length - 1], value);
    }
}
