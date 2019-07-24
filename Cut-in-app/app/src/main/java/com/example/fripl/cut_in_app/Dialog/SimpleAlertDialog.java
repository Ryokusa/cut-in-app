package com.example.fripl.cut_in_app.Dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

/**
 * Created by fripl on 2018/01/21.
 * タイトルとメッセージとOKボタンのみのシンプルなダイアログ
 */

public class SimpleAlertDialog extends DialogFragment {
    DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            //なし
        }
    };

    public static SimpleAlertDialog getInstance(String title, String msg){
        SimpleAlertDialog alertDialog = new SimpleAlertDialog();
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        bundle.putString("msg", msg);
        alertDialog.setArguments(bundle);

        return alertDialog;
    }

    public void addOnClickListener(DialogInterface.OnClickListener onClickListener){
        this.onClickListener = onClickListener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getArguments().getString("title"));
        builder.setMessage(getArguments().getString("msg"));
        builder.setPositiveButton("OK", onClickListener);

        return builder.create();
    }
}
