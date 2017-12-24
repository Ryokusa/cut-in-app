package com.example.fripl.movie_test;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.net.URI;

import static android.provider.LiveFolders.INTENT;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button bt = (Button)findViewById(R.id.button);

        bt.setOnClickListener(new View.OnClickListener () {
            @Override
            public void onClick(View v) {   //ボタンがクリックされたとき
                String mvURL;
                Intent intent = new Intent(Intent.ACTION_VIEW);
                EditText edt = (EditText)findViewById(R.id.editText);
                mvURL = edt.getText().toString(); //動画URL取得
                mvURL = "http://vs02.thisav.com/dash/Y_Z_bUEqRqHSHbLkguH2iw,1512233734/330210_high_dashinit.mp4";
                Uri uri = Uri.parse(mvURL); //URI取得

                intent.setType("video/mp4");
                intent.setData(uri);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });
    }
}
