package edu.umich.insoar.language.Patterns;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sml.Identifier;
import edu.umich.insoar.language.LinguisticEntity;
import edu.umich.insoar.world.WMUtil;

public class VerbCommand extends LinguisticEntity{
    public static String TYPE = "VerbCommand";
	private String verb = null;
	private LingObject directObject = null;
	private String preposition = null;
	private LingObject secondObject = null;
	

    public String getVerb()
    {
        return verb;
    }


    public LingObject getDirectObject()
    {
        return directObject;
    }

    public String getPreposition()
    {
        return preposition;
    }

    public LingObject getSecondObject()
    {
        return secondObject;
    }
	
	public void translateToSoarSpeak(Identifier id, String connectingString){
		Identifier messageId = id;
		messageId.CreateStringWME("type", "verb-command");
		Identifier infoId = messageId.CreateIdWME("information");
		Identifier verbId = infoId.CreateIdWME("verb");
		verbId.CreateStringWME("word", verb);
		if(directObject != null){
			Identifier firstObjectId = verbId.CreateIdWME("direct-object");
			directObject.translateToSoarSpeak(firstObjectId,"object");
		}
		if(preposition != null){
			Identifier prepId = verbId.CreateIdWME("preposition");
			prepId.CreateStringWME("word", preposition);
			secondObject.translateToSoarSpeak(prepId,"object");
		}
	}

	public void extractLinguisticComponents(String string, Map tagsToWords) {
		Pattern p = Pattern.compile("VB\\d*");
		Matcher m = p.matcher(string);
		if(m.find()){
			verb = tagsToWords.get(m.group()).toString();
		}
		
		
		Pattern pp = Pattern.compile("PP\\d* OBJ\\d*");
		Matcher mp = pp.matcher(string);
		if (mp.find()){
			StringBuffer sb = new StringBuffer();
			String ppstring = mp.group().toString();
			p = Pattern.compile("PP\\d*");
			m = p.matcher(ppstring);
			if(m.find()){
				preposition = tagsToWords.get(m.group()).toString();
			}
			p = Pattern.compile("OBJ\\d*");
			m = p.matcher(ppstring);
			if(m.find()){
				secondObject = (LingObject) tagsToWords.get(m.group());
			}
			mp.appendReplacement(sb,"PPH");
			mp.appendTail(sb);
			string = sb.toString();
		}
		
		p = Pattern.compile("OBJ\\d*");
		m = p.matcher(string);
		if(m.find()){
			directObject = (LingObject) tagsToWords.get(m.group());
		}
	
	}
	
	public static VerbCommand createFromSoarSpeak(Identifier id, String name){
        if(id == null){
            return null;
        }
        Identifier verbId = WMUtil.getIdentifierOfAttribute(id, name);
        if(verbId == null){
            return null;
        }
	    VerbCommand verbCommand = new VerbCommand();
	    verbCommand.verb = WMUtil.getValueOfAttribute(verbId, "word");
        verbCommand.directObject = LingObject.createFromSoarSpeak(verbId, "direct-object");
        Identifier prepositionId = WMUtil.getIdentifierOfAttribute(verbId, "preposition");
        if(prepositionId != null){
            verbCommand.preposition = WMUtil.getValueOfAttribute(prepositionId, "word");
            verbCommand.secondObject = LingObject.createFromSoarSpeak(prepositionId, "object");
        }
	    return verbCommand;
	}
}
