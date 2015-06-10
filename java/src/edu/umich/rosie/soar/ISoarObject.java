package edu.umich.rosie.soar;

import sml.Identifier;

public interface ISoarObject {
	// Add the object to working memory, on the given identifier
	public void addToWM(Identifier parentId);
	
	// Returns true if the object is currently in working memory
	public boolean isAdded();
	
	// Update is called before input phase, changes can be made to WM
	public void updateWM();
	
	// Release all references to SML objects and remove from WM
	public void removeFromWM();
}
