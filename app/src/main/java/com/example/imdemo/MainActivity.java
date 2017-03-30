package com.example.imdemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private Button btnFile,btnBuffer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
    }

    private void initViews() {
        btnFile= (Button) findViewById(R.id.btnFile);
        btnBuffer= (Button) findViewById(R.id.btnBuffer);
        btnFile.setOnClickListener(this);
        btnBuffer.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnFile:
                Intent intent1=new Intent(MainActivity.this,FileActivity.class);
                startActivity(intent1);
            break;
            case R.id.btnBuffer:
                Intent intent2=new Intent(MainActivity.this,BufferActivity.class);
                startActivity(intent2);
            break;
        }
    }
}
