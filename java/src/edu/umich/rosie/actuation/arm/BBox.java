package edu.umich.rosie.actuation.arm;

import sml.FloatElement;
import sml.Identifier;

public class BBox implements IInputLinkElement
{
	private static String[] wmeStrings = {"x", "y", "z"};
	private static String[] typeStrings = {"min", "max"};
    private enum ElementIndex{X, Y, Z};
    private enum TypeIndex{MIN, MAX};
    private double[][] bounds;
    private Identifier[] typeIDs;
    private FloatElement[][] wmes;
    private Identifier bboxID;
    
    public BBox(){     
        initBBox();
    	for(int i = 0; i < 2; i++){
    		for(int j = 0; j < 3; j++){
    			bounds[i][j] = 0;
    		}
    	}
    }
    
    public BBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ){     
        initBBox();
    	bounds[TypeIndex.MIN.ordinal()][ElementIndex.X.ordinal()] = minX;
    	bounds[TypeIndex.MIN.ordinal()][ElementIndex.Y.ordinal()] = minY;
    	bounds[TypeIndex.MIN.ordinal()][ElementIndex.Z.ordinal()] = minZ;
    	bounds[TypeIndex.MAX.ordinal()][ElementIndex.X.ordinal()] = maxX;
    	bounds[TypeIndex.MAX.ordinal()][ElementIndex.Y.ordinal()] = maxY;
    	bounds[TypeIndex.MAX.ordinal()][ElementIndex.Z.ordinal()] = maxZ;
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
    	wmes = null;
    	typeIDs = null;
    	bboxID = null;
    	bounds = new double[2][3];
    }
    
    public double getMinX(){
        return bounds[TypeIndex.MIN.ordinal()][ElementIndex.X.ordinal()];
    }
    public void setMinX(double minX){
    	bounds[TypeIndex.MIN.ordinal()][ElementIndex.X.ordinal()] = minX;
    }
    

    public double getMinY(){
        return bounds[TypeIndex.MIN.ordinal()][ElementIndex.Y.ordinal()];
    }
    public void setMinY(double minY){
    	bounds[TypeIndex.MIN.ordinal()][ElementIndex.Y.ordinal()] = minY;
    }
    

    public double getMinZ(){
        return bounds[TypeIndex.MIN.ordinal()][ElementIndex.Z.ordinal()];
    }
    public void setMinZ(double minZ){
    	bounds[TypeIndex.MIN.ordinal()][ElementIndex.Z.ordinal()] = minZ;
    }
    

    public double getMaxX(){
        return bounds[TypeIndex.MAX.ordinal()][ElementIndex.X.ordinal()];
    }
    public void setMaxX(double MaxX){
    	bounds[TypeIndex.MAX.ordinal()][ElementIndex.X.ordinal()] = MaxX;
    }
    

    public double getMaxY(){
        return bounds[TypeIndex.MAX.ordinal()][ElementIndex.Y.ordinal()];
    }
    public void setMaxY(double MaxY){
    	bounds[TypeIndex.MAX.ordinal()][ElementIndex.Y.ordinal()] = MaxY;
    }
    

    public double getMaxZ(){
        return bounds[TypeIndex.MAX.ordinal()][ElementIndex.Z.ordinal()];
    }
    public void setMaxZ(double MaxZ){
    	bounds[TypeIndex.MAX.ordinal()][ElementIndex.Z.ordinal()] = MaxZ;
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
                bounds[i/3][i%3] = 0;
            } else {
            	bounds[i/3][i%3] = Double.parseDouble(poseInfo[i].trim());
            }
        }         
    }
    
    public void updateWithArray(double[][] boundsInfo){
    	for(int i = 0; i < 2; i++){
    		for(int j = 0; j < 3; j++){
    			if(boundsInfo.length <= i || boundsInfo[i].length <= j){
    				bounds[i][j] = 0;
    			} else {
    				bounds[i][j] = boundsInfo[i][j];
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
    

    // update the input-link appropriately
    @Override
    public synchronized void updateInputLink(Identifier parentIdentifier){
        if(bboxID == null){
            // Create the pose on the input link
            bboxID = parentIdentifier.CreateIdWME("bbox");
            typeIDs = new Identifier[2];
            wmes = new FloatElement[2][3];
            for(int i = 0; i < 2; i++){
            	typeIDs[i] = bboxID.CreateIdWME(typeStrings[i]);
            	for(int j = 0; j < 3; j++){
            		wmes[i][j] = typeIDs[i].CreateFloatWME(wmeStrings[j], bounds[i][j]);
            	}
            }
        } else {
            // Update the pose on the input link
        	for(int i = 0; i < 2; i++){
        		for(int j = 0; j < 3; j++){
        			if(wmes[i][j].GetValue() != bounds[i][j]){
                        wmes[i][j].Update(bounds[i][j]);
                    }
        		}
        	}
        }
    }
    
    @Override
    public synchronized void onInitSoar(){
    	wmes = null;
    	typeIDs = null;
    	bboxID = null;
    }
    
    // remove the object from the input-link
    @Override
    public synchronized void destroy(){
        if(wmes != null){
        	for(int i = 0; i < 2; i++){
        		for(int j = 0; j < 3; j++){
        			wmes[i][j].DestroyWME();
        		}
        		typeIDs[i].DestroyWME();
        	}
        	bboxID.DestroyWME();
        	
        	wmes = null;
        	typeIDs = null;
        	bboxID = null;
        }
    }
}
