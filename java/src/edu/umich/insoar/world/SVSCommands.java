package edu.umich.insoar.world;


public class SVSCommands {
    
    public static String add(String objID, double[] pos, double[] rot, double[] size){
    	return String.format("a %s object world v %s p %s r %s s %s\n", objID, bboxVertices(), posToStr(pos), rotToStr(rot), sizeToStr(size));
    }
    
    public static String add(String objID){
    	return String.format("a %s object world v %s\n", objID, bboxVertices());
    }
    
    public static String changePos(String objID, double[] pos){
    	return String.format("c %s p %s\n", objID, posToStr(pos));
    }
    
    public static String changeRot(String objID, double[] rot){
    	return String.format("c %s r %s\n", objID, rotToStr(rot));
    }
    
    public static String changeSize(String objID, double[] size){
    	return String.format("c %s s %s\n", objID, rotToStr(size));
    }
    
    public static String delete(String objID){
    	return String.format("d %s\n",objID);
    }
    
    public static String addProperty(String objID, String propName, String value){
    	return String.format("p %s a %s %s\n", objID, propName, value);
    }
    
    public static String changeProperty(String objID, String propName, String value){
    	return String.format("p %s c %s %s\n", objID, propName, value);
    }
    
    public static String deleteProperty(String objID, String propName){
    	return String.format("p %s d %s\n", objID, propName);
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
