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

    public static void load(String name, String episode, Callback callback) {
        String apiUrl = Setting.getDanmuApi();
        if (apiUrl.isEmpty() || !Setting.isDanmuApiEnabled()) {
            callback.onResult(new ArrayList<>());
            return;
        }
        if (loading) {
            callback.onResult(new ArrayList<>());
            return;
        }
        loading = true;
        App.execute(() -> {
            try {
                String url = apiUrl.replaceAll("/+$", "") + "/api/v2/fongmi/danmaku?name=" + URLEncoder.encode(name, "UTF-8") + "&episode=" + URLEncoder.encode(episode != null ? episode : "", "UTF-8");
                String json = OkHttp.newCall(OkHttp.client(Constant.TIMEOUT_DANMAKU), url, "danmu_api").execute().body().string();
                List<Danmaku> items = App.gson().fromJson(json, new TypeToken<List<Danmaku>>() {}.getType());
                if (items == null) items = new ArrayList<>();
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
