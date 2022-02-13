package com.example.colorblindtestapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ThirdActivity extends AppCompatActivity {

    Button sixbtn;
    Button sbtn;
    Button dollarbtn;
    TextView passTV;
    TextView failTV;
    Button nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.third_layout);

        sixbtn = findViewById(R.id.sixbtn);
        sbtn = findViewById(R.id.sbtn);
        dollarbtn = findViewById(R.id.dollarbtn);
        passTV = findViewById(R.id.passTV);
        failTV = findViewById(R.id.failTV);
        nextButton = findViewById(R.id.nextButton);

        sixbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                passTV.setText("Passed!");
            }
        });
        sbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                failTV.setText("Failed!");
            }
        });
        dollarbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                failTV.setText("Failed!");
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(ThirdActivity.this, FourthActivity.class );
                startActivity(myIntent);


            }
        });
    }
}