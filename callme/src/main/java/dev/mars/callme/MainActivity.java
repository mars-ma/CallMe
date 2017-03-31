package dev.mars.callme;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telecom.Call;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import dev.mars.callme.base.BaseActivity;
import dev.mars.callme.common.Constants;
import dev.mars.callme.event.CallingEvent;
import dev.mars.callme.event.OnCallEvent;
import dev.mars.callme.service.CommunicateService;
import dev.mars.audio.AudioUtils;
import dev.mars.audio.LogUtils;
import dev.mars.audio.NativeLib;

import static android.Manifest.permission.CHANGE_WIFI_MULTICAST_STATE;

public class MainActivity extends BaseActivity {

    String[] permissions = {Manifest.permission.ACCESS_WIFI_STATE, CHANGE_WIFI_MULTICAST_STATE,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO};
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
            if (hasPermissions(permissions)) {
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
            if(!hasPermissions(permissions)){
                finish();
            }else{
                init();
            }
        }

    }

    private void init() {
        CommunicateService.startListen(MainActivity.this, Constants.UDP_PORT,Constants.TCP_PORT);
    }
    

    public void startCall(View view) {
        CommunicateService.startCall(getContext());
    }

    public void endCall(View view) {
        CommunicateService.endCall(getContext());
    }

    public void sendText(View view) {
        CommunicateService.sendText(getContext(),editText.getText().toString());
    }

    public Context getContext(){
        return MainActivity.this;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(CallingEvent event){
        CallingActivity.calling(getContext(),event.ip);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(OnCallEvent event){
        OnCallActivity.onCall(getContext(),event.ip);
    }



    public void testRecord(View view) {
        CommunicateService.startRecord(getActivity());
    }

    public void stopRecord(View view) {
        CommunicateService.stopRecord(getActivity());
    }

    public void testPlay(View view) {
        CommunicateService.startPlay(getActivity());
    }

    public void stopPlay(View view) {
        CommunicateService.stopPlay(getActivity());
    }
}
