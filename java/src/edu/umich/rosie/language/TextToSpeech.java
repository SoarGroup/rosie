package edu.umich.rosie.language;

import javax.sound.sampled.*;
import java.io.*;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
/**
 * TextToSpeech class converts string to audio output using googles tts services
 * @author kirk
 *
 */
public class TextToSpeech
{
	Object speechLock = new Object();
	
	/**
	 * Constructor
	 */
	public TextToSpeech()
	{
	}
	
    public void speak(String words) 
    {
    	new Thread(new SpeechThread(words)).start();
    }
    
    class SpeechThread implements Runnable {
    	String words;
    	public SpeechThread(String words){
    		this.words = words;
    	}
    	
    	public void run(){
    		synchronized(speechLock){
	    		AudioInputStream din = null;
	        	try 
	        	{
	        		// Offline TTS option uses pico2wave program and temp test.wav file
	        	
	        		ProcessBuilder pb = new ProcessBuilder("pico2wave", "-l=en-GB", "-w=test.wav", words);
	        		
	        		Process p = pb.start();
	        		p.waitFor();
	        		File file = new File("test.wav");
	        		
	        		
	        		//ONLINE GOOGLE Text to Speech Services
	        		/*
	        		words = java.net.URLEncoder.encode(words, "UTF-8");
	        		URL url = new URL("http://translate.google.com/translate_tts?ie=UTF-8&tl=" + "en-UK" + "&q="+words);
	        		HttpURLConnection urlConn =(HttpURLConnection) url.openConnection();
	    	    
	        		urlConn.addRequestProperty("User-Agent", "Mozilla/5.0");
	        		InputStream audioStream = urlConn.getInputStream();
	        		DataInputStream data = new DataInputStream(audioStream);
	        		
	        		File file = new File("sound.mp3");

	        		OutputStream outstream = new FileOutputStream(file);
	        		byte[] buffer = new byte[1024];
	        		int len;
	        		
	        		while ((len = data.read(buffer)) > 0) 
	        		{
	        			outstream.write(buffer, 0, len);                
	        		}
	        		outstream.close();
	        		
	        		*/
	        		AudioInputStream in = AudioSystem.getAudioInputStream(file);
	        		AudioFormat baseFormat = in.getFormat();
	        		AudioFormat decodedFormat = new AudioFormat(
	        				AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(), 16, 
	        				baseFormat.getChannels(), baseFormat.getChannels() * 2, 
	        				baseFormat.getSampleRate(), false);
	    	    
	        		din = AudioSystem.getAudioInputStream(decodedFormat, in);
	        		DataLine.Info info = new DataLine.Info(SourceDataLine.class, decodedFormat);
	        		SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
	    	    
	        		if(line != null) 
	        		{
	        			line.open(decodedFormat);
	        			byte[] result = new byte[4096];
	    		
	        			// Start
	        			line.start();
	        			int nBytesRead;
	    		
	        			while ((nBytesRead = din.read(result, 0, result.length)) != -1)
	        			{
	        				line.write(result, 0, nBytesRead);
	        			}
	        			// Stop
	        			line.drain();
	        			line.stop();
	        			line.close();
	        			din.close();
	        		}
	        		
	    	    
	        	}
	        	catch(Exception e) 
	        	{
	        		e.printStackTrace();
	        	}
	        	finally 
	        	{
	        		if(din != null) 
	        		{
	        			try
	        			{ 
	        				din.close();
	        			} catch(IOException e) { }
	        		}
	        	}
	    	}
    	}
    }
}
