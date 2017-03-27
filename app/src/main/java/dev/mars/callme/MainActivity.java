package dev.mars.callme;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import dev.mars.callme.remote.UDPUtils;
import dev.mars.callme.service.SocketService;
import dev.mars.callme.utils.IPUtils;
import dev.mars.callme.utils.LogUtils;
import dev.mars.callme.utils.WifiUtils;

import static android.Manifest.permission.CHANGE_WIFI_MULTICAST_STATE;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    String[] permissions = {Manifest.permission.ACCESS_WIFI_STATE, CHANGE_WIFI_MULTICAST_STATE};
    EditText editText;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventBus.getDefault().register(this);
        editText = (EditText) findViewById(R.id.etContent);
        textView = (TextView) findViewById(R.id.tvContent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.CHANGE_WIFI_MULTICAST_STATE) == PackageManager.PERMISSION_GRANTED
                    ) {
                init();
            } else {
                requestPermissions(permissions, 0);
            }
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void receiveRemoteTextMessage(String text){
        textView.setText("收到:"+text);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 0) {
            for (int res : grantResults) {
                if (res != PackageManager.PERMISSION_GRANTED) {
                    finish();
                }
            }
        }
        init();
    }

    private void init() {
        SocketService.start(MainActivity.this);
    }


    public void startCall(View view) {
        if(SocketService.STATE==0)
        SocketService.sendLocalIP(getContext());
    }

    public void endCall(View view) {
        if(SocketService.STATE!=0)
        SocketService.endCall(getContext());
    }

    public void sendText(View view) {
        SocketService.sendText(getContext(),editText.getText().toString());
    }

    public Context getContext(){
        return MainActivity.this;
    }
}
