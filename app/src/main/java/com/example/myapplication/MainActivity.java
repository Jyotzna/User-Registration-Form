package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    Button buttonRegister, buttonViewData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonRegister = findViewById(R.id.buttonRegister);
        buttonViewData = findViewById(R.id.buttonViewData);

        buttonRegister.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, RegistrationActivity.class)));

        buttonViewData.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ViewDataActivity.class)));
    }
}