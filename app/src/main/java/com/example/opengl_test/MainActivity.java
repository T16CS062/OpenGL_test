package com.example.opengl_test;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    MyGLView m_glView;
    private SensorManager m_sensorManager;
    private float m_sensor_val_x, m_sensor_val_y;
    private Runnable m_runnable;
    private final Handler m_handler = new Handler();

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // OpenGL View生成
        m_glView = new MyGLView(this);
        setContentView(m_glView);

        // センサー初期化
        m_sensor_val_x = m_sensor_val_y = 0.0f;
        m_sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        StartCyclicHandler();       // 周期ハンドラ開始
    }

    @Override protected void onResume() {
        super.onResume();
        m_glView.onResume();

        // Event Listener登録
        Sensor accel = m_sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        m_sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL);
        StartCyclicHandler();       // 周期ハンドラ開始
    }

    @Override protected void onPause() {
        super.onPause();
        m_glView.onPause();

        // Event Listener登録解除
        m_sensorManager.unregisterListener(this);
        StoptCyclicHandler();       // 周期ハンドラ停止
    }

    protected void StartCyclicHandler(){
        m_runnable = new Runnable() {
            @Override public void run() {
                m_glView.AccelerometerNotify(m_sensor_val_x, m_sensor_val_y);
                m_handler.postDelayed(this, 30);    // 30msスリープ
            }
        };
        m_handler.post(m_runnable);     // スレッド起動
    }
    protected void StoptCyclicHandler() {
        m_handler.removeCallbacks(m_runnable);
    }

    @Override public void onSensorChanged(SensorEvent event) {      // センサー通知
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            m_sensor_val_x = event.values[0];
            m_sensor_val_y = event.values[1];
        }
    }

    @Override public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}