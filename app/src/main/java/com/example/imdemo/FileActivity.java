package com.example.imdemo;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Asus on 2017/3/8.
 */

public class FileActivity extends AppCompatActivity{
    private TextView tvPress,tvRes;
    private ExecutorService mExecutorService;
    private MediaRecorder mRecorder;
    private File mFile;
    private long mStarTime,mStopTime;
    private volatile boolean isPlaying;
    private MediaPlayer mMediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);
        initViews();
        mExecutorService= Executors.newSingleThreadExecutor();

    }

    private void initViews() {
        tvPress= (TextView) findViewById(R.id.tvPress);
        tvRes= (TextView) findViewById(R.id.tvRes);
        tvRes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mFile!=null&&!isPlaying){
                    isPlaying=true;
                    mExecutorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            doPlay(mFile);
                        }
                    });
                }else {

                }
            }
        });
        tvPress.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        startRecord();
                        break;
                    case MotionEvent.ACTION_UP:

                    case MotionEvent.ACTION_CANCEL:
                        stopRecord();
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
    }

    private void doPlay(File file) {
        try{
            mMediaPlayer=new MediaPlayer();
            mMediaPlayer.setDataSource(file.getAbsolutePath());
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    stopPlay();
                }
            });
            mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                    stopPlay();
                    return true;
                }
            });
            mMediaPlayer.setVolume(1,1);
            mMediaPlayer.setLooping(false);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        }catch (Exception e){
            stopPlay();
        }
    }


    private void startRecord() {

        tvPress.setText("开始说话");
        tvPress.setBackgroundResource(R.color.colorPrimaryDark);
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                releaseRecoder();
                if (!doStart()){
                    recordFail();
                }

            }
        });
    }

    private void stopRecord() {
        tvPress.setText("按住说话");
        tvPress.setBackgroundResource(R.color.colorAccent);
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {

                if (!doStop()){
                    recordFail();
                }
                releaseRecoder();
            }
        });
    }

    private void recordFail() {
        mFile=null;
    }

    private boolean doStart() {
        try {
            mRecorder=new MediaRecorder();
            mFile=new File(Environment.getExternalStorageDirectory().getAbsoluteFile(),"/zwe/"+ "sound.mp3");
            mFile.getParentFile().mkdirs();
            mFile.createNewFile();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mRecorder.setAudioSamplingRate(44100);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mRecorder.setAudioEncodingBitRate(96000);
            mRecorder.setOutputFile(mFile.getAbsolutePath());
            mRecorder.prepare();
            mRecorder.start();
            mStarTime=System.currentTimeMillis();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private void releaseRecoder() {
        if (mRecorder!=null){
            mRecorder.release();
            mRecorder=null;
        }
    }



    private boolean doStop() {
        try {
            mRecorder.stop();
            mStopTime=System.currentTimeMillis();
            final int second= (int)( (mStopTime-mStarTime)/1000);
            if (second>1){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvRes.setText("录音成功"+second+"秒");
                    }
                });
            }
        }catch (Exception e){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvRes.setText("fail");
                }
            });
            return false;
        }
      return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mExecutorService.shutdownNow();
        releaseRecoder();
        stopPlay();
    }

    private void stopPlay() {

        isPlaying=false;

        if (mMediaPlayer!=null){
            mMediaPlayer.setOnCompletionListener(null);
            mMediaPlayer.setOnErrorListener(null);
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer=null;
        }
    }
}
