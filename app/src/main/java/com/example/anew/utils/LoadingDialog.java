package com.example.anew.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.example.anew.R;

public class LoadingDialog {

    private static AlertDialog dialog;

    public static void show(Context context) {
        if (dialog != null && dialog.isShowing()) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_loader, null);
        builder.setView(view);

        dialog = builder.create();
        dialog.show();
    }

    public static void hide() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}
