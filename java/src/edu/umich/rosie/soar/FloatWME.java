package edu.umich.rosie.soar;

import sml.FloatElement;
import sml.Identifier;

public class FloatWME implements ISoarObject{
	private String att;
	private Double val;
	
	private FloatElement wme;
	
	private boolean changed = false;
	private boolean added = false;
	
	public FloatWME(String att, Double val){
		this.att = att;
		this.val = val;
		this.wme = null;
	}
	
	public boolean isAdded(){
		return added;
	}		
	
	public String getAttribute(){
		return att;
	}

	public void setValue(Double newVal){
		val = newVal;
		changed = true;
	}
	public double getValue(){
		return val;
	}

	public void addToWM(Identifier parentId) {
		if(wme != null){
			removeFromWM();
		}
		wme = parentId.CreateFloatWME(att, val);
		added = true;
	}
	
	public void updateWM() {
		if(wme == null || !changed){
			return;
		}
		if(wme.GetValue() != val){
			wme.Update(val);
		}
		changed = false;
	}

	public void removeFromWM() {
		if(wme != null){
			wme.DestroyWME();
			wme = null;
			added = false;
		}
	}
}
