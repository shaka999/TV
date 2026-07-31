package com.github.catvod.utils;

public class Github {

    // 升级源指向本 fork 自有仓库, 避免误装 FongMi 原版(会覆盖 fork 的弹幕/兼容改动)
    public static final String URL = "https://raw.githubusercontent.com/shaka999/TV/fongmi-4.0.7";

    private static String getUrl(String path, String name) {
        return URL + "/" + path + "/" + name;
    }

    public static String getJson(boolean dev, String name) {
        return getUrl("apk/" + (dev ? "dev" : "release"), name + ".json");
    }

    public static String getApk(boolean dev, String name) {
        return getUrl("apk/" + (dev ? "dev" : "release"), name + ".apk");
    }
}
