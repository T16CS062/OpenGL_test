package com.example.opengl_test;

import android.content.Context;
import android.opengl.GLSurfaceView;

public class MyGLView extends GLSurfaceView {
    private MyGLRenderer m_renderer;

    public MyGLView(Context context){
        super(context);

        // レンダラー生成
        m_renderer = new MyGLRenderer(context);
        setRenderer(m_renderer);
    }

    public void AccelerometerNotify( float x, float y ){    // 加速度センサー通知
        m_renderer.AccelerometerNotify(x, y);
    }
}