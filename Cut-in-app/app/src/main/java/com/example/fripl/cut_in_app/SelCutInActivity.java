package com.example.fripl.cut_in_app;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;

import com.example.fripl.cut_in_app.Dialog.SimpleAlertDialog;
import com.example.fripl.cut_in_app.cut_in_editer.CutInEditerActivity;
import com.example.fripl.cut_in_app.data_manage.CutInDataManager;

import java.util.ArrayList;

/**
 * Created by fripl on 2017/12/25.
 * カットイン選択アクティビティ
 */

public class SelCutInActivity extends AppCompatActivity {
    private static final String TAG = "SelCutInActivity";

    private static final int CUTIN_SET = 0;
    private static final int CUTIN_EDIT = 1;
    private static final int CUTIN_DELETE = 2;
    private static final int CUTIN_TITLE_CHANGE = 3;

    private CutInAdapter cutInAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sel_cut_in_layout);

        //カットインアダプター追加処理
        cutInAdapter = new CutInAdapter(this, 0, CutInService.cutInList);
        GridView gridView = (GridView)findViewById(R.id.gridView);
        gridView.setAdapter(cutInAdapter);
        gridView.setOnItemClickListener(onCutInClick);

    }

    @Override
    protected void onResume() {
        super.onResume();
        cutInAdapter.notifyDataSetChanged();
    }

    //オプションメニュー作成時
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        //メニューレイアウト適用
        getMenuInflater().inflate(R.menu.sel_cutin_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        switch (id){
            case R.id.newCutIn:    //カットイン新規作成
                Intent intent = new Intent(SelCutInActivity.this, CutInEditerActivity.class);
                //選択カットイン番号を伝える
                Bundle bundle = new Bundle();
                bundle.putInt("selCutInId", -1);    //新規作成
                intent.putExtras(bundle);
                startActivity(intent);
                break;
        }
        return true;
    }

    private AdapterView.OnItemClickListener onCutInClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
            Log.i(TAG, "onItemClick:" + position);

            //リスナーで使うためのポジション
            final int _position = position;

            /* 設定及び編集画面選択ウィンドウ表示 */
            AlertDialog.Builder builder = new AlertDialog.Builder(SelCutInActivity.this);
            builder.setTitle(R.string.control_cutin_menu);
            builder.setItems(R.array.control_cutin_menu_items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case CUTIN_SET: //カットイン設定
                            // カットインをセット
                            int cutInSetIndex;
                            String action = "";

                            //選択されたイベント名取得
                            action = getResources().getStringArray(R.array.event_names)[MainActivity.selEvent];
                            if(action.equals("app_notification")){  //アプリ通知の場合はアプリ名格納
                                action = MainActivity.selAppData.getAppName();
                            }

                            //該当するアクションのカットイン探索
                            if((cutInSetIndex = CutInService.getCutInSetIndex(action)) != -1){
                                //見つかった場合カットイン番号設定
                                CutInService.cutInSetList.get(cutInSetIndex).setCutInId(_position);
                            }else{
                                //見つらなかった場合追加
                                CutInService.cutInSetList.add(new CutInSet(_position, action));
                            }

                            //カットインセット保存
                            new CutInDataManager(getApplicationContext()).cutInSetListSave(new ArrayList<CutInSet>(CutInService.cutInSetList));

                            finish();
                            break;
                        case CUTIN_EDIT:    //カットイン編集
                            Intent intent = new Intent(SelCutInActivity.this, CutInEditerActivity.class);
                            //選択カットイン番号を伝える
                            Bundle bundle = new Bundle();
                            bundle.putInt("selCutInId", position);
                            intent.putExtras(bundle);
                            startActivity(intent);
                            break;
                        case CUTIN_DELETE:  //カットイン削除
                            //カットイン削除確認ウィンドウ
                            AlertDialog.Builder builder = new AlertDialog.Builder(SelCutInActivity.this);
                            builder.setTitle("注意");
                            builder.setMessage("このカットインを削除しますがよろしいですか？");
                            builder.setNegativeButton("いいえ", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //キャンセル時何もせず閉じる
                                }
                            });
                            builder.setPositiveButton("はい", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //OK時カットイン削除
                                    CutInService.removeCutIn(position);
                                    cutInAdapter.notifyDataSetChanged();
                                }
                            });
                            builder.create().show();
                            break;
                        case CUTIN_TITLE_CHANGE:
                            final EditText editView = new EditText(SelCutInActivity.this);
                            final AlertDialog.Builder editBuilder = new AlertDialog.Builder(SelCutInActivity.this);
                            editBuilder.setView(editView);
                            editBuilder.setTitle("タイトル入力");
                            editBuilder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if(!editView.getText().equals("")){
                                        CutInService.cutInList.get(position).setTitle(editView.getText().toString());
                                        cutInAdapter.notifyDataSetChanged();
                                    }
                                }
                            });
                            editBuilder.create().show();
                            break;

                    }
                }
            });
            builder.create().show();
        }
    };
}
