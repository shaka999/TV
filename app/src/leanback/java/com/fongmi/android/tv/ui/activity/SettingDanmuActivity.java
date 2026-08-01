package com.fongmi.android.tv.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.Setting;
import com.fongmi.android.tv.databinding.ActivitySettingDanmuBinding;
import com.fongmi.android.tv.ui.base.BaseActivity;
import com.fongmi.android.tv.utils.ResUtil;

public class SettingDanmuActivity extends BaseActivity {

    private ActivitySettingDanmuBinding mBinding;

    public static void start(Activity activity) {
        activity.startActivity(new Intent(activity, SettingDanmuActivity.class));
    }

    private String getSwitch(boolean value) {
        return getString(value ? R.string.setting_on : R.string.setting_off);
    }

    @Override
    protected ViewBinding getBinding() {
        return mBinding = ActivitySettingDanmuBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
        mBinding.danmakuLoad.requestFocus();
        mBinding.danmakuLoadText.setText(getSwitch(Setting.isDanmakuLoad()));
        mBinding.danmuApiEnabledText.setText(getSwitch(Setting.isDanmuApiEnabled()));
        mBinding.danmuApiUrlText.setText(Setting.getDanmuApi());
        mBinding.danmuApiAutoText.setText(getSwitch(Setting.isDanmuApiAuto()));
        updateApiVisibility();
    }

    @Override
    protected void initEvent() {
        mBinding.danmakuLoad.setOnClickListener(this::setDanmakuLoad);
        mBinding.danmuApiEnabled.setOnClickListener(this::setDanmuApiEnabled);
        mBinding.danmuApiUrl.setOnClickListener(this::onDanmuApiUrl);
        mBinding.danmuApiAuto.setOnClickListener(this::setDanmuApiAuto);
    }

    private void updateApiVisibility() {
        boolean enabled = Setting.isDanmuApiEnabled();
        mBinding.danmuApiUrl.setVisibility(enabled ? View.VISIBLE : View.GONE);
        mBinding.danmuApiAuto.setVisibility(enabled ? View.VISIBLE : View.GONE);
    }

    private void setDanmakuLoad(View view) {
        Setting.putDanmakuLoad(!Setting.isDanmakuLoad());
        mBinding.danmakuLoadText.setText(getSwitch(Setting.isDanmakuLoad()));
    }

    private void setDanmuApiEnabled(View view) {
        boolean willEnable = !Setting.isDanmuApiEnabled();
        if (willEnable) {
            // 启用时弹出输入框让用户填写API地址
            showApiUrlDialog(willEnable);
        } else {
            Setting.putDanmuApiEnabled(false);
            mBinding.danmuApiEnabledText.setText(getSwitch(false));
            updateApiVisibility();
        }
    }

    private void onDanmuApiUrl(View view) {
        showApiUrlDialog(Setting.isDanmuApiEnabled());
    }

    private void showApiUrlDialog(boolean keepEnabled) {
        EditText editText = new EditText(this);
        editText.setText(Setting.getDanmuApi());
        editText.setHint("http://192.168.1.7:9321");
        editText.setSingleLine(true);
        editText.setTextColor(ResUtil.getColor(R.color.white));
        editText.setBackgroundColor(ResUtil.getColor(R.color.transparent));
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.danmaku_api_url))
                .setView(editText)
                .setPositiveButton(getString(R.string.dialog_positive), (dialog, which) -> {
                    String url = editText.getText().toString().trim();
                    Setting.putDanmuApi(url);
                    boolean enabled = keepEnabled && !TextUtils.isEmpty(url);
                    Setting.putDanmuApiEnabled(enabled);
                    mBinding.danmuApiEnabledText.setText(getSwitch(enabled));
                    mBinding.danmuApiUrlText.setText(url);
                    updateApiVisibility();
                })
                .setNegativeButton(getString(R.string.dialog_negative), null)
                .show();
    }

    private void setDanmuApiAuto(View view) {
        Setting.putDanmuApiAuto(!Setting.isDanmuApiAuto());
        mBinding.danmuApiAutoText.setText(getSwitch(Setting.isDanmuApiAuto()));
    }
}
