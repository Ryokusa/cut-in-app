package com.example.fripl.customdialogtest;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fripl on 2017/12/17.
 * アラートダイアログを別クラスで定義する方法
 */

public class testDialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //builder.setMessage("リスト表示できてる？");
        builder.setTitle("テストダイアログ");

        //リスト配列作成
        List<String> names = new ArrayList<String>();
        names.add("test");
        names.add("int");
        names.add("G o k i B u r i");


        //アダプター作成
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(ApplicationContext.getInstance().getApplicationContext(), android.R.layout.simple_list_item_1, names);
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        names.add("tet");


        return builder.create();
    }

}
