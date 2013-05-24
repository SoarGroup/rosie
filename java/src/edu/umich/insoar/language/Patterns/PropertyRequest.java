package edu.umich.insoar.language.Patterns;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.umich.insoar.language.LinguisticEntity;
import edu.umich.insoar.world.WMUtil;

import sml.Agent;
import sml.Identifier;

public class PropertyRequest extends LinguisticEntity{
    public static String TYPE = "PropertyRequest";
    private String attribute = null;
    
    public String getAttribute(){
        return attribute;
    }
    
    public void translateToSoarSpeak(Identifier messageId, String connectingString){
        messageId.CreateStringWME("type", "property-question");
        messageId.CreateStringWME("originator", "mentor");
        Identifier fieldsId = messageId.CreateIdWME("information");
        fieldsId.CreateStringWME("word", attribute);
        fieldsId.CreateStringWME("specifier", "this");
        fieldsId.CreateStringWME("question-word", "what");
    }

    public void extractLinguisticComponents(String string, Map tagsToWords) {
        Pattern p = Pattern.compile("AT\\d*");
        Matcher m = p.matcher(string);
        if(m.find()){
            attribute = (String)tagsToWords.get(m.group());
        }
    }
}
