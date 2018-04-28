package cicinnus.com.aartest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import cicinnus.com.aarlibrary.AARUtil;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AARUtil aarUtil = new AARUtil();
    }
}
