package edu.umich.insoar;

import java.util.HashMap;

import april.util.TimeUtil;

public class SoarTimer {
	public static HashMap<Integer, Long> timers = new HashMap<Integer, Long>();
	public static void setTime(Integer timerNumber){
		timers.put(timerNumber, TimeUtil.utime());
	}
	public static Long getTime(Integer timerNumber){
		long t = TimeUtil.utime();
		long dt = 0;
		Long oldT = timers.get(timerNumber);
		if(oldT != null){
			dt = t - oldT;
		}
		timers.put(timerNumber, t);
		return dt;
	}
}
