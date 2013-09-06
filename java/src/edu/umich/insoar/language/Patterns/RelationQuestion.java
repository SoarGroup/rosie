package edu.umich.insoar.language.Patterns;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sml.Identifier;
import edu.umich.insoar.language.LinguisticEntity;
import edu.umich.insoar.world.WMUtil;


// will parse only positive predicates for now
public class RelationQuestion extends LinguisticEntity{
    public static String TYPE = "RelationQuestion";
	private String preposition;
	private LingObject object1;
	private LingObject object2;
	
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
	

	@Override
	public void extractLinguisticComponents(String string, Map tagsToWords) {
		//get preposition 
		Pattern p = Pattern.compile("PP\\d*");
		Matcher m = p.matcher(string);
		if(m.find()){
			preposition = tagsToWords.get(m.group()).toString();
		//	System.out.println(preposition);
		}
		
		//get object1
		p = Pattern.compile("OBJ\\d*");
		m = p.matcher(string);
		if(m.find()){
			object1 = (LingObject) tagsToWords.get(m.group());
		}
		if(m.find()){
			object2 = (LingObject) tagsToWords.get(m.group());
		}
	}


	@Override
	public void translateToSoarSpeak(Identifier id, String connectingString) {
		Identifier messageId = id;
		messageId.CreateStringWME("type", "object-is-question");
		Identifier infoId = messageId.CreateIdWME("information");
		
		object1.translateToSoarSpeak(infoId, "object");
		
		Identifier relId = infoId.CreateIdWME("relation");
		relId.CreateStringWME("word", preposition);
		relId.CreateStringWME("questioned", "true");
		// P1
		Identifier p1Id = relId.CreateIdWME("p1");
		p1Id.CreateSharedIdWME("object", object1.getRoot());
		// P2
		Identifier objId2 = relId.CreateIdWME("p2");
		object2.translateToSoarSpeak(objId2, "object");
	}
	
    public static RelationQuestion createFromSoarSpeak(Identifier id, String name)
    {
        if(id == null){
            return null;
        }
        Identifier relationId = WMUtil.getIdentifierOfAttribute(id, name);
        if(relationId == null){
            return null;
        }
        RelationQuestion objectRelation = new RelationQuestion();
        objectRelation.preposition = WMUtil.getValueOfAttribute(relationId, "word");
        objectRelation.object1 = LingObject.createFromSoarSpeak(relationId, "object1");
        objectRelation.object2 = LingObject.createFromSoarSpeak(relationId, "object2");
        
        return objectRelation;
    }

}
