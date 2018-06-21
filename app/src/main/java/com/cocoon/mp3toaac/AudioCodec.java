package com.cocoon.mp3toaac;

import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;

/**
 * Created by cj on 2017/11/5.
 * 音频相关的操作类
 */

public class AudioCodec {
    final static int TIMEOUT_USEC = 0;
    private static Handler handler = new Handler(Looper.getMainLooper());

    /**
     * 将音频文件解码成原始的PCM数据
     */
    public static void getPCMFromAudio(String audioPath, String audioSavePath, final AudioDecodeListener listener) {
        MediaExtractor extractor = new MediaExtractor();
        int audioTrack = -1;
        boolean hasAudio = false;
        try {
            extractor.setDataSource(audioPath);
            for (int i = 0; i < extractor.getTrackCount(); i++) {
                MediaFormat trackFormat = extractor.getTrackFormat(i);
                String mime = trackFormat.getString(MediaFormat.KEY_MIME);
                if (mime.startsWith("audio/")) {
                    audioTrack = i;
                    hasAudio = true;
                    break;
                }
            }
            if (hasAudio) {
                extractor.selectTrack(audioTrack);

                //原始音频解码
                new Thread(new AudioDecodeRunnable(extractor, audioTrack, audioSavePath, new DecodeOverListener() {
                    @Override
                    public void decodeIsOver() {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (listener != null){
                                    listener.decodeOver();
                                }
                            }
                        });
                    }
                    @Override
                    public void decodeFail() {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (listener != null){
                                    listener.decodeFail();
                                }
                            }
                        });
                    }
                })).start();

            } else {
                Log.e("hero", " select audio file has no auido track");
                if (listener != null){
                    listener.decodeFail();
                }
            }
        } catch (IOException  e) {
            e.printStackTrace();
            Log.e("hero", " decode failed !!!!");
            if (listener != null){
                listener.decodeFail();
            }
        }
    }
    /**
     * PCM文件转音频
     * 从pcm文件中读取byte数据
     * byte数据放进编码器
     * 把编码后的数据放进文件
     * */
    public static void PCM2Audio(String pcmPath,String audioPath,final AudioDecodeListener listener){
       new Thread(new AudioEncodeRunnable(pcmPath, audioPath, new AudioDecodeListener() {
           @Override
           public void decodeOver() {
               if (listener != null){
                   handler.post(new Runnable() {
                       @Override
                       public void run() {
                           listener.decodeOver();
                       }
                   });
               }
           }

           @Override
           public void decodeFail() {
               if (listener != null){
                   handler.post(new Runnable() {
                       @Override
                       public void run() {
                           listener.decodeFail();
                       }
                   });
               }
           }
       })).start();
    }

    /**
     * 写入ADTS头部数据
     * */
    public static void addADTStoPacket(byte[] packet, int packetLen) {
        int profile = 2; // AAC LC
        int freqIdx = 4; // 44.1KHz
        int chanCfg = 2; // CPE

        packet[0] = (byte) 0xFF;
        packet[1] = (byte) 0xF9;
        packet[2] = (byte) (((profile - 1) << 6) + (freqIdx << 2) + (chanCfg >> 2));
        packet[3] = (byte) (((chanCfg & 3) << 6) + (packetLen >> 11));
        packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
        packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
        packet[6] = (byte) 0xFC;
    }


    interface DecodeOverListener {
        void decodeIsOver();

        void decodeFail();
    }

    public interface AudioDecodeListener{
        void decodeOver();
        void decodeFail();
    }
}
