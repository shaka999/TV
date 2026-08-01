package com.fongmi.android.tv.ui.dialog;

import android.text.TextUtils;
import android.view.LayoutInflater;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

import com.fongmi.android.tv.Setting;
import com.fongmi.android.tv.databinding.DialogDanmuApiBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class DanmuApiDialog {

    private final DialogDanmuApiBinding binding;
    private final Runnable onDismiss;
    private final AlertDialog dialog;

    public DanmuApiDialog(FragmentActivity activity, Runnable onDismiss) {
        this.onDismiss = onDismiss;
        this.binding = DialogDanmuApiBinding.inflate(LayoutInflater.from(activity));
        this.dialog = new MaterialAlertDialogBuilder(activity).setView(binding.getRoot()).create();
    }

    public void show() {
        initDialog();
        initView();
        initEvent();
    }

    private void initDialog() {
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setOnDismissListener(d -> {
            if (onDismiss != null) onDismiss.run();
        });
        dialog.show();
    }

    private void initView() {
        binding.switchDanmakuLoad.setChecked(Setting.isDanmakuLoad());
        binding.switchEnabled.setChecked(Setting.isDanmuApiEnabled());
        binding.editUrl.setText(Setting.getDanmuApi());
        binding.editUrl.setHint("http://192.168.1.7:9321");
        binding.editUrl.setEnabled(Setting.isDanmuApiEnabled());
        binding.switchAuto.setChecked(Setting.isDanmuApiAuto());
        binding.switchAuto.setEnabled(Setting.isDanmuApiEnabled());
    }

    private void initEvent() {
        binding.rowDanmakuLoad.setOnClickListener(v -> binding.switchDanmakuLoad.toggle());
        binding.rowEnabled.setOnClickListener(v -> binding.switchEnabled.toggle());
        binding.rowAuto.setOnClickListener(v -> binding.switchAuto.toggle());

        binding.switchEnabled.setOnCheckedChangeListener((button, checked) -> {
            binding.editUrl.setEnabled(checked);
            binding.switchAuto.setEnabled(checked);
            if (checked && TextUtils.isEmpty(binding.editUrl.getText())) binding.editUrl.requestFocus();
        });

        binding.positive.setOnClickListener(v -> save());
        binding.negative.setOnClickListener(v -> dialog.dismiss());
    }

    private void save() {
        Setting.putDanmakuLoad(binding.switchDanmakuLoad.isChecked());
        String url = binding.editUrl.getText().toString().trim();
        Setting.putDanmuApi(url);
        Setting.putDanmuApiEnabled(binding.switchEnabled.isChecked() && !TextUtils.isEmpty(url));
        Setting.putDanmuApiAuto(binding.switchAuto.isChecked());
        dialog.dismiss();
    }
}
