package top.alazeprt.plugifycraft.util;

import top.alazeprt.pclib.util.Plugin;

import java.io.File;

public record DownloadInfo(Plugin plugin, String platform, int pluginId, int version, File path, int threadCount) {
}
