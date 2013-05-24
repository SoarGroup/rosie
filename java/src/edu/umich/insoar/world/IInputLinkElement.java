package edu.umich.insoar.world;

import sml.Identifier;

/**
 * An interface for objects that will get written to the InputLink
 * 
 * @author mininger
 * 
 */
public interface IInputLinkElement
{    
    // update the input-link appropriately
    void updateInputLink(Identifier parentIdentifier);
    
    // remove the object from the input-link
    void destroy();
   
}
