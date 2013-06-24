package edu.umich.insoar.world;

import java.util.HashMap;
import sml.Agent;

public class SVSCommands {
    
    public static String add(WorldObject obj){
    	return String.format("a %s object world v %s p %s\n", obj.getIdString(), bboxToStr(obj.bbox), poseToStr(obj.pose));
    }
    
    public static String changePos(WorldObject obj){
    	return String.format("c %s p %s\n", obj.getIdString(), poseToStr(obj.pose));
    }
    
    public static String changeBBox(WorldObject obj){
    	return String.format("c %s v %s\n", obj.getIdString(), bboxToStr(obj.bbox));
    }
    
    public static String delete(WorldObject obj){
    	return String.format("d %s\n", obj.getIdString());
    }
    
    public static String addProperty(WorldObject obj, String propName, String value){
    	return String.format("p %s a %s %s\n", obj.getIdString(), propName, value);
    }
    
    public static String changeProperty(WorldObject obj, String propName, String value){
    	return String.format("p %s c %s %s\n", obj.getIdString(), propName, value);
    }
    
    public static String deleteProperty(WorldObject obj, String propName){
    	return String.format("p %s d %s\n", obj.getIdString(), propName);
    }
    
    public static String bboxToStr(double[][] bbox){
    	StringBuilder sb = new StringBuilder();
        for(int i = 0; i < 2; i++){
        	for(int j = 0; j < 2; j++){
        		for(int k = 0; k < 2; k++){
        			if(i > 0 || j > 0 || k > 0){
        				sb.append(" ");
        			}
        			sb.append(bbox[i][0] + " " + bbox[j][1] + " " + bbox[k][2]);
        		}
        	}
        }
        return sb.toString();
    }
    
    public static String poseToStr(double[] pose){
    	return String.format("%f %f %f", pose[0], pose[1], pose[2]);
    }    
}
