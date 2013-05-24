package edu.umich.insoar.language.Patterns;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.umich.insoar.language.LinguisticEntity;
import edu.umich.insoar.world.WMUtil;

import sml.Agent;
import sml.Identifier;

public class BareAttributeResponse extends LinguisticEntity{
    public static String TYPE = "BareAttributeResponse";
    private String category = null;
    
    public String getCategory(){
        return category;
    }
    
    public void translateToSoarSpeak(Identifier messageId, String connectingString){
        messageId.CreateStringWME("type", "bare-attribute-response");
        messageId.CreateStringWME("originator", "mentor");
        Identifier fieldsId = messageId.CreateIdWME("fields");
        fieldsId.CreateStringWME("category", category);
    }

    public void extractLinguisticComponents(String string, Map tagsToWords) {
        Pattern p = Pattern.compile("AT\\d*");
        Matcher m = p.matcher(string);
        if(m.find()){
        	category = (String)tagsToWords.get(m.group());
        }
    }
}
