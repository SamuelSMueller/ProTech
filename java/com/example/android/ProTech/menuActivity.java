package com.example.android.ProTech;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.android.ProTech.vpnService.vPNActivity;

public class menuActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
    }

    public void sendMessage1(View view) {
        this.finishAffinity();
    }

    public void sendMessage2(View view) {
        Intent intent = new Intent(this, discoNetActivity.class);
        startActivity(intent);
    }

    public void sendMessage4(View view) {
        Intent intent = new Intent(this, vPNActivity.class);
        startActivity(intent);
    }


}