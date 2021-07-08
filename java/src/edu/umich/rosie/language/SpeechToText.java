package edu.umich.rosie.language;

import java.io.File;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import edu.umich.rosie.language.LanguageConnector.MessageType;
import edu.umich.rosie.soar.SoarClient;

public class SpeechToText {
    // Audio Recorder for Speech to Text
    private AudioFormat audioFormat;
    private AudioRecorder recorder;
    private File audioFile;
    private DataLine.Info info;
    private TargetDataLine	targetDataLine;    

    private String speechFile;
    
    private SoarClient soarClient;

    public SpeechToText(String speechFile, SoarClient soarClient){
        this.speechFile = speechFile;
        this.soarClient = soarClient;

    	this.audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 16000.0F, 16, 1, 2, 16000.0F, false);
        this.audioFile = new File("audio_files/forward.raw");

        this.info = null;
        //new DataLine.Info(TargetDataLine.class, audioFormat);
        this.targetDataLine = null;
        //when to get line
        this.recorder = null;	
    }
   
	public void startListening(){
    	//start recorder thread
    	//create audiorecorder
        info = new DataLine.Info(TargetDataLine.class, audioFormat);
        try
        {
			targetDataLine = (TargetDataLine) AudioSystem.getLine(info);
			targetDataLine.open(audioFormat);
		}
        catch (LineUnavailableException e)
        {
			e.printStackTrace();
			System.exit(1);
		}
        recorder = new AudioRecorder(targetDataLine, AudioFileFormat.Type.WAVE, audioFile);
    	recorder.start();
	}
	
	public void stopListening(){
    	recorder.stopRecording();
    	
    	//decode audio through jni call to sphinx code
		String lmFile = speechFile + ".lm";
		String dicFile = speechFile + ".dic";
    	String result = new sphinxJNI().decodeAudio(lmFile, dicFile);
    	if (result != null)
    		result = result.toLowerCase();

    	System.out.println("STT Result: " + result);
		LanguageConnector conn = soarClient.getConnector(LanguageConnector.class);
    	conn.sendMessage(result, MessageType.INSTRUCTOR_MESSAGE);
	}
}
