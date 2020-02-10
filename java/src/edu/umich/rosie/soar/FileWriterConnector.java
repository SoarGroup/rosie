package edu.umich.rosie.soar;

import java.io.*;
import sml.*;

public abstract class FileWriterConnector extends AgentConnector {
	protected String filename;
    protected PrintWriter writer = null;

	public FileWriterConnector(SoarAgent agent, String filename){
		super(agent);

		this.filename = filename;
	}
	
	@Override
	public void connect(){
		super.connect();
		try{
            writer = new PrintWriter(new FileWriter(filename));
		} catch (FileNotFoundException e){
			System.err.println("File not found: " + filename);
		} catch (IOException e){
			System.err.println("IOException opening file " + filename);
		}
	}
	
	@Override
	public void disconnect(){
		if(writer != null){
			writer.flush();
			writer.close();
		}
		super.disconnect();
	}

	protected void onInitSoar(){ }

	protected void onInputPhase(Identifier inputLink){ }

	protected void onOutputEvent(String attName, Identifier id){ }
}
