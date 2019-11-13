package com.litifer.chat.audiotrack;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;

import static android.media.AudioFormat.ENCODING_PCM_16BIT;
import static android.media.AudioManager.MODE_IN_CALL;
import static android.media.AudioManager.MODE_NORMAL;
import static android.media.AudioManager.STREAM_MUSIC;

public class AudioTrackHelper {
    private String TAG = "AudioTrackHelper";
    private static AudioTrackHelper instance;
    private static AudioTrack mAudioTrack;
    private Context mContext;
    AudioManager audioManager;

    int AUDIO_FORMAT_ENCODING=ENCODING_PCM_16BIT;
    int SAMPLE_RATE=16000;
    int CHANNEL_OUT_CONFIG=AudioFormat.CHANNEL_OUT_MONO;
    int MODE=AudioTrack.MODE_STREAM;
    public static final int STREAM_TYPE = STREAM_MUSIC; //MODE_NORMAL;


    private AudioTrackHelper(int streamType, Context context){
        mContext= context;
        mAudioTrack = initiateAudioTrack(getNewAudioTrack(streamType));
        audioManager=(AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    public static AudioTrackHelper getInstance(int streamType, Context context){
        if (mAudioTrack==null)
            instance = new AudioTrackHelper(streamType, context);
        return instance;
    }

    public int write(byte[] data){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final ByteBuffer audioData = ByteBuffer.wrap(data);
            return mAudioTrack.write(audioData, audioData.remaining(), AudioTrack.WRITE_BLOCKING);
        } else
            return mAudioTrack.write(data, 0, data.length);
    }

    public void setVolume(float newVolume){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            mAudioTrack.setVolume(newVolume);
    }

    public float getMaxVolume(){
        return AudioTrack.getMaxVolume();
    }

    public float getMinVolume(){
        return AudioTrack.getMinVolume();
    }

    public void play(){
        if (mAudioTrack.getPlayState() != AudioTrack.PLAYSTATE_PLAYING){
//            setVolumeDefaultMedia(); //TODO
            mAudioTrack.play();
        }
    }

    public int getPlayState(){
        return mAudioTrack.getPlayState();
    }

    public void stop(){
        mAudioTrack.stop();
    }

    public void release(){
        mAudioTrack.release();
//        mAudioTrack=null;
        //TODO mAudioTrack=null, when audiotrack & audiorecord work parallely.
    }

    /**
     * @return AudioTrack object to write audio bytes & play.
     * AudioTrack is only used in output & not in input.
     */
    private AudioTrack getNewAudioTrack(int streamType) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return new AudioTrack.Builder()
                    .setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .setLegacyStreamType(streamType)
                            .setFlags(AudioAttributes.FLAG_LOW_LATENCY)//FLAG_SCO
//                                .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                            .build())
                    .setAudioFormat(new AudioFormat.Builder()
                            .setEncoding(AUDIO_FORMAT_ENCODING)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(CHANNEL_OUT_CONFIG)
                            .build())
                    .setBufferSizeInBytes(getOutputBufferSize())
//                        .setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
                    .setTransferMode(MODE)
                    .build();
        }
        return  new AudioTrack(streamType,
                SAMPLE_RATE,
                CHANNEL_OUT_CONFIG,
                AUDIO_FORMAT_ENCODING,
                getOutputBufferSize(),
                MODE);
    }

    private int getOutputBufferSize(){

        return AudioTrack.getMinBufferSize(SAMPLE_RATE,
                CHANNEL_OUT_CONFIG, //channel_in_config
                AUDIO_FORMAT_ENCODING);
    }

    /**
     * play audioTrack for the first time before writing bytes to it.
     * Mentioned in docs, it's required.
     * @param audioTrack
     * @return
     */
    private AudioTrack initiateAudioTrack(AudioTrack audioTrack) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            audioTrack.setVolume(100);
        }
        audioTrack.play();
//        audioTrack.play();
        return audioTrack;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public int getUnderrunCount(){
        return mAudioTrack.getUnderrunCount();
    }

    public int getLatency(){
        try {
            return (int) AudioTrack.class.getMethod("getLatency").invoke(mAudioTrack);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return -1;
    }

}
