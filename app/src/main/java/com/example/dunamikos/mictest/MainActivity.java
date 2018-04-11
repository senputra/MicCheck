package com.example.dunamikos.mictest;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class MainActivity extends AppCompatActivity {

    private final String LOG_TAG = "VS";

    private boolean streamTap; //True = audio streaming, false = stop streaming
    private AudioRecord recorder;

    private int port = 5008;

    private boolean playbackTap; //True = audio playback, false = stop playback
    private AudioTrack audioTrack;

    private int sampleRate = 44100;
    private int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT ;
    int minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);

    Button startStreamingBtn;
    Button stopStreamingBtn;
    Button startPlaybackBtn;
    Button stopPlaybackBtn;

    private final String[] permissions = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAPTURE_AUDIO_OUTPUT,
            Manifest.permission.INTERNET
    };
    private final int permissionRequestCode = 50;
    private boolean permissionToRecordAccepted = false;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case permissionRequestCode:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted) finish();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startStreamingBtn = findViewById(R.id.startButton);
        startStreamingBtn.setOnClickListener(startListener);

        stopStreamingBtn = findViewById(R.id.stopButton);
        stopStreamingBtn.setOnClickListener(stopListener);

        startPlaybackBtn = findViewById(R.id.startPlaybackButton);
        startPlaybackBtn.setOnClickListener(startPlaybackListener);

        stopPlaybackBtn = findViewById(R.id.stopPlaybackButton);
        stopPlaybackBtn.setOnClickListener(stopPlaybackListener);

        //get permission to use mic
        ActivityCompat.requestPermissions(this, permissions, permissionRequestCode);

    }

    private final View.OnClickListener stopPlaybackListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(LOG_TAG, "playback button pressed");
            if (playbackTap) {
                playbackTap = false;
                audioTrack.release();
                Log.d(LOG_TAG, "Recorder released");
            }
        }
    };

    private final View.OnClickListener startPlaybackListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(LOG_TAG, "playback button pressed");
            if (!playbackTap) {
                playbackTap = true;
                startPlayback();
            }
        }
    };

    private void startPlayback() {
        Thread playbackThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DatagramSocket receivingSocket = new DatagramSocket(port);
                    Log.d(LOG_TAG, "Playback Socket is open. port: " + port);

                    byte[] buffer = new byte[minBufSize];
                    Log.d(LOG_TAG, "Buffer is prepared with size of: " + minBufSize);

                    DatagramPacket receivingPacket = new DatagramPacket(buffer, minBufSize);
                    Log.d(LOG_TAG, "Datagram Packet for playback is prepared");


                    audioTrack = new AudioTrack(
                            AudioManager.STREAM_MUSIC,
                            sampleRate,
                            AudioFormat.CHANNEL_OUT_MONO,
                            audioFormat,
                            minBufSize,
                            AudioTrack.MODE_STREAM
                    );

                    audioTrack.play();
                    Log.d(LOG_TAG, "Playback audiotrack has been started!");
                    while (playbackTap) {
                        receivingSocket.receive(receivingPacket);
                        buffer = receivingPacket.getData();

                        int x = audioTrack.write(buffer, 0, minBufSize);
                        Log.d(LOG_TAG, "audio track : " + minBufSize);

                    }

                    receivingSocket.close();
                    receivingSocket.disconnect();


                } catch (Exception e) {
                    Log.e(LOG_TAG, e.getMessage());
                }
            }
        });
        playbackThread.start();
    }

    private final View.OnClickListener stopListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (streamTap) {
                streamTap = false;
                recorder.release();
                Log.d(LOG_TAG, "Recorder released");
            }
        }
    };

    private final View.OnClickListener startListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!streamTap) {
                streamTap = true;
                startStreaming();
            }
        }
    };

    private void startStreaming() {
        Thread streamThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DatagramSocket datagramSocket = new DatagramSocket();
                    Log.d(LOG_TAG, "Datagram socket opened.");

                    byte[] buffer = new byte[minBufSize];
                    Log.d(LOG_TAG, "Buffer created of size " + minBufSize);

                    DatagramPacket datagramPacket;

                    final InetAddress destination = InetAddress.getByName("127.0.0.1");
                    Log.d(LOG_TAG, "IP address is successfully added.");

                    recorder = new AudioRecord(
                            MediaRecorder.AudioSource.MIC,
                            sampleRate,
                            channelConfig,
                            audioFormat,
                            minBufSize * 1);
                    Log.d(LOG_TAG, "Recorder initialized");

                    recorder.startRecording();

                    while (streamTap) {
                        //reading data from MIC into buffer
                        minBufSize = recorder.read(buffer, 0, buffer.length);

                        //putting buffer in the packet
                        datagramPacket = new DatagramPacket(buffer, buffer.length, destination, port);

                        datagramSocket.send(datagramPacket);
                        System.out.println("MinBufferSize: " + buffer.length + " " + buffer[0]);
                    }
                    datagramSocket.close();
                    datagramSocket.disconnect();
                } catch (Exception e) {
                    Log.e(LOG_TAG, e.getMessage());
                }
            }
        });
        streamThread.start();
    }
}
