package edu.umich.insoar.language;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sml.Identifier;
import sml.WMElement;
import edu.umich.insoar.world.WMUtil;


// will parse only positive predicates for now
public class ObjectRelation {
    public static String TYPE = "ObjectRelation";
	private String preposition;
	private LingObject object1;
	private LingObject object2;
	private LingObject object3;
	public int order;
	
	public String getPreposition(){
	    return preposition;
	}
	
    public LingObject getObject1()
    {
        return object1;
    }
    
    public LingObject getObject2()
    {
        return object2;
    }
	
    public static ObjectRelation createFromSoarSpeak(Identifier id, String name)
    {
        if(id == null){
            return null;
        }
        Identifier relationId = WMUtil.getIdentifierOfAttribute(id, name);
        return ObjectRelation.createFromSoarSpeak(relationId);
    }
    
    public static ObjectRelation createFromSoarSpeak(Identifier id)
    {
        if(id == null){
            return null;
        }

        ObjectRelation objectRelation = new ObjectRelation();
        String prep = WMUtil.getValueOfAttribute(id, "word");
        objectRelation.preposition = prep.replaceAll("-", " ");
        objectRelation.object1 = LingObject.createFromSoarSpeak(id, "object1");
        objectRelation.object2 = LingObject.createFromSoarSpeak(id, "object2");
        objectRelation.object3 = null;
        //for specific specifiers a bit hackish
        Identifier a,b,c;
        if ((a = WMUtil.getIdentifierOfAttribute(id, "a")) != null)
        {
        	objectRelation.order = Integer.parseInt(WMUtil.getValueOfAttribute(id, "order"));
        	objectRelation.object1.setDeterminer(WMUtil.getValueOfAttribute(a, "specifier"));
        }
        if ((b = WMUtil.getIdentifierOfAttribute(id, "b")) != null)
        {
        	objectRelation.object2.setDeterminer(WMUtil.getValueOfAttribute(b, "specifier"));
        }
        if ((c = WMUtil.getIdentifierOfAttribute(id, "c")) != null)
        {
        	objectRelation.object3 = LingObject.createFromSoarSpeak(id, "object3");
        	objectRelation.object3.setDeterminer(WMUtil.getValueOfAttribute(c, "specifier"));
        }
        return objectRelation;
    }
    
	
    public static List<ObjectRelation> createAllFromSoarSpeak(Identifier id, String name){
        List<ObjectRelation> objectRelations = new ArrayList<ObjectRelation>();
        for(int index = 0; index < id.GetNumberChildren(); index++){
            WMElement wme = id.GetChild(index);
            if(wme.GetAttribute().equals(name)){
            	objectRelations.add(ObjectRelation.createFromSoarSpeak(wme.ConvertToIdentifier()));
            }
        }
        //sort
        Collections.sort(objectRelations, new ObjectRelationCompare());
        return objectRelations;
    }

    
    @Override
    public String toString(){   
    	if (object3 != null)// 3 arguments
    	{
    		return String.format("%s is %s %s and %s", object1.toString(), preposition, object2.toString(), object3.toString());
    	}
        return String.format("%s is %s %s", object1.toString(), preposition, object2.toString());
    }
}
//comparator class for ObjectRelation
class ObjectRelationCompare implements Comparator<ObjectRelation> {

    @Override
    public int compare(ObjectRelation o1, ObjectRelation o2) {
        // write comparison logic here like below , it's just a sample
        return (o1.order - o2.order);
    }
}
