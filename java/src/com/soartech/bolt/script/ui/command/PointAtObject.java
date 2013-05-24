package com.soartech.bolt.script.ui.command;

import edu.umich.insoar.world.World;

public class PointAtObject implements UiCommand {
	private int objectId;
	
	public PointAtObject(int id) {
		objectId = id;
	}
	
	@Override
	public void execute() {
		World.Singleton().setPointedObjectID(objectId);
	}
}
