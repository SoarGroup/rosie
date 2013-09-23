package edu.umich.insoar.language.Patterns;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sml.Identifier;
import edu.umich.insoar.language.LinguisticEntity;
import edu.umich.insoar.world.WMUtil;


// will parse only positive predicates for now
public class ObjectState extends LinguisticEntity{
    public static String TYPE = "ObjectState";
	private String attribute;
	private LingObject object;
	
	public String getAttribute(){
	    return attribute;
	}
	
    public LingObject getObject1()
    {
        return object;
    }
    

	@Override
	public void extractLinguisticComponents(String string, Map tagsToWords) {
		//get attribute
		Pattern p = Pattern.compile("AT\\d*");
		Matcher m = p.matcher(string);
		if(m.find()){
			attribute = tagsToWords.get(m.group()).toString();
		//	System.out.println("attribute is " + attribute);
		}
		
		//get object1
		p = Pattern.compile("OBJ\\d*");
		m = p.matcher(string);
		if(m.find()){
			object = (LingObject) tagsToWords.get(m.group());
		//	System.out.println("object is " + object.getNoun());
		}
	}


	@Override
	public void translateToSoarSpeak(Identifier id, String connectingString) {
		Identifier sttId = id.CreateIdWME(connectingString);
		sttId.CreateStringWME("word", attribute);
		Identifier objId = sttId.CreateIdWME("p1");
		object.translateToSoarSpeak(objId, "object");
	}
	
    public static ObjectState createFromSoarSpeak(Identifier id, String name)
    {
        if(id == null){
            return null;
        }
        Identifier stateId = WMUtil.getIdentifierOfAttribute(id, name);
        if(stateId == null){
            return null;
        }
        ObjectState objectRelation = new ObjectState();
        objectRelation.attribute = WMUtil.getValueOfAttribute(stateId, "word");
        objectRelation.object = LingObject.createFromSoarSpeak(stateId, "object1");
        
        return objectRelation;
    }

}
