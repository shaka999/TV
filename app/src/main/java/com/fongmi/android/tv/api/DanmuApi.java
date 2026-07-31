package com.fongmi.android.tv.api;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.Constant;
import com.fongmi.android.tv.Setting;
import com.fongmi.android.tv.bean.Danmaku;
import com.github.catvod.net.OkHttp;
import com.google.gson.reflect.TypeToken;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class DanmuApi {

    private static boolean loading;

    public interface Callback {
        void onResult(List<Danmaku> items);
    }

    public static boolean isConfigured() {
        return !Setting.getDanmuApi().isEmpty() && Setting.isDanmuApiEnabled();
    }

    public static boolean shouldAutoLoad() {
        return isConfigured() && Setting.isDanmuApiAuto();
    }

    public static void autoLoad(String name, String episode, Callback callback) {
        if (!shouldAutoLoad()) {
            callback.onResult(new ArrayList<>());
            return;
        }
        load(name, episode, callback);
    }

    public static void manualLoad(String name, String episode, Callback callback) {
        if (!isConfigured()) {
            callback.onResult(new ArrayList<>());
            return;
        }
        load(name, episode, callback);
    }

    private static void load(String name, String episode, Callback callback) {
        String apiUrl = Setting.getDanmuApi();
        if (loading) {
            callback.onResult(new ArrayList<>());
            return;
        }
        loading = true;
        final boolean crawler = Setting.isDanmuApiCrawler();
        App.execute(() -> {
            try {
                String url = apiUrl.replaceAll("/+$", "") + "/api/v2/fongmi/danmaku?name=" + URLEncoder.encode(name, "UTF-8") + "&episode=" + URLEncoder.encode(episode != null ? episode : "", "UTF-8");
                String json = OkHttp.newCall(OkHttp.client(Constant.TIMEOUT_DANMAKU), url, "danmu_api").execute().body().string();
                List<Danmaku> result = App.gson().fromJson(json, new TypeToken<List<Danmaku>>() {}.getType());
                final List<Danmaku> items = new ArrayList<>();
                if (result != null) {
                    for (Danmaku item : result) {
                        if (crawler) item.setName("[聚合] " + item.getName());
                        items.add(item);
                    }
                }
                App.post(() -> {
                    loading = false;
                    callback.onResult(items);
                });
            } catch (Exception e) {
                e.printStackTrace();
                App.post(() -> {
                    loading = false;
                    callback.onResult(new ArrayList<>());
                });
            }
        });
    }
}
