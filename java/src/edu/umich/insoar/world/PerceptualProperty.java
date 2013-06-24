package edu.umich.insoar.world;

import java.util.*;
import probcog.lcmtypes.*;
import sml.*;

/**
 * A category for each object, which contains several possible labels and their confidences
 * 
 * @author mininger
 * 
 */
public class PerceptualProperty implements IInputLinkElement
{   
	protected static HashMap<Integer, String> categoryNames = null;
	public static String getCategoryName(Integer categoryID){
		if(categoryNames == null){
			categoryNames = new HashMap<Integer, String>();
			categoryNames.put(category_t.CAT_COLOR, "color");
			categoryNames.put(category_t.CAT_SHAPE, "shape");
			categoryNames.put(category_t.CAT_SIZE, "size");
			categoryNames.put(category_t.CAT_LOCATION, "name");
		}
		return categoryNames.get(categoryID);
	}
	
	public static Integer getCategoryID(String categoryName){
		if(categoryName.equals("color")){
			return category_t.CAT_COLOR;
		} else if(categoryName.equals("shape")){
			return category_t.CAT_SHAPE;
		} else if(categoryName.equals("size")){
			return category_t.CAT_SIZE;
		} else if(categoryName.equals("name")){
			return category_t.CAT_LOCATION;
		} else {
			return null;
		}
	}
	
	public static String getCategoryType(String categoryName){
		if(categoryName.equals("weight")){
			return "measurable-prop";
		} else {
			return "visual-prop";
		}
	}
    
    // Root identifier for the category
    protected Identifier propertyID;
    
    // Name of the category
    protected Integer categoryType;
    
    // String WME for the identifier
    protected StringElement nameWME;
    
    // Labels and confidences
    protected HashMap<String, Double> labels;
    
    protected HashMap<String, FloatElement> labelWMEs;
    
    protected Identifier featuresID;
    
    protected ArrayList<Double> features;
    
    protected ArrayList<FloatElement> featureWMEs;

    public PerceptualProperty(categorized_data_t category){
    	propertyID = null;
    	nameWME = null;
    	categoryType = category.cat.cat;
    	labels = new HashMap<String, Double>();
    	labelWMEs = new HashMap<String, FloatElement>();
    	featuresID = null;
    	features = new ArrayList<Double>();
    	featureWMEs = new ArrayList<FloatElement>();
    	updateCategoryInfo(category);
    }
    
    public Integer getType(){
    	return categoryType;
    }
    
    // Accessors
    public String getName(){
        return getCategoryName(categoryType);
    }
    
    public HashMap<String, Double> getLabels(){
    	return labels;
    }

    // Mutators

    @Override
    public synchronized void updateInputLink(Identifier parentIdentifier)
    {
    	if(propertyID == null){
    		propertyID = parentIdentifier.CreateIdWME(getCategoryType(getName()));
    		nameWME = propertyID.CreateStringWME("category", getName());
    		if(features.size() > 0){
        		featuresID = propertyID.CreateIdWME("features");
        		for(int i = 0; i < features.size(); i++){
        			Identifier featureID = featuresID.CreateIdWME("feature");
        			featureID.CreateIntWME("index", i);
        			FloatElement featureWME = featureID.CreateFloatWME("value", features.get(i));
        			featureWMEs.add(featureWME);
        		}
    		}
    		
    	}
    	
    	for(int i = 0; i < features.size(); i++){
    		if(featureWMEs.get(i).GetValue() != features.get(i)){
    			featureWMEs.get(i).Update(features.get(i));
    		}
    	}
    	
    	Set<String> labelsToDestroy = new HashSet<String>();
    	for(String label : labelWMEs.keySet()){
    		labelsToDestroy.add(label);
    	}
    	
    	for(Map.Entry<String, Double> label : labels.entrySet()){
    		if(labelsToDestroy.contains(label.getKey())){
    			// That WME already exists, update it
    			labelsToDestroy.remove(label.getKey());
    			FloatElement labelWME = labelWMEs.get(label.getKey());
    			if(labelWME.GetValue() != label.getValue()){
    				labelWME.Update(label.getValue());
    			}
    		} else {
    			labelWMEs.put(label.getKey(), propertyID.CreateFloatWME(label.getKey(), label.getValue()));
    		}
    	}
    	
    	for(String label : labelsToDestroy){
    		labelWMEs.get(label).DestroyWME();
    		labelWMEs.remove(label);
    	}
    }

    @Override
    public synchronized void destroy()
    {
    	if(propertyID != null){
    		for(Map.Entry<String, FloatElement> wme : labelWMEs.entrySet()){
    			//wme.getValue().DestroyWME();
    		}
    		labelWMEs.clear();
    		//nameWME.DestroyWME();
    		//nameWME = null;
    		propertyID.DestroyWME();
    		propertyID = null;
    		//featuresID.DestroyWME();
    		//featuresID = null;
    	}
    }
    
    public synchronized void updateCategoryInfo(categorized_data_t category){
    	if(category.cat.cat != categoryType){
    		return;
    	}
    	Set<String> labelsToRemove = new HashSet<String>();
    	for(String label : labels.keySet()){
    		labelsToRemove.add(label);
    	}
    	for(int i = 0; i < category.len; i++){
    		if(labelsToRemove.contains(category.label[i].toLowerCase())){
    			labelsToRemove.remove(category.label[i].toLowerCase());
    		}
			labels.put(category.label[i].toLowerCase(), category.confidence[i]);
			// AM: only consider the first one
			break;
    	}
    	for(String label : labelsToRemove){
    		labels.remove(label);
    	}
    }
}
