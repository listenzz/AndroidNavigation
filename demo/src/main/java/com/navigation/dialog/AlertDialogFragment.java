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
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Hello World!")
                .setMessage("苔花如米小，也学牡丹开")
                .setPositiveButton("是的", (dialogInterface, i) -> {
                    Bundle result = new Bundle();
                    result.putString("text", "一起加油！！");
                    setResult(0, result);
                })
                .setNegativeButton("给个赞", (dialogInterface, i) -> {
                    Bundle result = new Bundle();
                    result.putString("text", "感谢支持，祝生活愉快。");
                    setResult(0, result);
                })
                .create();
        return dialog;
    }




}
