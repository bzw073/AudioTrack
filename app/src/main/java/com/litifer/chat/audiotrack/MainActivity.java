package com.litifer.chat.audiotrack;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {

    private int REQUEST_ENABLE_BT = 1;
    /* Used to handle permission request */
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private BluetoothReceiver bluetoothReceiver;
    private static final String TAG="Main Activity";
    private byte[] music;
    private AudioTrackHelper mAudioTrackHelper;
    private AudioManager audioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mAudioTrackHelper = AudioTrackHelper.getInstance(AudioTrackHelper.STREAM_TYPE, this);
        audioManager=(AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Check if user has given permission to record audio
        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
            return;
        }
        bluetoothReceiver = new BluetoothReceiver(this, mListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bluetoothReceiver == null){
            Log.e(TAG, "Bluetooth Receiver is null before unregistering in onDestroy");
            return;
        }
        bluetoothReceiver.closeReceiver();
        bluetoothReceiver=null;
    }

    public void connectHSP(View v){
        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }
        Intent intent = new Intent(this, Connector.class);
        startService(intent);
    }

    public void play(View v){
        if (music==null)
            return;
        mAudioTrackHelper.write(music);
        mAudioTrackHelper.play();

    }

    public void volumeup(View v){
        audioManager.adjustStreamVolume(AudioTrackHelper.STREAM_TYPE, AudioManager.ADJUST_RAISE, 0);
    }

    public void volumedown(View v){
        audioManager.adjustStreamVolume(AudioTrackHelper.STREAM_TYPE, AudioManager.ADJUST_LOWER, 0);
    }

    private BluetoothReceiver.BluetoothListener mListener = new BluetoothReceiver.BluetoothListener() {
        @Override
        public void onBluetoothConnect() {
            try {
                Log.d(TAG, "bluetooth connected.");
                music=new byte[100000];//size & length of the file
                InputStream is = getResources().openRawResource(R.raw.sample);
                BufferedInputStream bis = new BufferedInputStream(is, 8000);
                DataInputStream dis = new DataInputStream(bis);      //  Create a DataInputStream to read the audio data from the saved file

                int i = 0;                                                          //  Read the file into the "music" array
                while (dis.available() > 0)
                {
                    music[i] = dis.readByte();                                      //  This assignment does not reverse the order
                    i++;
                }
                dis.close();
                mAudioTrackHelper.write(music);
                mAudioTrackHelper.play();
            }catch (IOException e){
                e.printStackTrace();
            }

        }

        @Override
        public void onBluetoothDisconnect() {
            Log.d(TAG, "bluetooth disconnected.");
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Recognizer initialization is a time-consuming and it involves IO,
                // so we execute it in async task
//                 new SetupTask(this).execute();
                bluetoothReceiver = new BluetoothReceiver(this, mListener);
            } else {
                finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REQUEST_ENABLE_BT && resultCode== Activity.RESULT_OK){
            Intent intent = new Intent(this, Connector.class);
            startService(intent);
        }
    }
}
