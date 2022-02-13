package com.example.colorblindtestapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SecondActivity extends AppCompatActivity {

    Button twobtn;
    Button sbtn;
    Button fivebtn;
    TextView passTV;
    TextView failTV;
    Button nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.second_layout);

        twobtn = findViewById(R.id.twobtn);
        sbtn = findViewById(R.id.sbtn);
        fivebtn = findViewById(R.id.fivebtn);
        passTV = findViewById(R.id.passTV);
        failTV = findViewById(R.id.failTV);
        nextButton = findViewById(R.id.nextButton);

        twobtn.setOnClickListener(new View.OnClickListener() {
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
        fivebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                failTV.setText("Failed!");
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(SecondActivity.this, ThirdActivity.class );
                startActivity(myIntent);
            }
        });

    }
}