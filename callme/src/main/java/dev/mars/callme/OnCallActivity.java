package dev.mars.callme;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import dev.mars.callme.base.BaseActivity;
import dev.mars.callme.event.SessionClosedEvent;
import dev.mars.callme.event.StartCommunicatingEvent;
import dev.mars.callme.service.CommunicateService;
import dev.mars.callme.utils.RingtonePlayer;

public class OnCallActivity extends BaseActivity {
    TextView textView;
    String otherIP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        setContentView(R.layout.activity_on_call);
        textView = (TextView) findViewById(R.id.textView);
        init();
    }

    private void init() {
        otherIP = getIntent().getStringExtra("IP");
        textView.setText("收到来电:"+otherIP);
        RingtonePlayer.play(getActivity());
    }

    public static void onCall(Context context, String ip){
        Intent intent = new Intent(context,OnCallActivity.class);
        intent.putExtra("IP",ip);
        context.startActivity(intent);
    }

    public void answerCall(View view) {
        CommunicateService.answerCall(getActivity());
        view.setVisibility(View.GONE);

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(StartCommunicatingEvent event){
        textView.setText("正在与 "+otherIP+" 通话");
        CommunicatingActivity.enter(getActivity(),otherIP);
        finish();
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
        RingtonePlayer.close();
    }
}
