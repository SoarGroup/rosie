package edu.umich.rosie.soar;

import sml.StringElement;
import sml.Identifier;

public class StringWME implements ISoarObject{
	private String att;
	private String val;
	
	private StringElement wme;
	
	private boolean changed = false;
	private boolean added = false;
	
	public StringWME(String att, String val){
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

	public void setValue(String newVal){
		val = newVal;
		changed = true;
	}
	
	public String getValue(){
		return val;
	}

	public void addToWM(Identifier parentId) {
		if(wme != null){
			removeFromWM();
		}
		wme = parentId.CreateStringWME(att, val);
		added = true;
	}
	
	public void updateWM() {
		if(wme == null || !changed){
			return;
		}
		if(!wme.GetValue().equals(val)){
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
