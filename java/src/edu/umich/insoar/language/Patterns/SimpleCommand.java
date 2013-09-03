package edu.umich.insoar.language.Patterns;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sml.Identifier;
import edu.umich.insoar.language.LinguisticEntity;

public class SimpleCommand extends LinguisticEntity{
    public static String TYPE = "SimpleCommand";
    private String command = null;
    
    public void translateToSoarSpeak(Identifier messageId, String connectingString){
        messageId.CreateStringWME("type", "single-word-response");
        messageId.CreateStringWME("originator", "mentor");
        Identifier fieldsId = messageId.CreateIdWME("information");
        fieldsId.CreateStringWME("response", command);
    }

    public void extractLinguisticComponents(String string, Map tagsToWords) {        
        Pattern p = Pattern.compile("VB\\d*");
        Matcher m = p.matcher(string);
        if(m.find()){
        	command = (String)tagsToWords.get(m.group());
        }
    }
}
