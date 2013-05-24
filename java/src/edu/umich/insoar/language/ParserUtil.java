package edu.umich.insoar.language;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParserUtil {
    
    public String extractPattern(EntityPattern pattern, String tagString, Map<String, Object> tagsToWords){
        int Counter = 0;
        Pattern regex = pattern.regex;
        String tag = pattern.tag;
        Matcher m = regex.matcher(tagString);
        if(m.find()){
            StringBuffer sb = new StringBuffer();
            do{ 
                LinguisticEntity entity = EntityFactory.createEntity(pattern.entityType);
                entity.extractLinguisticComponents(m.group(),tagsToWords);
                String newTag = tag+Integer.toString(Counter);
                tagsToWords.put(newTag, entity);
                m.appendReplacement(sb, newTag);
                Counter++;
            }while(m.find());
            m.appendTail(sb);
            return sb.toString();
        }
        return tagString;
    }
}
