package edu.umich.insoar.language.Patterns;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sml.Identifier;
import edu.umich.insoar.language.LinguisticEntity;

public class BareValueResponse extends LinguisticEntity{
    public static String TYPE = "BareValueResponse";
    private String attribute = null;
    private String value = null;
    
    public String getAttribute(){
        return attribute;
    }
    
    public String getValue(){
        return value; 
    }
    
    public void translateToSoarSpeak(Identifier messageId, String connectingString){
        messageId.CreateStringWME("type", "bare-value-response");
        messageId.CreateStringWME("originator", "mentor");
        Identifier fieldsId = messageId.CreateIdWME("fields");
        fieldsId.CreateStringWME("attribute", attribute);
        fieldsId.CreateStringWME("value", value);
    }

    public void extractLinguisticComponents(String string, Map tagsToWords) {
        Pattern p = Pattern.compile("AT\\d*");
        Matcher m = p.matcher(string);
        if(m.find()){
            attribute = (String)tagsToWords.get(m.group());
        }
        
        p = Pattern.compile("JJ\\d*");
        m = p.matcher(string);
        if(m.find()){
            value = (String)tagsToWords.get(m.group());
        }
    }
}
