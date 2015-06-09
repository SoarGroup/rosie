package edu.umich.rosie.language;

import java.io.IOException;
import java.io.File;

import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioFileFormat;

public class AudioRecorder extends Thread
{
	private AudioFileFormat.Type fileType;
    private TargetDataLine dataline;
    private AudioInputStream audioInputStream;
    private File file;

    /*
     * constructor
     */
    public AudioRecorder(TargetDataLine line,
			       AudioFileFormat.Type type,
			       File file)
    {
    	this.dataline = line;
    	this.audioInputStream = new AudioInputStream(line);
    	this.fileType = type;
    	this.file = file;
    }

    public void start()
    {
    	dataline.start();
    	super.start();
    }
    public void stopRecording()
    {
    	dataline.stop();
    	dataline.close();
    }
    
    /*
     * overload run, called on thread start
     */
    public void run()
    {
    	try
    	{
    		AudioSystem.write(audioInputStream, fileType, file);
		}
    	catch (IOException e)
	    {
    		e.printStackTrace();
	    }
    }
}