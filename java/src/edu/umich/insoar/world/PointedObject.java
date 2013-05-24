package edu.umich.insoar.world;

import sml.Identifier;
import sml.IntElement;

public class PointedObject implements IInputLinkElement
{
    private int objectID;
    private IntElement wme;
    
    public PointedObject(int objectID){
        this.objectID = objectID;
        this.wme = null;
    }
    
    public int getObjectID(){
        return objectID;
    }
    public void setObjectID(int objectID){
        this.objectID = objectID;
    }

    @Override
    public void updateInputLink(Identifier parentIdentifier)
    {
        if(objectID == -1){
            destroy();
        } else if(wme == null){
            wme = parentIdentifier.CreateIntWME("pointed-object", objectID);
        } else if(wme.GetValue() != objectID){
            wme.Update(objectID);
        }
    }

    @Override
    public void destroy()
    {
        if(wme != null){
            wme.DestroyWME();
            wme = null;
        }
    }

}
