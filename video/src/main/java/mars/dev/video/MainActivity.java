package mars.dev.video;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import static android.Manifest.permission.CHANGE_WIFI_MULTICAST_STATE;

public class MainActivity extends BaseActivity{
    String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (hasPermissions(permissions)) {
            } else {
                requestPermissions(permissions, 0);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 0) {
            if(!hasPermissions(permissions)){
                finish();
            }else{
            }
        }

    }

    public void enterVideoTest(View view) {
        VideoActivity.enter(getActivity());
    }
}
