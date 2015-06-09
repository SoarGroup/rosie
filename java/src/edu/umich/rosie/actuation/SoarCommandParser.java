package edu.umich.rosie.actuation;

import java.util.HashMap;
import java.util.Map;
//
//import edu.umich.rosie.SoarUtil;
//import april.util.TimeUtil;
//import probcog.commands.TypedValue;
//import probcog.lcmtypes.condition_test_t;
//import probcog.lcmtypes.control_law_t;
//import probcog.lcmtypes.typed_value_t;
//import sml.Identifier;
//import sml.WMElement;

import sml.Identifier;
import sml.WMElement;

public class SoarCommandParser {
//	public static control_law_t parseControlLaw(Identifier id){
//		control_law_t cl = new control_law_t();
//
//		cl.id = -1;
//
//		// Name of the condition test
//		cl.name = SoarUtil.getValueOfAttribute(id, "name");
//		if(cl.name == null){
//			System.err.println("No ^name attribute on condition test");
//			return null;
//		}
//
//		// Parameters of the control law
//		HashMap<String, typed_value_t> params = parseParameters(id, "parameters");
//		cl.num_params = params.size();
//		cl.param_names = new String[cl.num_params];
//		cl.param_values = new typed_value_t[cl.num_params];
//		int i = 0;
//		for(Map.Entry<String, typed_value_t> e : params.entrySet()){
//			cl.param_names[i] = e.getKey();
//			cl.param_values[i] = e.getValue();
//			i++;
//		}
//
//		// Termination condition - when to stop
//		Identifier termId = SoarUtil.getIdentifierOfAttribute(id, "termination-condition");
//		cl.termination_condition = parseConditionTest(termId);
//		if(cl.termination_condition == null){
//			System.err.println("Invalid termination condition");
//			return null;
//		}
//
//		return cl;
//	}
//
//	public static condition_test_t parseConditionTest(Identifier id){
//		condition_test_t ct = new condition_test_t();
//
//		// Null condition test
//		if(id == null){
//			ct.name = "NONE";
//			ct.num_params = 0;
//			ct.param_names = new String[0];
//			ct.param_values = new typed_value_t[0];
//			ct.compare_type = condition_test_t.CMP_EQ;
//			ct.compared_value = (new TypedValue("")).toLCM();
//			return ct;
//		}
//
//		// Name of the condition test
//		ct.name = SoarUtil.getValueOfAttribute(id, "name");
//		if(ct.name == null){
//			System.err.println("No ^name attribute on condition test");
//			return null;
//		}
//
//		// Parameters of the condition
//		HashMap<String, typed_value_t> params = parseParameters(id, "parameters");
//		ct.num_params = params.size();
//		ct.param_names = new String[ct.num_params];
//		ct.param_values = new typed_value_t[ct.num_params];
//		int i = 0;
//		for(Map.Entry<String, typed_value_t> e : params.entrySet()){
//			ct.param_names[i] = e.getKey();
//			ct.param_values[i] = e.getValue();
//			i++;
//		}
//
//		ct.compare_type = condition_test_t.CMP_GT;
//		ct.compared_value = (new TypedValue(0)).toLCM();
//		// compare-type
////		//   The type of comparison (gt, gte, eq, lte, lt)
////		String compareType = SoarUtil.getValueOfAttribute(id, "compare-type");
////		if(compareType == null){
////			System.err.println("No compare-type on condition test");
////			return null;
////		} else if(compareType.equals("gt")){
////			ct.compare_type = condition_test_t.CMP_GT;
////		} else if(compareType.equals("gte")){
////			ct.compare_type = condition_test_t.CMP_GTE;
////		} else if(compareType.equals("eq")){
////			ct.compare_type = condition_test_t.CMP_EQ;
////		} else if(compareType.equals("lte")){
////			ct.compare_type = condition_test_t.CMP_LTE;
////		} else if(compareType.equals("lt")){
////			ct.compare_type = condition_test_t.CMP_LT;
////		} else {
////			System.err.println("Unknown compare-type on condition test");
////		}
//
//		// compared-value
//		//   the value being compared against when evaluating the test
////		String comparedValue = SoarUtil.getValueOfAttribute(id, "compared-value");
////		if(comparedValue == null){
////			System.err.println("no compared-value on condition test");
////			return null;
////		}
////		ct.compared_value = SoarUtil.wrapTypedValue(id, "compared-value");
//
//		return ct;
//	}
//
//	public static HashMap<String, typed_value_t> parseParameters(Identifier id, String att){
//		HashMap<String, typed_value_t> params = new HashMap<String, typed_value_t>();
//		Identifier paramsId = SoarUtil.getIdentifierOfAttribute(id, att);
//		if(paramsId != null){
//			for(int i = 0; i < paramsId.GetNumberChildren(); i++){
//				WMElement wme = paramsId.GetChild(i);
//				String name = wme.GetAttribute();
//				params.put(name, SoarUtil.wrapTypedValue(wme));
//			}
//		}
//
//		return params;
//	}
//	
//	public static control_law_t createEmptyControlLaw(String name){
//		control_law_t cl = new control_law_t();
//		cl.utime = TimeUtil.utime();
//		cl.id = -1;
//		cl.name = name;
//		cl.num_params = 0;
//		cl.param_names = new String[0];
//		cl.param_values = new typed_value_t[0];
//		cl.termination_condition = parseConditionTest(null);
//		return cl;
//	}
    
//    public static typed_value_t wrapTypedValue(WMElement wme){
//		typed_value_t tv = new typed_value_t();
//		tv.value = wme.GetValueAsString();
//
//		String valType = wme.GetValueType();
//        if(valType.equals(INTEGER_VAL)){
//        	tv.type = typed_value_t.TYPE_INT;
//        } else if(valType.equals(FLOAT_VAL)){
//        	tv.type = typed_value_t.TYPE_DOUBLE;
//        } else if(tv.value.equals("true") || tv.value.equals("false")){
//        	tv.type = typed_value_t.TYPE_BOOL;
//        } else {
//        	tv.type = typed_value_t.TYPE_STRING;
//        }
//        return tv;
//    }
//
//	public static typed_value_t wrapTypedValue(Identifier id, String att){
//		WMElement wme = id.FindByAttribute(att, 0);
//        if(wme == null || wme.GetValueAsString().length() == 0){
//            return null;
//        }
//        return wrapTypedValue(wme);
//	}
}
