package com.spartanlaboratories.engine.structure;

import java.awt.event.KeyEvent;
import java.util.ArrayList;

import com.spartanlaboratories.engine.game.Ability;
import com.spartanlaboratories.engine.game.Actor;
import com.spartanlaboratories.engine.game.Alive.Faction;
import com.spartanlaboratories.engine.game.Hero;
import com.spartanlaboratories.engine.util.Location;

public abstract class Human extends Controller{
	protected Location screenSize = new Location();
	ArrayList<Camera> cameras = new ArrayList<Camera>();
	Location mouseLocation = new Location();
	public static ArrayList<Human> players = new ArrayList<Human>();
	protected final int MOUSELOCATION = 0, MOUSELEFT = 1, MOUSEMIDDLE = 2, MOUSERIGHT = 3, MOUSEWHEEL = 4;
	public Human(Engine engine, Faction setFaction) {
		super(engine, setFaction);
		players.add(this);
		this.engine.map.forConnected(this);
	}
	@Override 
	public void tick(){
		processQuadInfo();
		sendMouseLocation();
	}
	public abstract void poll();
	public abstract void processQuadInfo();
	public abstract void out(String message);
	public abstract void notifyClient(String message);
	public Location getMouseLocation(){
		return mouseLocation;
	}
	void receiveMouseInput(int button, String string) throws IllegalArgumentException{
		try{
			if(button == MOUSELOCATION)
				mouseLocation = Location.parseLocation(string);
			else if(button < 4 && button > 0){
				coveringCamera(getMouseLocation()).handleClick(button);
				if(button == MOUSELEFT){
					Actor clicked = coveringCamera(mouseLocation).unitAt(mouseLocation);
					if(clicked != null)
						setSelectedUnit(clicked);
				}
				else if(button == MOUSERIGHT)
					if(selectedUnit != null)
						if(controlledUnits.contains(selectedUnit))
							selectedUnit.rightClick(mouseLocation, coveringCamera(mouseLocation));
						else
							setSelectedUnit(controlledUnits.isEmpty() ? null : controlledUnits.get(0));
			}
			else if(button == MOUSEWHEEL)
				coveringCamera(getMouseLocation()).handleMouseWheel(Integer.parseInt(string), getMouseLocation());
			else throw new IllegalArgumentException();
			
		}catch(SLEImproperInputException e){
			System.out.println("the server caught improper input");
		}
	}
	public void receiveKeyInput(int key, String pressType){
		if(key == KeyEvent.VK_Q)((Ability)((Hero)controlledUnits.get(0)).abilities.get(0)).cast();
	}
	public Camera coveringCamera(Location locationOnScreen) throws SLEImproperInputException{
		for(Camera c:cameras)
			if(c.coversMonitorLocation(locationOnScreen))
				return c;
		throw new SLEImproperInputException(engine.tracker, "The Human type unit controller: " + engine.controllers.indexOf(this)
				+ " attempted a world based mouse action that was outside of the scope of any camera: " + locationOnScreen);
	}
	public void addCamera(Camera camera){
		cameras.add(camera);
	}
	void setScreenSize(com.spartanlaboratories.measurements.Location location){
		this.screenSize.duplicate(location);
	}
	public Location getScreenSize(){
		return screenSize;
	}
	private void sendMouseLocation(){
		for(Camera c: cameras)c.handleMouseLocation(mouseLocation);
	}
}
