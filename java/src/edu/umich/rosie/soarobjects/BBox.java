package edu.umich.rosie.soarobjects;

import edu.umich.rosie.soar.FloatWME;
import edu.umich.rosie.soar.ISoarObject;
import sml.FloatElement;
import sml.Identifier;

public class BBox implements ISoarObject
{
	private static String[] wmeStrings = {"x", "y", "z"};
	private static String[] typeStrings = {"min", "max"};
    private enum ElementIndex{X, Y, Z};
    private enum TypeIndex{MIN, MAX};
    private FloatWME[][] bounds;

    public BBox(){     
    	initBBox();
    }
    
    public BBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ){     
    	initBBox();
    	bounds[TypeIndex.MIN.ordinal()][ElementIndex.X.ordinal()].setValue(minX);
    	bounds[TypeIndex.MIN.ordinal()][ElementIndex.Y.ordinal()].setValue(minY);
    	bounds[TypeIndex.MIN.ordinal()][ElementIndex.Z.ordinal()].setValue(minZ);
    	bounds[TypeIndex.MAX.ordinal()][ElementIndex.X.ordinal()].setValue(maxX);
    	bounds[TypeIndex.MAX.ordinal()][ElementIndex.Y.ordinal()].setValue(maxY);
    	bounds[TypeIndex.MAX.ordinal()][ElementIndex.Z.ordinal()].setValue(maxZ);
    }
    
    public BBox(double[][] boundsValues){     
    	initBBox();
        updateWithArray(boundsValues);
    }
    
    public BBox(String s){      
    	initBBox();
        updateWithString(s);            
    }
    
    private void initBBox(){
    	bounds = new FloatWME[2][3];
    	for(int i = 0; i < 2; i++){
    		for(int j = 0; j < 3; j++){
    			bounds[i][j] = new FloatWME(wmeStrings[j], 0.0);
    		}
    	}
    }
    
    public double getMinX(){
        return bounds[TypeIndex.MIN.ordinal()][ElementIndex.X.ordinal()].getValue();
    }
    public void setMinX(double minX){
    	bounds[TypeIndex.MIN.ordinal()][ElementIndex.X.ordinal()].setValue(minX);
    }
    

    public double getMinY(){
        return bounds[TypeIndex.MIN.ordinal()][ElementIndex.Y.ordinal()].getValue();
    }
    public void setMinY(double minY){
    	bounds[TypeIndex.MIN.ordinal()][ElementIndex.Y.ordinal()].setValue(minY);
    }
    

    public double getMinZ(){
        return bounds[TypeIndex.MIN.ordinal()][ElementIndex.Z.ordinal()].getValue();
    }
    public void setMinZ(double minZ){
    	bounds[TypeIndex.MIN.ordinal()][ElementIndex.Z.ordinal()].setValue(minZ);
    }
    

    public double getMaxX(){
        return bounds[TypeIndex.MAX.ordinal()][ElementIndex.X.ordinal()].getValue();
    }
    public void setMaxX(double maxX){
    	bounds[TypeIndex.MAX.ordinal()][ElementIndex.X.ordinal()].setValue(maxX);
    }
    

    public double getMaxY(){
        return bounds[TypeIndex.MAX.ordinal()][ElementIndex.Y.ordinal()].getValue();
    }
    public void setMaxY(double maxY){
    	bounds[TypeIndex.MAX.ordinal()][ElementIndex.Y.ordinal()].setValue(maxY);
    }
    

    public double getMaxZ(){
        return bounds[TypeIndex.MAX.ordinal()][ElementIndex.Z.ordinal()].getValue();
    }
    public void setMaxZ(double maxZ){
    	bounds[TypeIndex.MAX.ordinal()][ElementIndex.Z.ordinal()].setValue(maxZ);
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
                bounds[i/3][i%3].setValue(0.0);
            } else {
            	bounds[i/3][i%3].setValue(Double.parseDouble(poseInfo[i].trim()));
            }
        }         
    }
    
    public void updateWithArray(double[][] boundsInfo){
    	for(int i = 0; i < 2; i++){
    		for(int j = 0; j < 3; j++){
    			if(boundsInfo.length <= i || boundsInfo[i].length <= j){
    				bounds[i][j].setValue(0.0);
    			} else {
    				bounds[i][j].setValue(boundsInfo[i][j]);
    			}
    		}
    	}    
    }
    
    public String getFullPoints()
    {
        return String.format("%f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f %f", 
                getMinX(), getMinY(), getMinZ(),
                getMinX(), getMinY(), getMaxZ(),
                getMinX(), getMaxY(), getMinZ(),
                getMinX(), getMaxY(), getMaxZ(),
                getMaxX(), getMinY(), getMinZ(),
                getMaxX(), getMinY(), getMaxZ(),
                getMaxX(), getMaxY(), getMinZ(),
                getMaxX(), getMaxY(), getMaxZ());
    }
    
    @Override
    public String toString(){
        return String.format("[%f %f %f %f %f %f]", getMinX(), getMinY(), getMinZ(), getMaxX(), getMaxY(), getMaxZ());
    }
    
    
    /******************************************************************
     * Methods for Modifying Working Memory
     *****************************************************************/
    private Identifier[] typeIDs = null;
    private Identifier bboxID = null; 
    private boolean added = false;
    
    public boolean isAdded(){
    	return added;
    }

    public void addToWM(Identifier parentID){
    	if(added){
    		removeFromWM();
    	}
        bboxID = parentID.CreateIdWME("bbox");
        typeIDs = new Identifier[2];
        for(int i = 0; i < 2; i++){
        	typeIDs[i] = bboxID.CreateIdWME(typeStrings[i]);
        	for(int j = 0; j < 3; j++){
        		bounds[i][j].addToWM(typeIDs[i]);
        	}
        }
        added = true;
    }
    
    public void updateWM(){
    	if(!added){
    		return;
    	}
        // Update the pose on the input link
    	for(int i = 0; i < 2; i++){
    		for(int j = 0; j < 3; j++){
    			bounds[i][j].updateWM();
    		}
    	}
    }
    
    public void removeFromWM(){
    	if(!added){
    		return;
    	}
        // Remove the pose from the input link
    	for(int i = 0; i < 2; i++){
    		for(int j = 0; j < 3; j++){
    			bounds[i][j].removeFromWM();
    		}
    	}
    	bboxID.DestroyWME();
    	bboxID = null;
    	typeIDs = null;
    	added = false;
    }
}
