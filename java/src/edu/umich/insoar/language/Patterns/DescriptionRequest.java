package edu.umich.insoar.language.Patterns;

import java.util.Map;

import sml.Identifier;
import edu.umich.insoar.language.LinguisticEntity;

public class DescriptionRequest extends LinguisticEntity{
    public static String TYPE = "DescriptionRequest";
    
    public void translateToSoarSpeak(Identifier messageId, String connectingString){
        messageId.CreateStringWME("type", "description-request");
        messageId.CreateStringWME("originator", "mentor");
        Identifier fieldsId = messageId.CreateIdWME("fields");
    }

    public void extractLinguisticComponents(String string, Map tagsToWords) {  }
}
