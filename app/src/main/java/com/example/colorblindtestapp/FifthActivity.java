package com.example.colorblindtestapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class FifthActivity extends AppCompatActivity {

    Button ssbtn;
    Button seventyfourbtn;
    Button ninetyfourbtn;
    TextView passTV;
    TextView failTV;
    Button nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fifth_layout);

        ssbtn = findViewById(R.id.ssbtn);
        seventyfourbtn = findViewById(R.id.seventyfourbtn);
        ninetyfourbtn = findViewById(R.id.ninetyfourbtn);
        passTV = findViewById(R.id.passTV);
        failTV = findViewById(R.id.failTV);
        nextButton = findViewById(R.id.nextButton);

        seventyfourbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                passTV.setText("Passed!");
            }
        });
        ssbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                failTV.setText("Failed!");
            }
        });
        ninetyfourbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                failTV.setText("Failed!");
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(FifthActivity.this, SixthActivity.class );
                startActivity(myIntent);
            }
        });
    }
}