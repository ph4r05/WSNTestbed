/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fi.wsnusbcollect.utils;

import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
//import org

public class AePlayWave extends Thread {

    private String filename;

    private Position curPosition;

    private final int EXTERNAL_BUFFER_SIZE = 524288; // 128Kb

    enum Position {
        LEFT, RIGHT, NORMAL
    };

    public AePlayWave(String wavfile) {
        filename = wavfile;
        curPosition = Position.NORMAL;
    }

    public AePlayWave(String wavfile, Position p) {
        filename = wavfile;
        curPosition = p;
    }

    public void run() {

        File soundFile = new File(filename);
        if (!soundFile.exists()) {
            System.err.println("Wave file not found: " + filename);
            return;
        }

        AudioInputStream audioInputStream = null;
        try {
            audioInputStream = AudioSystem.getAudioInputStream(soundFile);
        } catch (UnsupportedAudioFileException e1) {
            e1.printStackTrace();
            return;
        } catch (IOException e1) {
            e1.printStackTrace();
            return;
        }

        AudioFormat format = audioInputStream.getFormat();
        SourceDataLine auline = null;
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        //DataLine.Info info = new DataLine.Info(AlsaSourceDataLine.class, format);

        Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();

        try {
            ; //System.err.println("Available mixers:");
            for (int cnt = 0; cnt < mixerInfo.length;cnt++) {
                ;//System.err.println("i=" + cnt + "; " + mixerInfo[cnt].getName() + "; " + mixerInfo[cnt].getDescription() );
            }//end for loop
        } catch (Exception e) {
           System.err.println("Exception e: " + e.toString());
        }

        try {
            //Mixer mixer = AudioSystem.getMixer(mixerInfo[0]);
            //auline = (SourceDataLine) mixer.getLine(info);
            auline = (SourceDataLine) AudioSystem.getLine(info);

            auline.open(format);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
            return;
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        if (auline.isControlSupported(FloatControl.Type.PAN)) {
            FloatControl pan = (FloatControl) auline
                    .getControl(FloatControl.Type.PAN);
            if (curPosition == Position.RIGHT)
                pan.setValue(1.0f);
            else if (curPosition == Position.LEFT)
                pan.setValue(-1.0f);
        }

        auline.start();
        int nBytesRead = 0;
        byte[] abData = new byte[EXTERNAL_BUFFER_SIZE];

        try {
            while (nBytesRead != -1) {
                nBytesRead = audioInputStream.read(abData, 0, abData.length);
                if (nBytesRead >= 0)
                    auline.write(abData, 0, nBytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        } finally {
            auline.drain();
            auline.close();
        }

    }
}
 