package edu.umich.rosie.soar;


import sml.IntElement;
import sml.Identifier;

public class IntWME implements ISoarObject{
	private String att;
	private Long val;
	
	private IntElement wme;
	
	private boolean changed = false;
	private boolean added = false;
	
	public IntWME(String att, Long val){
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
	
	public void setValue(Long newVal){
		val = newVal;
		changed = true;
	}
	public Long getValue(){
		return val;
	}

	public void addToWM(Identifier parentId) {
		if(wme != null){
			removeFromWM();
		}
		wme = parentId.CreateIntWME(att, val);
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
