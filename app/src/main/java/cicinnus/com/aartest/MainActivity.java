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
        //使用本地aar会发现无法使用依赖传递,只能通过使用本地maven仓库,或者使用maven私服解决
//        AMap
    }
}
