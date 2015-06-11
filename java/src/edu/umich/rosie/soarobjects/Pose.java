package edu.umich.rosie.soarobjects;

import edu.umich.rosie.soar.FloatWME;
import edu.umich.rosie.soar.ISoarObject;
import sml.FloatElement;
import sml.Identifier;

public class Pose implements ISoarObject
{
    private static String[] poseStrings = {"x", "y", "z", "roll", "pitch", "yaw"};
    private enum PoseIndex{X, Y, Z, ROLL, PITCH, YAW};
    private FloatWME[] pose;
    
    public Pose(){
    	initPose();
    }
    
    public Pose(double x, double y, double z, double roll, double pitch, double yaw){
    	initPose();
        pose[PoseIndex.X.ordinal()].setValue(x); 
        pose[PoseIndex.Y.ordinal()].setValue(y); 
        pose[PoseIndex.Z.ordinal()].setValue(z); 
        pose[PoseIndex.ROLL.ordinal()].setValue(roll); 
        pose[PoseIndex.PITCH.ordinal()].setValue(pitch);
        pose[PoseIndex.YAW.ordinal()].setValue(yaw);
    }
    
    public Pose(double[] poseValues){
    	initPose();
        updateWithArray(poseValues);
    }
    
    public Pose(String s){      
    	initPose();
        updateWithString(s);            
    }
    
    private void initPose(){
        pose = new FloatWME[6];
        for(int i = 0; i < 6; i++){
            pose[i] = new FloatWME(poseStrings[i], 0.0);
        }
    }
    
    public double getX(){
        return pose[PoseIndex.X.ordinal()].getValue();
    }
    public void setX(double x){
        pose[PoseIndex.X.ordinal()].setValue(x);
    }
    

    public double getY(){
        return pose[PoseIndex.Y.ordinal()].getValue();
    }
    public void setY(double y){
        pose[PoseIndex.Y.ordinal()].setValue(y);
    }
    

    public double getZ(){
        return pose[PoseIndex.Z.ordinal()].getValue();
    }
    public void setZ(double z){
        pose[PoseIndex.Z.ordinal()].setValue(z);
    }
    

    public double getRoll(){
        return pose[PoseIndex.ROLL.ordinal()].getValue();
    }
    public void setRoll(double roll){
        pose[PoseIndex.ROLL.ordinal()].setValue(roll);
    }
    

    public double getPitch(){
        return pose[PoseIndex.PITCH.ordinal()].getValue();
    }
    public void setPitch(double pitch){
        pose[PoseIndex.PITCH.ordinal()].setValue(pitch);
    }
    

    public double getYaw(){
        return pose[PoseIndex.YAW.ordinal()].getValue();
    }
    public void setYaw(double yaw){
        pose[PoseIndex.YAW.ordinal()].setValue(yaw);
    }
    
    
    public void updatePosition(double[] pos){
        pose[PoseIndex.X.ordinal()].setValue(pos[0]);
        pose[PoseIndex.Y.ordinal()].setValue(pos[1]);
        pose[PoseIndex.Z.ordinal()].setValue(pos[2]);
    }
    
    public void updateWithString(String s){
        s = s.replace("[", "");
        s = s.replace("]", "");
        String[] poseInfo = s.split(" ");
        if(poseInfo[0].equals("")){
            poseInfo = new String[0];
        }
        
        for(int i = 0; i < 6; i++){
            if(poseInfo.length <= i){
                pose[i].setValue(0.0);
            } else {
                pose[i].setValue(Double.parseDouble(poseInfo[i].trim()));
            }
        }         
    }
    
    public void updateWithArray(double[] poseInfo){
        for(int i = 0; i < 6; i++){
            if(poseInfo.length <= i){
                pose[i].setValue(0.0);
            } else {
                pose[i].setValue(poseInfo[i]);
            }
        }      
    }
    
    @Override
    public String toString(){
        return String.format("[%f %f %f %f %f %f]", getX(), getY(), getZ(), getRoll(), getPitch(), getYaw());
    }
    
    public boolean equals(double[] poseInfo){
        for(int i = 0; i < 6; i++){
            if(poseInfo.length <= i){
                return true;
            } 
            else if (pose[i].getValue() != poseInfo[i])
            {
                return false;
            }
        }
        return true;
    }

    public boolean equals(String s){
        String n;
        n = s.replace("[", "");
        n = n.replace("]", "");
        String[] poseInfo = n.split(" ");
        
        if (poseInfo[0].equals("")){
            return false;
        }
        
        for(int i = 0; i < 6; i++){
            if(poseInfo.length <= i){
                return true;
            } else if (pose[i].getValue() != Double.parseDouble(poseInfo[i].trim()))
            {
                return false;
            }
        }
        return true;
    }
    
    /******************************************************
     * Methods for dealing with working memory
     ******************************************************/

    private Identifier poseID; 
    private boolean added = false;
    
    public boolean isAdded(){
    	return added;
    }
    
    public void addToWM(Identifier parentId){
    	if(added){
    		removeFromWM();
    	}
        // Create the pose on the input link
        poseID = parentId.CreateIdWME("pose");
        for(int i = 0; i < 6; i++){
        	pose[i].addToWM(poseID);
        }
        added = true;
    }
    
    public void updateWM(){
    	if(!added){
    		return;
    	}
        // Update the pose on the input link
        for(int i = 0; i < 6; i++){
        	pose[i].updateWM();
        }	
    }
    
    public void removeFromWM(){
    	if(!added){
    		return;
    	}
        // Remove the pose from the input link
        for(int i = 0; i < 6; i++){
        	pose[i].removeFromWM();
        }	
    	poseID.DestroyWME();
    	poseID = null;
    	added = false;
    }
}
