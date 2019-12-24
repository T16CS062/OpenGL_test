package com.example.opengl_test;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11Ext;

public class MyGLPic {
    protected int m_textureNo;
    protected float m_width, m_height;          // 画像の表示幅と高さ
    protected int m_crop_x, m_crop_y;           // テクスチャ切り出し左上オフセット座標
    protected int m_crop_w, m_crop_h;           // テクスチャ切り出し幅と高さ
    protected float m_pos_x, m_pos_y, m_pos_z;  // テクスチャ表示位置

    public MyGLPic(){
        m_textureNo = 0;
        m_pos_x = m_pos_y = m_pos_z = 0.0f;
        m_width = m_height = 0.0f;
    }

    // 画像情報問い合わせ
    public int get_pos_x(){ return (int)m_pos_x; }
    public int get_pos_y(){ return (int)m_pos_y; }
    public int get_w(){ return (int)m_width; }
    public int get_h(){ return (int)m_height; }

    // 本インスタンスに画像をロード
    public void SetTexture( GL10 gl, Resources res, int id ) {
        InputStream is = res.openRawResource(id);
        Bitmap bitmap;
        try {
            bitmap = BitmapFactory.decodeStream(is);
        } finally {
        }
        try {
            is.close();
        } catch (IOException e) {
            Log.d("MyGLPic", "Can't load resource.");
        }
        gl.glEnable(GL10.GL_ALPHA_TEST);
        gl.glEnable(GL10.GL_BLEND);
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
        // 下地色とテクスチャ色との合成方法 : 乗算
        gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_MODULATE);

        // テクスチャIDを割り当て
        int[] ary_textureNo = new int[1];
        gl.glGenTextures(1, ary_textureNo, 0);
        m_textureNo = ary_textureNo[0];

        // テクスチャIDをバインドし、データとIDを関連付ける。
        gl.glBindTexture(GL10.GL_TEXTURE_2D, m_textureNo);
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);

        // ポリゴン内の画像の繰り返し指定 S軸、T軸ともに繰り返しなし。
        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE );
        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE );

        // テクスチャのリサンプリングアルゴリズム指定
        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR );
        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR );

        // 表示座標設定
        int img_w = bitmap.getWidth();
        int img_h = bitmap.getHeight();
        SetPosAll(0, img_h, img_w, -img_h,0, 0, img_w, img_h);
    }

    //テクスチャ表示サイズ、表示位置情報をセット
    public void SetPosAll( int crop_x,int crop_y,int crop_w,int crop_h, float pos_x, float pos_y, float disp_w, float disp_h ){
        m_crop_x = crop_x;
        m_crop_y = crop_y;
        m_crop_w = crop_w;
        m_crop_h = crop_h;
        m_pos_x = pos_x;
        m_pos_y = pos_y;
        m_width = disp_w;
        m_height = disp_h;
    }
    public void SetPosXY( float pos_x, float pos_y ) {
        m_pos_x = pos_x;
        m_pos_y = pos_y;
    }

    public void Draw( GL10 gl ){
        gl.glDisable(GL10.GL_DEPTH_TEST);   // デプステストOFF

        // 白色
        //    gl.glColor4x(0x10000, 0x10000, 0x10000, 0x10000);

        // 表示するテクスチャをバインド
        gl.glActiveTexture(GL10.GL_TEXTURE0);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, m_textureNo);

        // テクスチャから指定部分を切り出して表示
        int rect[] = {m_crop_x, m_crop_y, m_crop_w, m_crop_h};
        ((GL11)gl).glTexParameteriv(GL10.GL_TEXTURE_2D, GL11Ext.GL_TEXTURE_CROP_RECT_OES, rect, 0);
        ((GL11Ext) gl).glDrawTexfOES(m_pos_x, m_pos_y, m_pos_z, m_width, m_height);

        gl.glEnable(GL10.GL_DEPTH_TEST);   // デプステストON
    }
}
