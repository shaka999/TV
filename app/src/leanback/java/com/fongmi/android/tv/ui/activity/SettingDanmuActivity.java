package com.fongmi.android.tv.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.Setting;
import com.fongmi.android.tv.databinding.ActivitySettingDanmuBinding;
import com.fongmi.android.tv.ui.base.BaseActivity;

public class SettingDanmuActivity extends BaseActivity {

    private ActivitySettingDanmuBinding mBinding;
    private InputMethodManager mImm;

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
        mImm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mBinding.danmakuLoad.requestFocus();
        refreshAll();
    }

    @Override
    protected void initEvent() {
        mBinding.danmakuLoad.setOnClickListener(this::setDanmakuLoad);
        mBinding.danmuApiEnabled.setOnClickListener(this::setDanmuApiEnabled);
        mBinding.danmuApiAuto.setOnClickListener(this::setDanmuApiAuto);

        // 内联输入框：获得焦点时弹出TV输入法，失焦收起；输入即时保存
        mBinding.danmuApiUrlEdit.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                mImm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
            } else {
                mImm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        });
        mBinding.danmuApiUrlEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                Setting.putDanmuApi(s.toString().trim());
            }
        });
    }

    // 级联刷新：弹幕加载 -> 启用外部API -> 地址输入框 + 自动获取
    private void refreshAll() {
        boolean load = Setting.isDanmakuLoad();
        mBinding.danmakuLoadText.setText(getSwitch(load));
        mBinding.danmuApiEnabled.setVisibility(load ? View.VISIBLE : View.GONE);
        if (load) {
            boolean enabled = Setting.isDanmuApiEnabled();
            mBinding.danmuApiEnabledText.setText(getSwitch(enabled));
            mBinding.danmuApiUrl.setVisibility(enabled ? View.VISIBLE : View.GONE);
            mBinding.danmuApiAuto.setVisibility(enabled ? View.VISIBLE : View.GONE);
            if (enabled) {
                mBinding.danmuApiUrlEdit.setText(Setting.getDanmuApi());
                mBinding.danmuApiAutoText.setText(getSwitch(Setting.isDanmuApiAuto()));
            }
        }
    }

    private void setDanmakuLoad(View view) {
        Setting.putDanmakuLoad(!Setting.isDanmakuLoad());
        // 关闭弹幕加载时收起输入法并隐藏下方所有子项
        if (!Setting.isDanmakuLoad()) {
            mImm.hideSoftInputFromWindow(mBinding.danmuApiUrlEdit.getWindowToken(), 0);
        }
        refreshAll();
    }

    private void setDanmuApiEnabled(View view) {
        boolean willEnable = !Setting.isDanmuApiEnabled();
        Setting.putDanmuApiEnabled(willEnable);
        refreshAll();
        // 打开外部API后直接聚焦内联输入框并弹出输入法，无需弹窗
        if (willEnable) {
            mBinding.danmuApiUrlEdit.requestFocus();
            mImm.showSoftInput(mBinding.danmuApiUrlEdit, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void setDanmuApiAuto(View view) {
        Setting.putDanmuApiAuto(!Setting.isDanmuApiAuto());
        mBinding.danmuApiAutoText.setText(getSwitch(Setting.isDanmuApiAuto()));
    }
}
