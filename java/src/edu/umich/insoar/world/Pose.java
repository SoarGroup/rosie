package edu.umich.insoar.world;

import sml.FloatElement;
import sml.Identifier;

public class Pose implements IInputLinkElement
{
    private static String[] poseStrings = {"x", "y", "z", "roll", "pitch", "yaw"};
    private enum PoseIndex{X, Y, Z, ROLL, PITCH, YAW};
    private double[] pose;
    private FloatElement[] wmes;
    private Identifier poseID;
    
    public Pose(){
        wmes = null;
        poseID = null;
        pose = new double[6];
        for(int i = 0; i < 6; i++){
            pose[i] = 0;
        }
    }
    
    public Pose(double x, double y, double z, double roll, double pitch, double yaw){
        wmes = null;
        poseID = null;
        pose = new double[6];
        pose[PoseIndex.X.ordinal()] = x;
        pose[PoseIndex.Y.ordinal()] = y;
        pose[PoseIndex.Z.ordinal()] = z;
        pose[PoseIndex.ROLL.ordinal()] = roll;
        pose[PoseIndex.PITCH.ordinal()] = pitch;
        pose[PoseIndex.YAW.ordinal()] = yaw;
    }
    
    public Pose(double[] poseValues){
        wmes = null;
        poseID = null;
        pose = new double[6];
        updateWithArray(poseValues);
    }
    
    public Pose(String s){      
        wmes = null;  
        poseID = null;
        pose = new double[6];
        
        updateWithString(s);            
    }
    
    public double getX(){
        return pose[PoseIndex.X.ordinal()];
    }
    public void setX(double x){
        pose[PoseIndex.X.ordinal()] = x;
    }
    

    public double getY(){
        return pose[PoseIndex.Y.ordinal()];
    }
    public void setY(double y){
        pose[PoseIndex.Y.ordinal()] = y;
    }
    

    public double getZ(){
        return pose[PoseIndex.Z.ordinal()];
    }
    public void setZ(double z){
        pose[PoseIndex.Z.ordinal()] = z;
    }
    

    public double getRoll(){
        return pose[PoseIndex.ROLL.ordinal()];
    }
    public void setRoll(double roll){
        pose[PoseIndex.ROLL.ordinal()] = roll;
    }
    

    public double getPitch(){
        return pose[PoseIndex.PITCH.ordinal()];
    }
    public void setPitch(double pitch){
        pose[PoseIndex.PITCH.ordinal()] = pitch;
    }
    

    public double getYaw(){
        return pose[PoseIndex.YAW.ordinal()];
    }
    public void setYaw(double yaw){
        pose[PoseIndex.YAW.ordinal()] = yaw;
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
                pose[i] = 0;
            } else {
                pose[i] = Double.parseDouble(poseInfo[i].trim());
            }
        }         
    }
    
    public void updateWithArray(double[] poseInfo){
        for(int i = 0; i < 6; i++){
            if(poseInfo.length <= i){
                pose[i] = 0;
            } else {
                pose[i] = poseInfo[i];
            }
        }      
    }
    
    @Override
    public String toString(){
        return String.format("[%f %f %f %f %f %f]", getX(), getY(), getZ(), getRoll(), getPitch(), getYaw());
    }
    

    // update the input-link appropriately
    @Override
    public synchronized void updateInputLink(Identifier parentIdentifier){
        if(poseID == null){
            // Create the pose on the input link
            poseID = parentIdentifier.CreateIdWME("pose");
            wmes = new FloatElement[6];
            for(int i = 0; i < 6; i++){
                wmes[i] = poseID.CreateFloatWME(poseStrings[i], pose[i]);
            }
        } else {
            // Update the pose on the input link
            for(int i = 0; i < 6; i++){
                if(wmes[i].GetValue() != pose[i]){
                    wmes[i].Update(pose[i]);
                }
            }
        }
    }
    
    // remove the object from the input-link
    @Override
    public synchronized void destroy(){
        if(wmes != null){
            for(int i = 0; i < 6; i++){
                wmes[i].DestroyWME();
            }
            wmes = null;
            
            poseID.DestroyWME();
            poseID = null;
        }
    }
    
    
    public boolean equals(double[] poseInfo){
        for(int i = 0; i < 6; i++){
            if(poseInfo.length <= i){
                return true;
            } 
            else if (pose[i] != poseInfo[i])
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
            } else if (pose[i] != Double.parseDouble(poseInfo[i].trim()))
            {
                return false;
            }
        }
        return true;
    }
}
