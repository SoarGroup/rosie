package edu.umich.rosie.soarobjects;

import sml.Identifier;
import edu.umich.rosie.soar.ISoarObject;
import edu.umich.rosie.soar.IntWME;
import java.util.Calendar;

public class Time implements ISoarObject{
	private long startTime;
	private boolean added = false;

	private Identifier timeId = null;
	private IntWME milsecs;
	private IntWME seconds;
	private IntWME steps;
	
	public Time(){
		startTime = mstime();
		
		milsecs = new IntWME("milliseconds", 0L);
		seconds = new IntWME("seconds", 0L);
		steps = new IntWME("steps", 0L);
	}
	
	public static long mstime(){
		return (Calendar.getInstance()).getTimeInMillis();
	}

	@Override
	public void addToWM(Identifier parentId) {
		if(added){
			return;
		}
		
		timeId = parentId.CreateIdWME("time");
		milsecs.addToWM(timeId);
		seconds.addToWM(timeId);
		steps.addToWM(timeId);
		
		added = true;
	}

	@Override
	public boolean isAdded() {
		return added;
	}

	@Override
	public void updateWM() {
		if(!added){
			return;
		}

		milsecs.setValue(mstime() - startTime);
		milsecs.updateWM();
		
		seconds.setValue((mstime() - startTime)/1000);
		seconds.updateWM();
		
		steps.setValue(steps.getValue()+1);
		steps.updateWM();
	}

	@Override
	public void removeFromWM() {
		if(!added){
			return;
		}

		milsecs.removeFromWM();
		seconds.removeFromWM();
		steps.removeFromWM();
		timeId = null;

		added = false;
	}

}
