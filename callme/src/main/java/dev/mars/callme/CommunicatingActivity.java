package dev.mars.callme;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import dev.mars.callme.base.BaseActivity;
import dev.mars.callme.event.SessionClosedEvent;
import dev.mars.callme.event.StartCommunicatingEvent;
import dev.mars.callme.service.CommunicateService;

public class CommunicatingActivity extends BaseActivity {

    TextView textView;
    String otherIP;
    CheckBox cbMic,cbSpeaker,cbEchoClear,cbNoiceClear;

    CommunicateService.CommunicateServiceBinder binder;

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder=(CommunicateService.CommunicateServiceBinder) service;
            cbMic.setOnCheckedChangeListener(null);
            cbSpeaker.setOnCheckedChangeListener(null);
            cbNoiceClear.setOnCheckedChangeListener(null);
            cbEchoClear.setOnCheckedChangeListener(null);

            cbMic.setChecked(binder.isMicOn());
            cbSpeaker.setChecked(binder.isSpeakerOn());
            cbNoiceClear.setChecked(binder.isNoiceClearEnable());
            cbEchoClear.setChecked(binder.isEchoClearEnable());

            cbMic.setOnCheckedChangeListener(onCheckedChangeListener);
            cbSpeaker.setOnCheckedChangeListener(onCheckedChangeListener);
            cbNoiceClear.setOnCheckedChangeListener(onCheckedChangeListener);
            cbEchoClear.setOnCheckedChangeListener(onCheckedChangeListener);

            cbMic.setChecked(true);
            cbSpeaker.setChecked(true);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            binder = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        setContentView(R.layout.activity_communicating);
        textView = (TextView) findViewById(R.id.textView);
        cbMic = (CheckBox) findViewById(R.id.cbMic);
        cbSpeaker = (CheckBox)findViewById(R.id.cbSpeaker);
        cbNoiceClear = (CheckBox) findViewById(R.id.cbNoiseClear);
        cbEchoClear = (CheckBox) findViewById(R.id.cbEchoClear);

        init();

        Intent intent = new Intent(getActivity(),CommunicateService.class);
        bindService(intent,serviceConnection,BIND_AUTO_CREATE);
    }

    private CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()){
                case R.id.cbMic:
                    openOrCloseMic();
                    break;
                case R.id.cbSpeaker:
                    openOrCloseSpeaker();
                    break;
                case R.id.cbNoiseClear:
                    openOrCloseNoiseClear();
                    break;
                case R.id.cbEchoClear:
                    break;
            }
        }
    };

    private void openOrCloseNoiseClear() {
        if(cbNoiceClear.isChecked()){
            binder.setNoiseClearEnable(true);
        }else{
            binder.setNoiseClearEnable(false);
        }
    }

    private void openOrCloseSpeaker() {
        if(cbSpeaker.isChecked()){
            CommunicateService.startPlay(getActivity());
        }else{
            CommunicateService.stopPlay(getActivity());
        }
    }

    private void openOrCloseMic() {
        if(cbMic.isChecked()){
            CommunicateService.startRecord(getActivity());
        }else{
            CommunicateService.stopRecord(getActivity());
        }
    }

    public static void enter(Context context, String ip){
        Intent intent = new Intent(context,CommunicatingActivity.class);
        intent.putExtra("IP",ip);
        context.startActivity(intent);
    }

    private void init() {
        otherIP = getIntent().getStringExtra("IP");
        textView.setText("正在与 "+otherIP+" 通话");
    }

    public void stopCalling(View view) {
        CommunicateService.stopCalling(getActivity());
        finish();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SessionClosedEvent event){
        showToast("通话被挂断");
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
