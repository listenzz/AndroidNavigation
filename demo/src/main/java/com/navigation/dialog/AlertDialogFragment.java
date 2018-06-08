package com.navigation.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

import me.listenzz.navigation.AwesomeFragment;

public class AlertDialogFragment extends AwesomeFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(requireContext())
                .setTitle("Hello World!")
                .setMessage("编码不易，且编且珍惜")
                .setPositiveButton("是的", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Bundle result = new Bundle();
                        result.putString("words", "一起加油！！");
                        setResult(0, result);
                    }
                })
                .setNegativeButton("给个赞", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Bundle result = new Bundle();
                        result.putString("words", "感谢支持，祝生活愉快。");
                        setResult(0, result);
                    }
                })
                .create();
    }
}
