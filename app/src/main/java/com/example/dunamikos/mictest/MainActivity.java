package com.example.dunamikos.mictest;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class MainActivity extends AppCompatActivity {

    private boolean streamTap; //True = audio streaming, false = stop streaming
    private AudioRecord recorder;

    private int port = 50008;

    private int sampleRate = 16000;
    private int channelConfig = AudioFormat.CHANNEL_OUT_MONO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    int minBufSize = AudioRecord.getMinBufferSize(sampleRate,channelConfig,audioFormat);

    Button startStreamingBtn;
    Button stopStreamingBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startStreamingBtn = (Button) findViewById(R.id.startButton);
        startStreamingBtn.setOnClickListener(startListener);

        stopStreamingBtn = (Button) findViewById(R.id.stopButton);
        stopStreamingBtn.setOnClickListener(stopListener);
    }

    private final View.OnClickListener stopListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            streamTap = false;
            recorder.release();
            Log.d("VS","Recorder released");
        }
    };

    private final View.OnClickListener startListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            streamTap = true;
            startStreaming();
        }
    };

    private void startStreaming(){
        Thread streamThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    DatagramSocket datagramSocket = new DatagramSocket();
                    Log.d("VS","Datagram socket opened.");

                    byte[] buffer = new byte[minBufSize];
                    Log.d("VS","Buffer created of size " + minBufSize);

                    DatagramPacket datagramPacket;

                    final InetAddress destination = InetAddress.getByName("127.0.0.1");
                    Log.d("VS","IP address is successfully added.");

                    recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,sampleRate,channelConfig,audioFormat,minBufSize*10);
                    Log.d("VS","Recorder initialized");

                    recorder.startRecording();

                    while(streamTap){
                        //reading data from MIC into buffer
                        minBufSize = recorder.read(buffer,0,buffer.length);

                        //putting buffer in the packet
                        datagramPacket = new DatagramPacket (buffer,buffer.length,destination,port);

                        datagramSocket.send(datagramPacket);
                        System.out.println("MinBufferSize: " +minBufSize);
                    }
                }catch (Exception e){
                    Log.e("VS",e.getMessage());
                }
            }
        });
    }
}
