package edu.umich.rosie.soar;

import java.io.*;

import sml.*;

public abstract class FileWriterConnector extends AgentConnector {
	protected String filename;
    protected PrintStream writer = null;

	public FileWriterConnector(SoarClient client, String filename){
		super(client);

		this.filename = filename;
	}
	
	@Override
	public void connect(){
		super.connect();
		try{
            writer = new PrintStream(filename);
		} catch (FileNotFoundException e){
			System.err.println("File not found: " + filename);
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
