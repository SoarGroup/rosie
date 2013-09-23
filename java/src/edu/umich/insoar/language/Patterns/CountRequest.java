package edu.umich.insoar.language.Patterns;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sml.Identifier;
import edu.umich.insoar.language.LinguisticEntity;

public class CountRequest extends LinguisticEntity{
    public static String TYPE = "CountRequest";
    private LingObject object = null;
    
    public LingObject getObject(){
        return object;
    }
    
    public void translateToSoarSpeak(Identifier messageId, String connectingString){
        messageId.CreateStringWME("type", "object-question");
        messageId.CreateStringWME("originator", "mentor");
        Identifier fieldsId = messageId.CreateIdWME("information");
        fieldsId.CreateStringWME("question-word", "how-many");
        if(object != null){
            object.translateToSoarSpeak(fieldsId,"object");
        }
    }

    public void extractLinguisticComponents(String string, Map tagsToWords) {
        Pattern p = Pattern.compile("OBJ\\d*");
        Matcher m = p.matcher(string);
        if(m.find()){
            object = (LingObject) tagsToWords.get(m.group());
        }
    }
}
