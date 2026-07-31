package com.fongmi.android.tv.ui.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import com.fongmi.android.tv.Setting;

public class DanmuApiDialog {

    private final AlertDialog dialog;
    private final Runnable onDismiss;
    private Switch danmakuLoadSwitch;
    private Switch enabledSwitch;
    private Switch autoSwitch;
    private Switch crawlerSwitch;
    private EditText urlInput;

    public DanmuApiDialog(Context context, Runnable onDismiss) {
        this.onDismiss = onDismiss;

        ScrollView scroll = new ScrollView(context);
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(48, 24, 48, 24);

        danmakuLoadSwitch = addSwitchRow(context, root, "弹幕加载", Setting.isDanmakuLoad());

        TextView sep1 = new TextView(context);
        sep1.setText("— 外部弹幕 API —");
        sep1.setTextSize(12);
        sep1.setPadding(0, 16, 0, 8);
        sep1.setTextColor(0xFF888888);
        root.addView(sep1);

        enabledSwitch = addSwitchRow(context, root, "启用外部弹幕 API", Setting.isDanmuApiEnabled());
        urlInput = addEditRow(context, root, "API 地址");
        urlInput.setText(Setting.getDanmuApi());
        urlInput.setHint("http://192.168.1.7:9321");
        urlInput.setEnabled(Setting.isDanmuApiEnabled());
        autoSwitch = addSwitchRow(context, root, "自动获取弹幕", Setting.isDanmuApiAuto());
        autoSwitch.setEnabled(Setting.isDanmuApiEnabled());
        crawlerSwitch = addSwitchRow(context, root, "爬虫模式（多平台聚合）", Setting.isDanmuApiCrawler());
        crawlerSwitch.setEnabled(Setting.isDanmuApiEnabled());

        enabledSwitch.setOnCheckedChangeListener((button, checked) -> {
            urlInput.setEnabled(checked);
            autoSwitch.setEnabled(checked);
            crawlerSwitch.setEnabled(checked);
            if (checked && TextUtils.isEmpty(urlInput.getText())) urlInput.requestFocus();
        });

        scroll.addView(root);
        dialog = new AlertDialog.Builder(context)
                .setTitle("弹幕设置")
                .setView(scroll)
                .setPositiveButton(android.R.string.ok, (d, which) -> save())
                .setNegativeButton(android.R.string.cancel, null)
                .setOnDismissListener(d -> {
                    if (onDismiss != null) onDismiss.run();
                })
                .create();
    }

    private Switch addSwitchRow(Context context, LinearLayout parent, String label, boolean checked) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 14, 0, 14);
        TextView tv = new TextView(context);
        tv.setText(label);
        tv.setTextSize(15);
        tv.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        Switch sw = new Switch(context);
        sw.setChecked(checked);
        row.addView(tv);
        row.addView(sw);
        parent.addView(row);
        return sw;
    }

    private EditText addEditRow(Context context, LinearLayout parent, String label) {
        TextView tv = new TextView(context);
        tv.setText(label);
        tv.setTextSize(13);
        tv.setPadding(0, 12, 0, 6);
        tv.setTextColor(0xFF888888);
        parent.addView(tv);
        EditText et = new EditText(context);
        et.setSingleLine();
        et.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        parent.addView(et);
        return et;
    }

    private void save() {
        Setting.putDanmakuLoad(danmakuLoadSwitch.isChecked());
        String url = urlInput.getText().toString().trim();
        Setting.putDanmuApi(url);
        Setting.putDanmuApiEnabled(enabledSwitch.isChecked() && !TextUtils.isEmpty(url));
        Setting.putDanmuApiAuto(autoSwitch.isChecked());
        Setting.putDanmuApiCrawler(crawlerSwitch.isChecked());
    }

    public void show() {
        dialog.show();
    }
}
