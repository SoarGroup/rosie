package edu.umich.insoar.world;

import abolt.lcmtypes.observations_t;
import sml.Identifier;

public class WorldTime implements IInputLinkElement
{
    private long startTime = 0;
    
    private long currentTime = 0;
    
    private int stepNumber = 0;
    
    private Identifier timeId;
    
    public WorldTime(){
        timeId = null;
    }

    @Override
    public synchronized void updateInputLink(Identifier parentIdentifier)
    {
        if(timeId == null){
            timeId = parentIdentifier.CreateIdWME("time");
        }
        
        stepNumber++;
        WMUtil.updateIntWME(timeId, "steps", getSteps());
        WMUtil.updateIntWME(timeId, "seconds", (int)getSeconds());
        WMUtil.updateIntWME(timeId, "microseconds", (int)getMicroseconds());
    }
    
    public synchronized void newObservation(observations_t observation){
        if(startTime == 0){
            startTime = observation.utime;
        }
        currentTime = observation.utime;
    }

    @Override
    public synchronized void destroy()
    {
        timeId.DestroyWME();
    }
    
    public synchronized int getSteps(){
        return stepNumber;
    }
    
    public synchronized long getSeconds(){
        long seconds = 0;
        if(currentTime != 0){
            seconds = (currentTime - startTime)/1000000;
        }
        return seconds;
    }
    
    public synchronized long getMicroseconds(){
        long microseconds = 0;
        if(currentTime != 0){
            microseconds = currentTime - startTime;
        }
        return microseconds;
    }

}
