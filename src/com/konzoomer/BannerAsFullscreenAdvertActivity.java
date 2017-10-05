package com.konzoomer;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Created by IntelliJ IDEA.
 * User: Torben Vesterager
 * Date: 29-01-2011
 * Time: 10:33:24
 */
public class BannerAsFullscreenAdvertActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.banner_as_fullscreen_advert);

        Button closeAdvert = (Button) findViewById(R.id.close_ad);
        closeAdvert.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                finish();
            }
        });
    }
}
