package com.example.fripl.cut_in_app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
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
        builder.setTitle("てｓｔ");

        //プログレスバーのビュー作成
        ProgressBar progressBar = new ProgressBar(getActivity().getApplicationContext());

        builder.setView(progressBar);
        return builder.create();
    }

}
