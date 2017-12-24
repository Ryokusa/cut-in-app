package com.example.fripl.cut_in_app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.ProgressBar;

/**
 * Created by fripl on 2017/12/18.
 */

public class ProgressDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        //ビルダー作成
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("アプリ情報読み込み中...");

        //プログレスバーのビュー作成
        ProgressBar progressBar = new ProgressBar(getActivity().getApplicationContext());

        builder.setView(progressBar);
        return builder.create();
    }


}
