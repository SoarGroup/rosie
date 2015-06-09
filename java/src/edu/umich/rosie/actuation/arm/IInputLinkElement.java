package edu.umich.rosie.actuation.arm;

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
    
    // Handle an init-soar (release all references to sml objects)
    void onInitSoar();
    
    // remove the object from the input-link
    void destroy();
   
}
