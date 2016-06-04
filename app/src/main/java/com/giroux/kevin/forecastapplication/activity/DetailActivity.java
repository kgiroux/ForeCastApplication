package com.giroux.kevin.forecastapplication.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.giroux.kevin.forecastapplication.Fragment.DetailActivityFragment;
import com.giroux.kevin.forecastapplication.R;

public class DetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailActivityFragment())
                    .commit();
        }

    }
}
