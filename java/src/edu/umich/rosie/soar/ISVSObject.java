package edu.umich.rosie.soar;

import sml.Identifier;

public interface ISVSObject {
	// Add the object to working memory, on the given identifier
	public void addToWM(Identifier parentId, StringBuilder svsCommands);
	
	// Returns true if the object is currently in working memory
	public boolean isAdded();
	
	// Update is called before input phase, changes can be made to WM
	public void updateWM(StringBuilder svsCommands);
	
	// Release all references to SML objects and remove from WM
	public void removeFromWM(StringBuilder svsCommands);
}
