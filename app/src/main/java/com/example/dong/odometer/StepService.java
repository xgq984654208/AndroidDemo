package com.example.dong.odometer;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StepService extends Service implements SensorEventListener {
    private String TAG = "StepService";

    //当前的步数
    private int current_step;
    //今日步数
    private int today_step;
    //开始步数
    private int start_step;

    private String CURRENT_STEP = "CURRENT_STEP";
    private String TODAY_STEP = "TODAY_STEP";
    private String START_STEP = "START_STEP";

    private StepBinder stepBinder = new StepBinder();

    private OnStepChangedListener stepChangedListener;

    public interface OnStepChangedListener {
        void onStepChanged(int step);
    }

    public void setStepChangedListener(OnStepChangedListener listener) {
        this.stepChangedListener = listener;
    }

    //用作数据交换
    public class StepBinder extends Binder {
        //开始计步
        public void startCount() {
            Log.e(TAG, "start step count ");
        }

        public StepService getService() {
            return StepService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreated executed");
        initSensor();
        initData();
        initNotification();
        startTimeCountDown();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e(TAG, "onUnbind: ");
        //取消前台进程
        stopForeground(true);
        Log.e(TAG, "onDestroy executed");
        return super.onUnbind(intent);
    }

    public long getTime(){
        long now = System.currentTimeMillis() / 1000l;
        long daySecond = 60 * 60 * 24;
        long dayTime = now - (now + 8 * 3600) % daySecond + 1*daySecond;
        return dayTime;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return stepBinder;
    }

    public void setOnStepChangedListener(OnStepChangedListener stepChangedListener) {
        this.stepChangedListener = stepChangedListener;
    }

    //获取当天的日期
    public String getTodayDate() {
        Date date = new Date(System.currentTimeMillis());
        DateFormat dateFormat = new SimpleDateFormat("MM-dd");
        return dateFormat.format(date);
    }

    //获取昨天的日期
    private String getYesterdayDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd");
        Date yesterday = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000L);
        return dateFormat.format(yesterday);
    }

    //获取当天的步数 从数据库中获取步数并更新UI
    public void initData() {
        current_step = getCurrentStep();
        start_step = getStartStep();

        today_step = current_step-start_step;
    }

    //当接收到日期改变的广播时调用
    //新的一天进行初始化数据  或者由于时间混乱而初始化数据
    //将today_step置为0，将start_step置为current_step
    private void initNewDay() {
        String time = "00:00";
        if (time.equals(new SimpleDateFormat("HH:mm").equals(new Date(System.currentTimeMillis())))) {
            start_step = current_step;
            today_step = 0;
            save();
        }
    }

    public boolean isRecord(){
        return SPUtils.getInstance(this).getBoolean("isRecord",false);
    }

    /*-------------------------------------通知栏----------------------------------------------*/

    //初始化通知栏 前台service
    public void initNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("计步器")
                .setContentText("点击进入应用查看详情")
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.icon_shoe)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.icon_launcher))
                .setContentIntent(pIntent)
                .build();
        startForeground(1, notification);
    }

    /*-------------------------------------计时器函数----------------------------------------------*/

    //倒计时器
    private TimeCountDown timeCountDown;

    //存储时间，默认为30s
    private long duration = 30 * 1000;

    //实现倒计时向数据库中存数据 新开一个线程实现倒计时
    class TimeCountDown extends CountDownTimer {

        public TimeCountDown(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long l) {

        }

        @Override
        public void onFinish() {
            timeCountDown.cancel();
            save();
            startTimeCountDown();
        }
    }

    public void startTimeCountDown() {
        if (timeCountDown == null) {
            timeCountDown = new TimeCountDown(duration, 1000);
        }
        timeCountDown.start();
    }

    public void setTimeCounterDuration(long duration){
        this.duration = duration;
    }


    /*-----------------------------------数据持久化----------------------------------------------*/


    //将步数存储到数据库中
    private void save() {
        SPUtils spUtils = SPUtils.getInstance(this);
        spUtils.putInt(CURRENT_STEP, current_step);
        spUtils.putInt(START_STEP, start_step);
        spUtils.putInt(TODAY_STEP, today_step);
        Log.e(TAG,"存储成功");
    }

    private int getCurrentStep() {
        return SPUtils.getInstance(this).getInt(CURRENT_STEP, 0);
    }

    private int getTodayStep() {
        return SPUtils.getInstance(this).getInt(TODAY_STEP, 0);
    }

    private int getStartStep() {
        return SPUtils.getInstance(this).getInt(START_STEP, 0);
    }

    /*-----------------------------------计步器传感器-----------------------------------------------*/

    private SensorManager sensorManager;

    //传感器
    private Sensor stepCounter;

    private void initSensor() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (stepCounter != null) {
            sensorManager.registerListener(this, stepCounter, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    //每当传感器的精度发生变化时触发
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        current_step = (int) sensorEvent.values[0];
        today_step = current_step - start_step;
        //每天第一次触发计步器时记录，将isRecord置为true,并将start_step置为cur_step-9
        if (!isRecord()){
            SPUtils.getInstance(this).putBoolean("isRecord",true);
            start_step = current_step-9;
            save();
        }
        if (stepChangedListener != null) {
            stepChangedListener.onStepChanged(today_step);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


}
