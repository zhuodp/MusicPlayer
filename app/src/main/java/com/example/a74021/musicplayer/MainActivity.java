package com.example.a74021.musicplayer;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.HeaderViewListAdapter;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;

import static com.example.a74021.musicplayer.R.id.time;

public class MainActivity extends AppCompatActivity {
    // private MusicService musicService;
    private MusicService musicService;

    private TextView musicStatus,musicTime,musicTotal;
    private SeekBar seekBar;


    private Button playOrPauseBtn;
    private Button stopBtn;
    private Button quitBtn;
    private Intent serviceIntent;

    private SimpleDateFormat time =new SimpleDateFormat("mm:ss");

    private boolean tag1=false;
    private boolean tag2=false;


    private void bindServiceConnection()
    {
        Intent intent =new Intent(MainActivity.this,MusicService.class);
        startService(intent);
        bindService(intent,serviceConnection,this.BIND_AUTO_CREATE);
    }

    //通过IBinder获取Service对象，实现Activity和Service的绑定
    private ServiceConnection serviceConnection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            musicService=((MusicService.MyBinder)(service)).getService();
            Log.i("musicService",musicService+"");
            musicTotal.setText(time.format(musicService.mediaPlayer.getDuration()));
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicService=null;
        }
    };

    //handler更新UI上的组件状态
    public Handler handler=new Handler();
    public Runnable runnable=new Runnable(){
        @Override
        public void run()
        {
            musicTime.setText(time.format(musicService.mediaPlayer.getCurrentPosition()));
            seekBar.setProgress(musicService.mediaPlayer.getCurrentPosition());
            seekBar.setMax(musicService.mediaPlayer.getDuration());
            musicTotal.setText(time.format(musicService.mediaPlayer.getDuration()));
            handler.postDelayed(runnable,200);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        musicTime=(TextView)findViewById(R.id.MusicTime);
        musicTotal=(TextView)findViewById(R.id.MusicTotal);
        seekBar =(SeekBar)findViewById(R.id.MusicSeekBar);
        playOrPauseBtn=(Button)findViewById(R.id.BtnPlayorPause);
        stopBtn=(Button)findViewById(R.id.BtnStop);
        quitBtn=(Button)findViewById(R.id.BtnQuit);
        musicStatus=(TextView)findViewById(R.id.MusicStatus);

        bindServiceConnection();
        verifyStoragePerisssions(this);

        //进度条
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar,int progress,boolean fromUser)
            {
                if(fromUser==true)
                {
                    musicService.mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public  void onStopTrackingTouch(SeekBar seekBar){}
        });




        //实现图片旋转
        ImageView imageView=(ImageView)findViewById(R.id.Image);
        final ObjectAnimator animator=ObjectAnimator.ofFloat(imageView,"rotation",0f,360.0f);
        animator.setDuration(10000);
        animator.setInterpolator(new LinearInterpolator());
        animator.setRepeatCount(-1);

        //  //设置各个控件的点击事件处理

        playOrPauseBtn = (Button) findViewById(R.id.BtnPlayorPause);
        playOrPauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //暂停和播放图标的切换
                if (musicService.tag != true) {
                    playOrPauseBtn.setText("播放");
                    animator.pause();
                    musicService.playOrPause();
                    musicService.tag = true;
                } else {
                    playOrPauseBtn.setText("暂停");
                    musicService.playOrPause();
                    musicService.tag = false;
                }
            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicStatus.setText("停止");
                playOrPauseBtn.setText("播放");
                animator.start();
                musicService.stop();
                animator.pause();
                musicService.tag=false;
            }
        });

        //停止service时解除绑定
        quitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.removeCallbacks(runnable);
                unbindService(serviceConnection);
                Intent intent=new Intent(MainActivity.this,MusicService.class);
                stopService(intent);
                try{
                    MainActivity.this.finish();
                }
                catch (Exception e){}
                }

        });





        }
    @Override
    public boolean onKeyDown(int keyCode ,KeyEvent event)
    {
        if (keyCode== KeyEvent.KEYCODE_BACK)
        {
            moveTaskToBack(false);
            return true;
        }
        return super.onKeyDown(keyCode,event);
    }


    public  void verifyStoragePerisssions(Activity activity)
    {
        try {
            int permission = ActivityCompat.checkSelfPermission(activity, "android.permission.READ_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(),"notPermission",Toast.LENGTH_SHORT).show();

            } else {
                //hasPermission=true;
                Toast.makeText(getApplicationContext(),"已经有权限",Toast.LENGTH_SHORT).show();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
