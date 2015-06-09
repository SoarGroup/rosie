package edu.umich.rosie; 

import java.util.HashSet;
import java.util.Set;

import sml.*;


/**
 * Provides some utility functions to better manipulate working memory
 * 
 * @author mininger
 * 
 */
public class SoarUtil
{

    // These are the three types that a WME can be, conform to Soar
    public static String INTEGER_VAL = "int";

    public static String FLOAT_VAL = "double";

    public static String STRING_VAL = "string";
    
    /**
     * Updates the given Identifier so that it becomes: identifier ^attribute
     * value, and is of the appropriate type
     */
    public static void updateWME(Identifier identifier, String attribute, String value){
        String valueType = getValueTypeOfString(value);

        // Create a new value WME of the appropriate type
        if (valueType.equals(INTEGER_VAL))
        {
            updateIntWME(identifier, attribute, Integer.parseInt(value));
        }
        else if (valueType.equals(FLOAT_VAL))
        {
            updateFloatWME(identifier, attribute, Double.parseDouble(value));
        }
        else
        {
            updateStringWME(identifier, attribute, value);
        }
    }
    
    /**
     * Updates the given Identifier so that it becomes: identifier ^attribute
     * value, using a FloatWME
     */
    public static void updateFloatWME(Identifier identifier, String attribute, double value){
        WMElement valueWME = identifier.FindByAttribute(attribute, 0);

        if (valueWME == null){
            identifier.CreateFloatWME(attribute, value);
        } else if(valueWME.GetValueType().equals(FLOAT_VAL)){
            FloatElement floatWME = valueWME.ConvertToFloatElement();
            if(floatWME.GetValue() != value){
                floatWME.Update(value);
            }
        } else {
            valueWME.DestroyWME();
            identifier.CreateFloatWME(attribute, value);
        }
    }
    
    /**
     * Updates the given Identifier so that it becomes: identifier ^attribute
     * value, using a IntWME
     */
    public static void updateIntWME(Identifier identifier, String attribute, long value){
        WMElement valueWME = identifier.FindByAttribute(attribute, 0);

        if (valueWME == null){
            identifier.CreateIntWME(attribute, value);
        } else if(valueWME.GetValueType().equals(INTEGER_VAL)){
            IntElement intWME = valueWME.ConvertToIntElement();
            if(intWME.GetValue() != value){
                intWME.Update(value);
            }
        } else {
            valueWME.DestroyWME();
            identifier.CreateIntWME(attribute, value);
        }
    }
    
    /**
     * Updates the given Identifier so that it becomes: identifier ^attribute
     * value, using a StringWME
     */
    public static void updateStringWME(Identifier identifier, String attribute, String value){
        WMElement valueWME = identifier.FindByAttribute(attribute, 0);

        if (valueWME == null){
            identifier.CreateStringWME(attribute, value);
        } else if(valueWME.GetValueType().equals(STRING_VAL)){
            StringElement stringWME = valueWME.ConvertToStringElement();
            if(!stringWME.GetValue().equals(value)){
                stringWME.Update(value);
            }
        } else {
            valueWME.DestroyWME();
            identifier.CreateStringWME(attribute, value);
        }
    }
    
    /**
     * If the given identifier has the given attribute, the associated WME will
     * be destroyed (only the first)
     */
    public static void removeWME(Identifier identifier, String attribute){
        WMElement valueWME = identifier.FindByAttribute(attribute, 0);
        if(valueWME != null){
            valueWME.DestroyWME();
        }
    }

    /**
     * Returns the type of the given string, can be either INTEGER_VAL,
     * DOUBLE_VAL, or STRING_VAL
     */
    public static  String getValueTypeOfString(String s)
    {
        try
        {
            Long.parseLong(s);
            return INTEGER_VAL;
        }
        catch (NumberFormatException e)
        {
            try
            {
                Double.parseDouble(s);
                return FLOAT_VAL;
            }
            catch (NumberFormatException e2)
            {
                return STRING_VAL;
            }
        }
    }

		
    /**
     * Given id and attribute, returns value for WME (id ^attribute value)
     */
    public static String getValueOfAttribute(Identifier id, String attribute){
        WMElement wme = id.FindByAttribute(attribute, 0);
        if(wme == null || wme.GetValueAsString().length() == 0){
            return null;
        }
        return wme.GetValueAsString();
    }
    
    /**
     * Given id and attribute, returns value for WME (id ^attribute value)
     * If that attribute doesn't exist, throws an IllegalStateException using the given errorMessage
     * and adds (id ^status error)
     */
    public static String getValueOfAttribute(Identifier id, String attribute, String errorMessage) throws IllegalStateException{
        String attString = getValueOfAttribute(id, attribute);
        if(attString == null){
            id.CreateStringWME("status", "error");
            throw new IllegalStateException(errorMessage);
        }
        return attString;
    }

    /**
     * Given id and attribute, returns the child identifier for WME (id ^attribute childId)
     */
    public static Identifier getIdentifierOfAttribute(Identifier id, String attribute){
        WMElement wme = id.FindByAttribute(attribute, 0);
        if(wme == null || !wme.IsIdentifier()){
            return null;
        }
        return wme.ConvertToIdentifier();
    }    
    
    /**
     * Given id and attribute, returns the child identifier for WME (id ^attribute childId)
     * If that attribute doesn't exist, throws an IllegalStateException using the given errorMessage
     * and adds (id ^status error)
     */
    public static Identifier getIdentifierOfAttribute(Identifier id, String attribute, String errorMessage){
        WMElement wme = id.FindByAttribute(attribute, 0);
        if(wme == null || !wme.IsIdentifier()){
            id.CreateStringWME("status", "error");
            throw new IllegalStateException(errorMessage);
        }
        return wme.ConvertToIdentifier();
    }
    
    /**
     * Given id and attribute, returns a set of mutli-valued attributes for each WME matching
     * (id ^attribute value)
     */
    public static Set<String> getAllValuesOfAttribute(Identifier id, String attribute){
        Set<String> attStrings = new HashSet<String>();
        for(int index = 0; index < id.GetNumberChildren(); index++){
            WMElement wme = id.GetChild(index);
            if(wme.GetAttribute().equals(attribute)){
                attStrings.add(wme.GetValueAsString());
            }
        }
        return attStrings;
    }
}
