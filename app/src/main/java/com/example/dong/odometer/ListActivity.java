package com.example.dong.odometer;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class ListActivity extends AppCompatActivity {
    private static final String TAG = "ListActivity";

    RecyclerView recyclerView;
    StepAdapter adapter;
    List<StepRecord> records;

    Toolbar toolbar;
    CollapsingToolbarLayout toolbarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        initDate();
        initView();
    }

    private void initDate(){
        records = new ArrayList<>();
        Gson gson = new Gson();
        String json = SPUtils.getInstance(this).getString("StepRecord", null);
        records = gson.fromJson(json,new TypeToken<List<StepRecord>>(){}.getType());
    }

    private void initView(){
        recyclerView = findViewById(R.id.recycler_view);
        adapter = new StepAdapter(this,records);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        toolbar = findViewById(R.id.toolbar);

        //调色板
        Palette.from(BitmapFactory.decodeResource(getResources(),R.drawable.run_run))
                .generate(new Palette.PaletteAsyncListener() {
                     @Override
                    public void onGenerated(@NonNull Palette palette) {
                        int color=palette.getVibrantColor(getResources().getColor(R.color.colorPrimary));
                        toolbarLayout.setContentScrimColor(color);
                    }
                });

        toolbarLayout = findViewById(R.id.toolbarLayout);
        toolbarLayout.setExpandedTitleColor(Color.WHITE);
        toolbarLayout.setCollapsedTitleTextColor(Color.WHITE);
        toolbarLayout.setTitle("运动记录");

    }
}
