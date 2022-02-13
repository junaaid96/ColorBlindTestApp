package com.example.colorblindtestapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class FourthActivity extends AppCompatActivity {

    Button elevenbtn;
    Button twentyonebtn;
    Button twelvebtn;
    TextView passTV;
    TextView failTV;
    Button nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fourth_layout);

        elevenbtn = findViewById(R.id.elevenbtn);
        twentyonebtn = findViewById(R.id.twentyonebtn);
        twelvebtn = findViewById(R.id.twelvebtn);
        passTV = findViewById(R.id.passTV);
        failTV = findViewById(R.id.failTV);
        nextButton = findViewById(R.id.nextButton);

        twelvebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                passTV.setText("Passed!");
            }
        });
        twentyonebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                failTV.setText("Failed!");
            }
        });
        elevenbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                failTV.setText("Failed!");
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(FourthActivity.this, FifthActivity.class );
                startActivity(myIntent);
            }
        });
    }
}