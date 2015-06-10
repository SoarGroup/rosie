package edu.umich.rosie.soar;


public class SVSCommands {
    
    public static String add(String objID, double[] pos, double[] rot, double[] size){
    	return String.format("add %s world v %s p %s r %s s %s\n", objID, bboxVertices(), posToStr(pos), rotToStr(rot), sizeToStr(size));
    }
    
    public static String add(String objID){
    	return String.format("add %s world v %s\n", objID, bboxVertices());
    }
    
    public static String changePos(String objID, double[] pos){
    	return String.format("change %s p %s\n", objID, posToStr(pos));
    }
    
    public static String changeRot(String objID, double[] rot){
    	return String.format("change %s r %s\n", objID, rotToStr(rot));
    }
    
    public static String changeSize(String objID, double[] size){
    	return String.format("change %s s %s\n", objID, rotToStr(size));
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
    
    public static String bboxVertices(){
    	StringBuilder sb = new StringBuilder();
    	
        for(int i = -1; i <= 1; i += 2){
        	for(int j = -1; j <= 1; j += 2){
        		for(int k = -1; k <= 1; k += 2){
        			if(i > 0 || j > 0 || k > 0){
        				sb.append(" ");
        			}
        			sb.append(String.format("%f %f %f", i * .5, j * .5, k * .5));
        		}
        	}
        }
        return sb.toString();
    }
    
    public static String posToStr(double[] pos){
    	return String.format("%f %f %f", pos[0], pos[1], pos[2]);
    }    
    
    public static String rotToStr(double[] rot){
    	return String.format("%f %f %f", rot[0], rot[1], rot[2]);
    }   
    
    public static String sizeToStr(double[] size){
    	return String.format("%f %f %f", size[0], size[1], size[2]);
    }   
}
