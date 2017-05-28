package com.astro.kakaobot;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        ImageView imageView = (ImageView) findViewById(R.id.launcher_icon);
        imageView.setImageBitmap(BitmapFactory.decodeStream(getResources().openRawResource(R.raw.ic_launcher)));
    }

    public void toGithub(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Astro36/NewKakaoBot"));
        intent.setPackage("com.android.chrome");
        startActivity(intent);
    }
}
