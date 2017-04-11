package mars.dev.video;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

/**
 * Created by mars_ma on 2017/4/11.
 */

public class BaseActivity  extends AppCompatActivity{
    public Activity getActivity(){
        return BaseActivity.this;
    }

    public void showToast(String str){
        Toast.makeText(getActivity(),str,Toast.LENGTH_SHORT).show();
    }

    @TargetApi(Build.VERSION_CODES.M)
    public boolean hasPermissions(String[] pers){
        for(String p:pers){
            if(checkSelfPermission(p) != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }
}
