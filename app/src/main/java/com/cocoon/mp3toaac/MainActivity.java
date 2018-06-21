package com.cocoon.mp3toaac;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.File;

/**
 * 将mp3转成aac 写入我们自己的adts 不用苹果那种了  md
 * 先将音频解码  然后编码成aac
 * */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private String[] paths = {Constants.getPath(MainActivity.this,"audio/","running_distance.mp3"),
            Constants.getPath(this,"audio/","s1.mp3"),Constants.getPath(this,"audio/","kilometer.mp3")};
    private String[] aacPaths = {Constants.getPath(MainActivity.this,"audio/","running_distance.m4a"),
            Constants.getPath(this,"audio/","s1.m4a"),Constants.getPath(this,"audio/","kilometer.m4a")};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.audio_change).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.audio_change:
                //读取指定的文件夹
                String path = Constants.getPath(this,"raw/");
                File file = new File(path);
                if (file.exists() && file.isDirectory()){
                    File[] files = file.listFiles();
                    for (int i = 0; i < files.length; i++) {
                        File file1 = files[i];
                        String name = file1.getName();
                        if (file1.isFile() && (name.endsWith(".mp3") || name.endsWith("wav"))){
                            String simpleName = name.substring(0,name.indexOf("."));

                            final String pcmPath = Constants.getPath(this, "pcm/", simpleName+ ".pcm");
                            final String aacPath  = Constants.getPath(this,"audio/aac/",simpleName+".m4a");
                                Log.e("hero","  打印路径  "+file.getPath()+"    "+file.getAbsolutePath());
                                AudioCodec.getPCMFromAudio(file.getAbsolutePath()+"/"+name, pcmPath, new AudioCodec.AudioDecodeListener() {
                                    @Override
                                    public void decodeOver() {
                                        Log.e("hero","  音频解码完成完成  "+pcmPath);
                                        AudioCodec.PCM2Audio(pcmPath,aacPath, new AudioCodec.AudioDecodeListener() {
                                            @Override
                                            public void decodeOver() {
                                                Log.e("hero","  音频转换完成  ");
                                            }

                                            @Override
                                            public void decodeFail() {

                                            }
                                        });
                                    }

                                    @Override
                                    public void decodeFail() {

                                    }
                                });
                        }
                    }
                }
               /* for (int i = 0; i < paths.length; i++) {
                    String audioPath = paths[i];
                    final String pcmPath = Constants.getPath(this, "pcm/", i + ".pcm");

                    final int finalI = i;
                    Log.e("hero","  打印路径  "+audioPath+"    "+pcmPath);
                    AudioCodec.getPCMFromAudio(audioPath, pcmPath, new AudioCodec.AudioDecodeListener() {
                        @Override
                        public void decodeOver() {
                            Log.e("hero","  音频解码完成完成  "+pcmPath);
                            AudioCodec.PCM2Audio(pcmPath, aacPaths[finalI], new AudioCodec.AudioDecodeListener() {
                                @Override
                                public void decodeOver() {
                                    Log.e("hero","  音频转换完成  ");
                                }

                                @Override
                                public void decodeFail() {

                                }
                            });
                        }

                        @Override
                        public void decodeFail() {

                        }
                    });
                }*/

                break;
        }
    }
}
