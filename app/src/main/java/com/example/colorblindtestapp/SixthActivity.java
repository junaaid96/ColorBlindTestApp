package com.example.colorblindtestapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SixthActivity extends AppCompatActivity {

    Button vvbtn;
    Button wbtn;
    Button vbtn;
    TextView passTV;
    TextView failTV;
    Button nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sixth_layout);

        vvbtn = findViewById(R.id.vvbtn);
        wbtn = findViewById(R.id.wbtn);
        vbtn = findViewById(R.id.vbtn);
        passTV = findViewById(R.id.passTV);
        failTV = findViewById(R.id.failTV);
        nextButton = findViewById(R.id.nextButton);

        wbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                passTV.setText("Passed!");
            }
        });
        vvbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                failTV.setText("Failed!");
            }
        });
        vbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                failTV.setText("Failed!");
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(SixthActivity.this, SeventhActivity.class );
                startActivity(myIntent);
            }
        });
    }
}