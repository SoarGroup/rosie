package edu.umich.rosie.soar;

public class SVSCommands {
	// double[] pos = double[]{ x, y, z }
    public static String posToStr(double[] pos){
    	return String.format("%f %f %f", pos[0], pos[1], pos[2]);
    }    
    
    // double[] rot = double[]{ roll, pitch, yaw }
    public static String rotToStr(double[] rot){
    	return String.format("%f %f %f", rot[0], rot[1], rot[2]);
    }   
    
    // double[] scale = double[]{ scale_x, scale_y, scale_z }
    public static String scaleToStr(double[] scale){
    	return String.format("%f %f %f", scale[0], scale[1], scale[2]);
    }   
	
    // Creates a set of 8 vertices forming a bounding box
    //   of unit size centered at the origin
    public static String bboxVertices(){
    	return "0.5 0.5 0.5 0.5 0.5 -0.5 0.5 -0.5 0.5 0.5 -0.5 -0.5 -0.5 0.5 0.5 -0.5 0.5 -0.5 -0.5 -0.5 0.5 -0.5 -0.5 -0.5";
    }
    
    public static String addBox(String objID){
    	return String.format("add %s world v %s\n", objID, bboxVertices());
    }

    public static String addBox(String objID, double[] pos){
    	return String.format("add %s world v %s p %s\n", objID, bboxVertices(), posToStr(pos));
    }

    public static String addBox(String objID, double[] pos, double[] scale){
    	return String.format("add %s world v %s p %s s %s\n", objID, bboxVertices(), posToStr(pos), scaleToStr(scale));
    }

    public static String addBox(String objID, double[] pos, double[] rot, double[] scale){
    	return String.format("add %s world v %s p %s r %s s %s\n", objID, bboxVertices(), posToStr(pos), rotToStr(rot), scaleToStr(scale));
    }
    
    public static String changePos(String objID, double[] pos){
    	return String.format("change %s p %s\n", objID, posToStr(pos));
    }
    
    public static String changeRot(String objID, double[] rot){
    	return String.format("change %s r %s\n", objID, rotToStr(rot));
    }
    
    public static String changeScale(String objID, double[] scale){
    	return String.format("change %s s %s\n", objID, scaleToStr(scale));
    }
    
    public static String delete(String objID){
    	return String.format("delete %s\n",objID);
    }
    
    public static String addTag(String objID, String tagName, String tagValue){
    	return String.format("tag add %s %s %s\n", objID, tagName, tagValue);
    }
    
    public static String changeTag(String objID, String tagName, String tagValue){
    	return String.format("tag change %s %s %s\n", objID, tagName, tagValue);
    }
    
    public static String deleteTag(String objID, String tagName){
    	return String.format("tag delete %s %s\n", objID, tagName);
    }
}
