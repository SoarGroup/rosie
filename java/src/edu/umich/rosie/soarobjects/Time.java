package edu.umich.rosie.soarobjects;

import sml.Identifier;
import edu.umich.rosie.soar.ISoarObject;
import edu.umich.rosie.soar.IntWME;
import edu.umich.rosie.soar.SoarUtil;
import java.util.Calendar;

public class Time implements ISoarObject{
	private long startTime;
	private boolean added = false;

	private int clockStep;

	private Identifier timeId = null;
	private IntWME milsecs;
	private IntWME seconds;
	private IntWME steps;

	private Identifier clockId = null;
	private IntWME clockHour;
	private IntWME clockMin;
	private IntWME clockSec;
	private IntWME clockMS;
	private IntWME clockEpoch;
	private IntWME[] clockWmes;
	
	public Time(int clockStep){
		this.clockStep = clockStep;
		
		this.milsecs = new IntWME("milliseconds", 0L);
		this.seconds = new IntWME("seconds", 0L);
		this.steps = new IntWME("steps", 0L);

		this.clockHour = new IntWME("hour", 0L);
		this.clockMin = new IntWME("minute", 0L);
		this.clockSec = new IntWME("second", 0L);
		this.clockMS = new IntWME("millisecond", 0L);
		this.clockEpoch = new IntWME("epoch", 0L);

		this.clockWmes = new IntWME[]{ clockHour, clockMin, clockSec, clockMS, clockEpoch };
		this.resetTime();
	}

	public void tick(){
		clockMS.setValue(clockMS.getValue() + clockStep);
		if(clockMS.getValue() > 1000){
			clockEpoch.setValue(clockEpoch.getValue() + clockMS.getValue() / 1000);
			clockSec.setValue(clockSec.getValue() + clockMS.getValue() / 1000);
			clockMS.setValue(clockMS.getValue() % 1000);
			if(clockSec.getValue() >= 60){
				clockMin.setValue(clockMin.getValue() + clockSec.getValue() / 60);
				clockSec.setValue(clockSec.getValue() % 60);
				if(clockMin.getValue() >= 60){
					clockHour.setValue( (clockHour.getValue() + clockMin.getValue() / 60) % 24 );
					clockMin.setValue(clockMin.getValue() % 60);
				}
			}
		}
	}

	private void resetTime(){
		this.startTime = mstime();
		this.clockHour.setValue(8L);
		this.clockMin.setValue(0L);
		this.clockSec.setValue(0L);
		this.clockMS.setValue(0L);
		this.clockEpoch.setValue(1605000000L);
	}

	public void setTime(long hour, long min){
		this.resetTime();
		clockHour.setValue(hour);
		clockMin.setValue(min);
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

		clockId = timeId.CreateIdWME("clock");
		for(IntWME wme : clockWmes){
			wme.addToWM(clockId);
		}
		
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

		this.tick();
		for(IntWME wme : clockWmes){
			wme.updateWM();
		}
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

		for(IntWME wme : clockWmes){
			wme.removeFromWM();
		}
		clockId = null;

		added = false;
	}

}
