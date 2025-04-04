package top.alazeprt.plugifycraft.util;

import java.io.File;

public record CachePlugin(String platform, int id, int version, File cacheFile) {
}
