package dev.mars.callme.base;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

/**
 * Created by ma.xuanwei on 2017/3/30.
 */

public class BaseActivity extends AppCompatActivity {

    public Activity getActivity(){
        return BaseActivity.this;
    }

    public void showToast(String str){
        Toast.makeText(getActivity(),str,Toast.LENGTH_SHORT).show();
    }
}
