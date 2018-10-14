import java.io.ByteArrayInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;

/**
 * micServer
 */
public class micServer {

    static int port = 5008;
    static DatagramSocket serverSocket;
    static int minBufferSize = 3584;

    static AudioInputStream ais;
    static AudioFormat format;
    static int sampleRate = 44100;

    static SourceDataLine sourceDataLine;
    static DataLine.Info dataLineInfo;

    public static void main(String args[]) {
        startServer();

    }

    static public void startServer() {
        Thread mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    serverSocket = new DatagramSocket(port);
                    System.out.println("Server socket is made!");

                    byte buffer[] = new byte[minBufferSize];
                    System.out.println("Buffer is made!");

                    DatagramPacket datagramPacket = new DatagramPacket(buffer, minBufferSize);
                    System.out.println("Datagram packet is made!");

                    format = new AudioFormat(sampleRate, 16, 1, true, false);
                    System.out.println("Audio format for playback is made");

                    dataLineInfo = new DataLine.Info(SourceDataLine.class, format);
                    sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
                    System.out.println("DataLine created for sound");

                    sourceDataLine.open(format);
                    System.out.println("Playback prepared");

                    FloatControl volumeControl = (FloatControl) sourceDataLine
                            .getControl(FloatControl.Type.MASTER_GAIN);
                    volumeControl.setValue(6.0206f);

                    sourceDataLine.start();
                    System.out.println("Playback started");

                    // baiss is a stream that is connected to the datagram's buffer
                    ByteArrayInputStream baiss = new ByteArrayInputStream(datagramPacket.getData());
                    ais = new AudioInputStream(baiss, format, datagramPacket.getLength());

                    
                    while (true) {
                        serverSocket.receive(datagramPacket);
                        // buffer = datagramPacket.getData();
                        sourceDataLine.write(datagramPacket.getData(), 0, minBufferSize);
                        // new Thread(new Runnable(){
                        //     @Override
                        //     public void run() {
                        //         sourceDataLine.write(datagramPacket.getData(), 0, minBufferSize);
                        //         // System.out.println("playing byte[]");        
                        //     }
                        // }).start();
                        
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        mThread.start();
    }

}