package com.example.dong.odometer;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Space;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.hanks.htextview.fade.FadeTextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.List;

public class MainActivity extends AppCompatActivity implements onStepChangedListener, View.OnClickListener {

    private static final String TAG = "MainActivity";

    private StepView stepView;
    private int last_step;
    private int steps = 0;

    private String CURRENT_STEP = "CURRENT_STEP";
    private String TODAY_STEP = "TODAY_STEP";
    private String START_STEP = "START_STEP";

    private StepService.StepBinder binder;


    Button btn_start;
    Button btn_finish;

    CardView item_card1;
    HorizontalProgressBar progressBar;
    ImageView btn_mark;
    FadeTextView fadeTextView;


    //与Service交互
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            binder = (StepService.StepBinder) iBinder;
            binder.startCount();
            binder.getService().setStepChangedListener(new StepService.OnStepChangedListener() {
                @Override
                public void onStepChanged(int step) {
                    Log.e(TAG, "step changed " + step);
                    last_step = step;
                    stepView.reSet(last_step, step);
                }
            });

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.e(TAG, "onCreate");

        stepView = findViewById(R.id.stepView);
        btn_start = findViewById(R.id.start_count);
        btn_finish = findViewById(R.id.finish_count);
        item_card1 = findViewById(R.id.card1);
        fadeTextView  = findViewById(R.id.fade_text);

        Intent intent = new Intent(this, StepService.class);
        bindService(intent, connection, BIND_AUTO_CREATE);

        btn_start.setOnClickListener(this);
        btn_finish.setOnClickListener(this);
        item_card1.setOnClickListener(this);
        btn_mark.setOnClickListener(this);


        int step = SPUtils.getInstance(this).getInt(TODAY_STEP, 0);
        stepView.reSet(0, step);
        int progress = step>8000?100:step/80;
        progressBar.setProgressWithAnimation(progress);

        showSaying();

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
        Log.e(TAG, "onDestroy");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG, "onStop");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(TAG, "onStart");
    }

    //OnStepChangedListener接口回调
    @Override
    public void stepChanged(int step) {
        stepView.reSet(last_step, step);
        last_step = step;
        // TODO: 2019/6/1 进度条的更新
    }


    //button的点击事件
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start_count:
                //isRecord置为false today_step置为0
                SPUtils.getInstance(this).putBoolean("isRecord", false);
                SPUtils.getInstance(this).putInt("TODAY_STEP", 0);
                stepView.reSet(0, 0);
                progressBar.setProgressWithAnimation(0);
                break;
            case R.id.finish_count:
                try {

                    storgeJson();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;

            case R.id.card1:
                Intent intent = new Intent(MainActivity.this,ListActivity.class);
                startActivity(intent);
                break;


        }

    }


    private void storgeJson() throws JSONException {
        //待存储的json
        JSONArray jsonArray = new JSONArray();

        String json = SPUtils.getInstance(this).getString("StepRecord", null);
        if (json != null) {

            Gson gson = new Gson();
            List<StepRecord> records = gson.fromJson(json, new TypeToken<List<StepRecord>>() {
            }.getType());

            for (StepRecord record : records) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("step", record.getStep());
                jsonObject.put("date", record.getDate());
                jsonArray.put(jsonObject);
            }
        }

        JSONObject jsonObject = new JSONObject();
        //若step==0则从SP中提取数据
        if (steps==0){
            int steps = SPUtils.getInstance(this).getInt(TODAY_STEP,0);
            jsonObject.put("step",steps);
        }else {
            jsonObject.put("step", steps);
        }
        jsonObject.put("date", getDate());
        jsonArray.put(jsonObject);
        Log.e(TAG, jsonArray.toString());
        SPUtils.getInstance(this).putString("StepRecord", jsonArray.toString());

    }

    private String getDate() {
        long time = System.currentTimeMillis();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd");
        return dateFormat.format(time);
    }





    private void showSaying(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(10*1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int i = (int) ((Math.random())*10);
                        String text= getResources().getStringArray(R.array.sayings)[i];
                        fadeTextView.animateText(text);
                        showSaying();
                    }
                });
            }
        }).start();
    }
}
