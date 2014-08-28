package edu.umich.insoar.language.Patterns;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sml.Identifier;
import edu.umich.insoar.language.LinguisticEntity;

public class ObjectIdentification extends LinguisticEntity{
    public static String TYPE = "ObjectIdentification";
    private LingObject object = null;
    private Set<String> adjective;
    
    public LingObject getObject(){
        return object;
    }
    
    public void translateToSoarSpeak(Identifier messageId, String connectingString){
        messageId.CreateStringWME("type", "object-message");
        messageId.CreateStringWME("originator", "mentor");
        Identifier fieldsId = messageId.CreateIdWME("information");
        if(object != null){
            object.translateToSoarSpeak(fieldsId,"object");
            object.setValue("specifier", "this");
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
