package com.spartanlaboratories.engine.structure;

import java.util.ArrayList;

import com.spartanlaboratories.engine.game.Actor;
import com.spartanlaboratories.engine.game.Alive.Faction;
import com.spartanlaboratories.engine.util.Location;

public abstract class Human extends Controller{
	protected Location screenSize = new Location();
	ArrayList<Camera> cameras = new ArrayList<Camera>();
	Location mouseLocation = new Location();
	public static ArrayList<Human> players = new ArrayList<Human>();
	protected final int MOUSELOCATION = 0, MOUSEWHEEL = 4;
	public Human(Engine engine, Faction setFaction) {
		super(engine, setFaction);
		
		players.add(this);
	}
	@Override 
	public void tick(){
		processQuadInfo();
		sendMouseLocation();
	}
	public abstract void poll();
	public abstract void processQuadInfo();
	public abstract void out(String message);
	public Location getMouseLocation(){
		return mouseLocation;
	}
	void receiveMouseInput(int button, String string) throws IllegalArgumentException{
		try{
			/*
			if(button == 0)
				coveringCamera(this.mouseLocation = Location.parseLocation(string)).handleMouseLocation(getMouseLocation());
			*/
			if(button == MOUSELOCATION)
				mouseLocation = Location.parseLocation(string);
			else if(button < 4 && button > 0){
				coveringCamera(getMouseLocation()).handleClick(button);
				if(button == 1){
					Actor clicked = coveringCamera(mouseLocation).unitAt(mouseLocation);
					if(clicked != null)
						setSelectedUnit(clicked);
				}
				else if(button == 3)
					if(selectedUnit != null)
						selectedUnit.rightClick(mouseLocation, coveringCamera(mouseLocation));
			}
			else if(button == MOUSEWHEEL)
				coveringCamera(getMouseLocation()).handleMouseWheel(Integer.parseInt(string), getMouseLocation());
			else throw new IllegalArgumentException();
			
		}catch(SLEImproperInputException e){
			System.out.println("the server caught improper input");
		}
	}
	public void receiveKeyInput(int key, String pressType){
		
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
