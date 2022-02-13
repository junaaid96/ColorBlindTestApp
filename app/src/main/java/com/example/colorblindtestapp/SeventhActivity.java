package com.example.colorblindtestapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SeventhActivity extends AppCompatActivity {

    Button threebtn;
    Button sbtn;
    Button dollarbtn;
    TextView passTV;
    TextView failTV;
    Button finishButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.seventh_layout);

        threebtn = findViewById(R.id.threebtn);
        sbtn = findViewById(R.id.sbtn);
        dollarbtn = findViewById(R.id.dollarbtn);
        finishButton = findViewById(R.id.finishButton);

        dollarbtn.setOnClickListener(new View.OnClickListener() {
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
        threebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                failTV.setText("Failed!");
            }
        });

        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent myIntent = new Intent(SeventhActivity.this, LastActivity.class );
                startActivity(myIntent);

            }
        });
    }
}