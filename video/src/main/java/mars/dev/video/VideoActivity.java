package mars.dev.video;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;

import java.io.IOException;

public class VideoActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    SurfaceView surfaceView;
    SurfaceHolder holder;
    Camera camera;

    Switch cameraSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        cameraSwitch = (Switch) findViewById(R.id.cameraSwitch);
        init();

    }

    private void init() {
        holder = surfaceView.getHolder();//获得句柄
        holder.addCallback(this);//添加回调
        cameraSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                switchCamera(isChecked);
            }
        });
    }

    private void switchCamera(boolean front) {
        //切换前后摄像头
        int cameraCount = 0;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();//得到摄像头的个数
        LogUtils.DT("摄像头个数:"+cameraCount);
        if(cameraCount<=1){
            return;
        }

        for(int i = 0; i < cameraCount; i ++) {
            Camera.getCameraInfo(i, cameraInfo);//得到每一个摄像头的信息
            if(front) {
                if(cameraInfo.facing  == Camera.CameraInfo.CAMERA_FACING_FRONT) {//代表摄像头的方位，CAMERA_FACING_FRONT前置      CAMERA_FACING_BACK后置
                    camera.stopPreview();//停掉原来摄像头的预览
                    camera.release();//释放资源
                    camera = null;//取消原来摄像头
                    camera = Camera.open(i);//打开当前选中的摄像头
                    try {
                        camera.setPreviewDisplay(holder);//通过surfaceview显示取景画面
                        resetCameraDisplayOrientation();
                        camera.startPreview();//开始预览
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    break;
                }
            } else {
                if(cameraInfo.facing  == Camera.CameraInfo.CAMERA_FACING_BACK) {//代表摄像头的方位，CAMERA_FACING_FRONT前置      CAMERA_FACING_BACK后置
                    camera.stopPreview();//停掉原来摄像头的预览
                    camera.release();//释放资源
                    camera = null;//取消原来摄像头
                    camera = Camera.open(i);//打开当前选中的摄像头
                    try {
                        camera.setPreviewDisplay(holder);//通过surfaceview显示取景画面
                        resetCameraDisplayOrientation();
                        camera.startPreview();//开始预览
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    break;
                }
            }

        }
    }

    private void resetCameraDisplayOrientation(){
        Camera.Parameters parameters = camera.getParameters();
        if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE)
        {
            //如果是竖屏
            parameters.set("orientation", "portrait");
            camera.setDisplayOrientation(90);
        } else{
            parameters.set("orientation", "landscape");
            camera.setDisplayOrientation(0);
        }
        camera.setParameters(parameters);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (camera == null) {
            camera = Camera.open();
            try {
                camera.setPreviewDisplay(holder);//通过surfaceview显示取景画面
                resetCameraDisplayOrientation();
                camera.startPreview();//开始预览


            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void relayout(int surfaceWidth, int surfaceHeight) {
        Camera.Parameters parameters = camera.getParameters();
        Camera.Size size = parameters.getPictureSize();
        LogUtils.DT("surfaceview width "+surfaceWidth+" height "+surfaceHeight);
        LogUtils.DT("camera width "+size.width+" height "+size.height);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        relayout(width, height);
       // camera.startPreview();// 开始预览
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        camera.stopPreview();
        camera.release();
        camera = null;
    }

    public static void enter(Context context) {
        Intent intent = new Intent(context, VideoActivity.class);
        context.startActivity(intent);
    }
}
