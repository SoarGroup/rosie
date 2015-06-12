package edu.umich.rosie.soarobjects;

import april.util.TimeUtil;
import sml.Identifier;
import edu.umich.rosie.soar.ISoarObject;
import edu.umich.rosie.soar.IntWME;

public class Time implements ISoarObject{
	
	private long startTime;
	private boolean added = false;

	private Identifier timeId = null;
	private IntWME seconds;
	private IntWME steps;
	
	public Time(){
		startTime = TimeUtil.utime();
		
		seconds = new IntWME("seconds", 0);
		steps = new IntWME("steps", 0);
	}

	@Override
	public void addToWM(Identifier parentId) {
		if(added){
			return;
		}
		
		timeId = parentId.CreateIdWME("time");
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
		
		seconds.setValue((int)((TimeUtil.utime() - startTime)/1000000));
		seconds.updateWM();
		
		steps.setValue(steps.getValue()+1);
		steps.updateWM();
	}

	@Override
	public void removeFromWM() {
		if(!added){
			return;
		}

		seconds.removeFromWM();
		steps.removeFromWM();
		timeId = null;

		added = false;
	}

}
