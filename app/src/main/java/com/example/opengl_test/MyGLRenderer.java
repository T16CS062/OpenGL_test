package com.example.opengl_test;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLUtils;
import android.util.Log;

public class MyGLRenderer implements GLSurfaceView.Renderer {
    private Context m_context;          // アプリのコンテキスト
    private MyGLPic m_pic = null;       // 表示画像
    private int m_view_w, m_view_h;     // View表示領域の幅と高さ

    public MyGLRenderer( Context context ){
        m_context = context;
    }

    @Override public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        gl.glDisable(GL10.GL_DITHER);       // 機能無効化 ： ディザリング
        gl.glEnable(GL10.GL_DEPTH_TEST);    // 機能有効化 : 隠面消去用 Depth Buffer更新
        gl.glEnable(GL10.GL_TEXTURE_2D);    // 機能有効化 : 2Dテクスチャ
        gl.glEnable(GL10.GL_ALPHA_TEST);    // 機能有効化 : アルファテスト(αチャネルによる画素の有効/無効判定)
        gl.glEnable(GL10.GL_BLEND);         // 機能有効化 : ブレンド(画像の重ね合わせ)
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);     // カラーブレンド(画像重ね合わせ)モード :  アルファブレンド
        // 描画する画像をリソースから取得
        m_pic = new MyGLPic();
        m_pic.SetTexture(gl, m_context.getResources(), R.drawable.ball);
    }

    @Override public void onSurfaceChanged(GL10 gl, int width, int height) {
        m_view_w = width;
        m_view_h = height;

        gl.glViewport(0, 0, width, height);
        //Canvasを使って、文字をBitMap化
        Bitmap bitmap = Bitmap.createBitmap(256, 256, Config.ARGB_8888);
        {
            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            paint.setColor(Color.WHITE);
            paint.setStyle(Style.FILL);
            canvas.drawColor(0);
            canvas.drawText("test_character", 0, 15, paint);
        }
        int textureWidth = bitmap.getWidth();
        int textureHeight = bitmap.getHeight();
        Log.i("bitmap size", "width : "+textureWidth+" height : "+textureHeight);

        //テクスチャバッファの成分を有効にする。
        gl.glEnable(GL10.GL_TEXTURE_2D);
        //テクスチャ用メモリを指定数確保（ここではテクスチャ１枚）
        int[] buffers = new int[1];
        //１．一度に確保するテクスチャの枚数を指定
        //２．確保したテクスチャの管理番号を書き込む為の配列を指定。
        //３．オフセット値：配列の何番目からに書き込むかの指定
        gl.glGenTextures(1, buffers, 0);
        //テクスチャ管理番号を保存する
        int textureName = buffers[0];
        //テクスチャ情報の設定
        //１．GL_TEXTURE_2D　を指定。
        //２．テクスチャ管理番号
        gl.glBindTexture(GL10.GL_TEXTURE_2D, textureName);
        //ビットマップのメモリからテクスチャへ転送。
        //１．GL_TEXTURE_2D　を指定。
        //２．ミップマップレベル（２Dには関係なし）
        //３．画像を格納したbitmapを指定
        //４．テクスチャ境界（常に０で良い）
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
        //フィルタ
        //画像の引き伸ばしの方法を指定する必要がある（デフォルトで指定されていない）
        //１．GL_TEXTURE_2D：２Dのテクスチャ
        //２．拡大時：GL_TEXTURE_MAG_FILTER　縮小時：GL_TEXTURE_MIN_FILTER
        //３．GL_NEAREST：処理対象に最も近いピクセルの色を参照する
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
                GL10.GL_NEAREST);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER,
                GL10.GL_NEAREST);
        //bitmapを破棄
        bitmap.recycle();
    }

    @Override public void onDrawFrame(GL10 gl) {
        // 描画用バッファをクリア
        // gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);


        // RGBA（0.0～1.0へ正規化）を引数にして登録
        gl.glClearColor(0.0f, 1.0f, 1.0f, 1.0f);
        // どのバッファ（レイヤー）を指定するか？
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
        //-------------------------------------------------------------

        m_pic.Draw(gl);     // 画像を描画

        // テクスチャ座標の位置とデータを指定
        // 位置情報 uvは左上が(0,0)
        // ※本当は左下が(0,0)だが画像が上下ひっくり返る為、左上が(0,0)と考える
        float uv[] = {
                0.0f, 0.0f,// !< 左上
                0.0f, 1.0f,// !< 左下
                1.0f, 0.0f,// !< 右上
                1.0f, 1.0f,// !< 右下
        };

        // OpenGLESがVMとは違う領域で動作する為、
        // 直接参照可能なメモリを用意し保存。
        // Floatなので、要素数×４バイトを確保
        ByteBuffer bbuv = ByteBuffer.allocateDirect(uv.length * 4);
        // （Big　or　Little）エンディアンの呼び出し、および設定。
        bbuv.order(ByteOrder.nativeOrder());
        // Floatを書き込むための補助クラス
        FloatBuffer fbuv = bbuv.asFloatBuffer();
        // Bufferに配列を転送する
        fbuv.put(uv);
        // 書き込み位置を最初に戻す
        fbuv.position(0);

        // テクスチャバッファの成分を有効にする。
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        // １．要素数uv 2つ
        // ２．Bufferが格納されている型の指定
        // ３．第一引数を読み込んだ後、読み込みをスキップする数
        // ４．値が格納されているバッファの指定
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, fbuv);
        //-------------------------------------------------------------
        //-------------------------------------------------------------
        // ! 位置情報
        float positions[] = {
                // ! x y z
                -1.0f, 1.0f, 0.0f, // !< 左上（uv一行目に対応）
                -1.0f,-1.0f, 0.0f, // !< 左下（uv二行目に対応）
                1.0f, 1.0f, 0.0f, // !< 右上（uv三行目に対応）
                1.0f,-1.0f, 0.0f, // !< 右下（uv四行目に対応）
        };

        // ! OpenGLはビッグエンディアンではなく、CPUごとの
        //ネイティブエンディアンで数値を伝える必要がある。
        // ! そのためJavaヒープを直接的には扱えず、
        //java.nio配下のクラスへ一度値を格納する必要がある。
        ByteBuffer bb = ByteBuffer.allocateDirect(positions.length * 4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(positions);
        fb.position(0);

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, fb);
        // １．モード（GL_TRIANGLE_STRIP：連続した三角形）
        // ２．何番目の頂点から描画を行なうかを指定
        // ３．いくつの頂点を利用するかを指定
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
        //-------------------------------------------------------------

    }

    public void AccelerometerNotify( float x, float y ){    // 加速度センサー通知
        if(m_pic == null){
            return;
        }
        int iX = (int)(x * 3.0);      // X方向移動量 : センサー出力値 x 3[pixel]動かす
        int iY = (int)(y * 3.0);      // Y方向移動量 : センサー出力値 x 3[pixel]動かす
        // 制御対象画像の位置とサイズを取得
        int pic_w = m_pic.get_w();
        int pic_h = m_pic.get_h();
        int pic_x = m_pic.get_pos_x();
        int pic_y = m_pic.get_pos_y();
        // 移動後の位置を算出
        int new_x = pic_x;
        int new_y = pic_y;
        // X軸
        new_x = pic_x - iX;
        if(iX > 0) {         // 左回転？
            if(new_x < 0){
                new_x = 0;
            }
        }
        else if(iX < 0) {   // 右回転？
            int lim_x = m_view_w - pic_w;
            if(new_x > lim_x){
                new_x = lim_x;
            }
        }
        // Y軸
        new_y = pic_y - iY;
        if(iY > 0) {         // 手前回転？ ※画面下方向（－方向）へ移動
            if(new_y < 0){
                new_y = 0;
            }
        }
        else if(iY < 0){    // 奥回転　※画面上方向（＋方向）へ移動
            int lim_y = m_view_h - pic_h;
            if(new_y > lim_y){
                new_y = lim_y;
            }
        }
        // 画像の位置を変更
        m_pic.SetPosXY(new_x, new_y);
    }
}