package com.spartanlaboratories.engine.structure;

import java.awt.event.KeyEvent;
import java.util.ArrayList;

import com.spartanlaboratories.engine.game.Actor;
import com.spartanlaboratories.engine.game.VisibleObject;
import com.spartanlaboratories.engine.util.Location;

public interface Camera {
	public ArrayList<Quad> getQuadList();
	public void generateQuad(VisibleObject visibleObject);
	public default void clearQuads(){
		getQuadList().clear();
	}
	public Location getLocationInWorld(Location locationOnScreen);
	public ArrayList<VisibleObject> getQualifiedObjects();
	public Actor unitAt(Location monitorLocation);
	public void handleClick(int mouseButton);
	public void handleMouseLocation(Location monitorLocation);
	public void handleKeyPress(KeyEvent keyEvent);
	public boolean coversMonitorLocation(Location locationOnScreen);
	void handleMouseWheel(int change, Location locationOnScreen);
}
