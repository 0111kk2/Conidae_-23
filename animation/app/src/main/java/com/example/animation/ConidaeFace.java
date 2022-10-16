package com.example.animation;

import android.widget.ImageView;
import android.os.Bundle;


import androidx.appcompat.app.AppCompatActivity;

public class ConidaeFace extends AppCompatActivity {
    private ImageView face;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conidae_face);

        face = findViewById(R.id.face);




    }

    protected void onResume() {
        super.onResume();

        Thread thread = new Thread(() -> {
            runOnUiThread(() -> setnomalface());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            runOnUiThread(() -> setgoodface());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            runOnUiThread(() -> seterrorface());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            runOnUiThread(() -> setfightface());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            runOnUiThread(() -> setprogremface());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        });
        thread.start();

    }

    //ノーマル
    public void setnomalface(){
        face.setImageResource(R.drawable.fig11);
    }

    //笑顔
    public void setgoodface(){
        face.setImageResource(R.drawable.good);
    }

    //怒ってるor力込めてる感じの顔
    public void setfightface(){
        face.setImageResource(R.drawable.gannbaru);
    }

    //困ってる顔
    public void setprogremface(){
        face.setImageResource(R.drawable.progrem);
    }

    //虚無ったこにだえの顔
    public void seterrorface(){
        face.setImageResource(R.drawable.error);
    }

}
