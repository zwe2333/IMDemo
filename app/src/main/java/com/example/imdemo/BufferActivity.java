package com.example.imdemo;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Asus on 2017/3/8.
 */

public class BufferActivity extends AppCompatActivity {
    private TextView tvShow,tvStart;
    private volatile boolean isRecording;
    private ExecutorService mExecutorService;
    private File mFile;
    private long startTime,stopTime;
    private static final int BUFFER_SIZE=2048;
    private byte[] mBytes;
    private FileOutputStream mFileOutputStream;
    private AudioRecord mAudioRecord;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buffer);
        initViews();
        mBytes=new byte[BUFFER_SIZE];
        mExecutorService= Executors.newSingleThreadExecutor();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mExecutorService.shutdownNow();
    }

    private void initViews() {
        tvStart= (TextView) findViewById(R.id.tvStart);
        tvShow= (TextView) findViewById(R.id.tvShow);
        tvStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isRecording){
                    tvStart.setText("点击说话");
                    tvStart.setBackgroundResource(R.color.colorAccent);

                    isRecording=false;
                }else {
                    tvStart.setText("开始说话");
                    tvStart.setBackgroundResource(R.color.colorPrimaryDark);

                    isRecording=true;

                    mExecutorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            if (!startRecord()){
                                recordFail();
                            }
                        }
                    });
                }
            }
        });
    }

    private void recordFail() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvShow.setText("失败");
                isRecording=false;
                tvStart.setText("点击说话");
            }
        });

    }

    private boolean startRecord() {
        try{
            mFile=new File(Environment.getExternalStorageDirectory().getAbsoluteFile(),"/zwe/"+ System.currentTimeMillis()+".pcm");
            mFile.getParentFile().mkdirs();
            mFile.createNewFile();
            mFileOutputStream=new FileOutputStream(mFile);

            int audioSource= MediaRecorder.AudioSource.MIC;
            int sampleRate=44100;
            int channelConfig= AudioFormat.CHANNEL_IN_MONO;
            int audioFormat=AudioFormat.ENCODING_PCM_16BIT;
            int minBufferSize=AudioRecord.getMinBufferSize(sampleRate,channelConfig,audioFormat);

            mAudioRecord=new AudioRecord(audioSource,sampleRate,channelConfig,audioFormat,Math.max(minBufferSize,BUFFER_SIZE));

            mAudioRecord.startRecording();

            startTime=System.currentTimeMillis();

            while (isRecording){
                int read=mAudioRecord.read(mBytes,0,BUFFER_SIZE);
                if (read>0){
                    mFileOutputStream.write(mBytes,0,read);
                }else {
                    return false;
                }
            }
            return stopRecord();
        }catch (Exception e){
            return false;
        }finally {
            if (mAudioRecord!=null){
                mAudioRecord.release();
            }
        }

    }

    private boolean stopRecord() {

        try {
            mAudioRecord.stop();
            mAudioRecord.release();
            mAudioRecord=null;
            mFileOutputStream.close();
            stopTime=System.currentTimeMillis();
            final int time= (int) ((stopTime-startTime)/1000);
            if (time>2){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvShow.setText("录制成功"+time+"秒");
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
