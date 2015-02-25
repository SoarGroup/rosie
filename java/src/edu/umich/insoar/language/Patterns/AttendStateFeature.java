package edu.umich.insoar.language.Patterns;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sml.Identifier;
import edu.umich.insoar.language.LinguisticEntity;
import edu.umich.insoar.world.WMUtil;


// will parse only positive predicates for now
public class AttendStateFeature extends LinguisticEntity{
    public static String TYPE = "AttendStateFeature";
	private String preposition;
	private LingObject object;
	
	public String getPreposition(){
	    return preposition;
	}
	
    public LingObject getObject()
    {
        return object;
    }
	

	@Override
	public void extractLinguisticComponents(String string, Map tagsToWords) {
		//find either preposition mentioned or object
		preposition = null;
		object = null;
		Pattern p = Pattern.compile("PP\\d*");
		Matcher m = p.matcher(string);
		if(m.find()){
			preposition = tagsToWords.get(m.group()).toString();
			return;
		}

		p = Pattern.compile("OBJ\\d*");
		m = p.matcher(string);
		if(m.find()){
			object = (LingObject) tagsToWords.get(m.group());
		}
	}


	@Override
	public void translateToSoarSpeak(Identifier id, String connectingString) {
		Identifier messageId = id;
		messageId.CreateStringWME("type", "goal-feature");
		Identifier infoId = messageId.CreateIdWME("information");
		infoId.CreateStringWME("word", "attend");
		if (object != null)
		{
			object.translateToSoarSpeak(infoId, "object");
			
		}
		else if (preposition != null)
		{
			infoId.CreateStringWME("relation", preposition);
		}
	}
	
    public static AttendStateFeature createFromSoarSpeak(Identifier id, String name)
    {
        if(id == null){
            return null;
        }
        Identifier featureId = WMUtil.getIdentifierOfAttribute(id, name);
        if(featureId == null){
            return null;
        }
        AttendStateFeature stateFeature = new AttendStateFeature();
        stateFeature.preposition = WMUtil.getValueOfAttribute(featureId, "relation");
        stateFeature.object = LingObject.createFromSoarSpeak(featureId, "object");
        
        return stateFeature;
    }

}
