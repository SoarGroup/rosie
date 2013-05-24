package edu.umich.insoar.language;

import java.util.regex.Pattern;

public class EntityPattern
{
    public Pattern regex;
    public String tag;
    public String entityType;
    
    public EntityPattern(){
        regex = null;
        tag = null;
        entityType = null;
    }
    
    public EntityPattern(Pattern regex, String tag, String entityType){
        this.regex = regex;
        this.tag = tag;
        this.entityType = entityType;
    }
}
