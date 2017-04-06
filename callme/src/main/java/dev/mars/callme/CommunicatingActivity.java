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

            cbMic.setChecked(false);
            cbSpeaker.setChecked(false);
            cbNoiceClear.setChecked(binder.isNoiceClearEnable());
            cbEchoClear.setChecked(binder.isEchoClearEnable());

            cbMic.setOnCheckedChangeListener(onCheckedChangeListener);
            cbSpeaker.setOnCheckedChangeListener(onCheckedChangeListener);
            cbNoiceClear.setOnCheckedChangeListener(onCheckedChangeListener);
            cbEchoClear.setOnCheckedChangeListener(onCheckedChangeListener);

            cbMic.setChecked(false);
            cbSpeaker.setChecked(false);
            cbEchoClear.setChecked(true);
            cbNoiceClear.setChecked(true);
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
                    setMic();
                    break;
                case R.id.cbSpeaker:
                    setSpeaker();
                    break;
                case R.id.cbNoiseClear:
                    setNoiseClear();
                    break;
                case R.id.cbEchoClear:
                    setEchoClear();
                    break;
            }
        }
    };

    private void setEchoClear() {
        binder.setEchoClearEnable(cbEchoClear.isChecked());
    }

    private void setNoiseClear() {
        binder.setNoiseClearEnable(cbNoiceClear.isChecked());
    }

    private void setSpeaker() {
        if(cbSpeaker.isChecked()){
            binder.turnOnSpeaker();
        }else{
            binder.turnOffSpeaker();
        }
    }

    private void setMic() {
        if(cbMic.isChecked()){
           binder.turnOnMic();
        }else{
            binder.turnOffMic();
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
