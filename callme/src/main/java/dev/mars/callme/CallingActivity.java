package dev.mars.callme;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import dev.mars.callme.base.BaseActivity;
import dev.mars.callme.event.SessionClosedEvent;
import dev.mars.callme.event.StartCommunicatingEvent;
import dev.mars.callme.service.CommunicateService;

public class CallingActivity extends BaseActivity {
    TextView textView;
    String otherIP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        setContentView(R.layout.activity_calling);
        textView = (TextView) findViewById(R.id.textView);
        init();
    }

    public static void calling(Context context,String ip){
        Intent intent = new Intent(context,CallingActivity.class);
        intent.putExtra("IP",ip);
        context.startActivity(intent);
    }

    private void init() {
        otherIP = getIntent().getStringExtra("IP");
        textView.setText("正在呼叫:"+otherIP);
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(StartCommunicatingEvent event){
        textView.setText("正在与 "+otherIP+" 通话");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
