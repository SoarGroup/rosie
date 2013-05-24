package edu.umich.insoar.language.Patterns;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.umich.insoar.language.LinguisticEntity;
import edu.umich.insoar.world.WMUtil;

import sml.Agent;
import sml.Identifier;

public class DescriptionRequest extends LinguisticEntity{
    public static String TYPE = "DescriptionRequest";
    
    public void translateToSoarSpeak(Identifier messageId, String connectingString){
        messageId.CreateStringWME("type", "description-request");
        messageId.CreateStringWME("originator", "mentor");
        Identifier fieldsId = messageId.CreateIdWME("fields");
    }

    public void extractLinguisticComponents(String string, Map tagsToWords) {  }
}
