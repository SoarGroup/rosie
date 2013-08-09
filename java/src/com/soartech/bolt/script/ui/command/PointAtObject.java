package com.soartech.bolt.script.ui.command;

import edu.umich.insoar.world.WorldModel;

public class PointAtObject implements UiCommand {
	private int objectId;
	
	public PointAtObject(int id) {
		objectId = id;
	}
	
	@Override
	public void execute() {
		// XXX: Fix
		//WorldModel.Singleton().setPointedObjectID(objectId);
	}
}
