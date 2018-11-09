package com.spartanlaboratories.engine.util;

import com.spartanlaboratories.engine.structure.Console;
import com.spartanlaboratories.engine.structure.StandardCamera;

/**
 * Stores a Point with the coordinates (x,y) which will most often designate an object's location in the world. 
 * Has a variety of methods of modifying those coordinates as well as converting them to represent an object's location on screen and visa versa.
 * @author Spartak
 * 
 */
public class Location extends com.spartanlaboratories.measurements.Location{
	public Location(double setX, double setY){
		super(setX,setY);
	}
	/**
	 * Creates a new Location which copies the coordinates of the given location. This does NOT 
	 * make this location reference the parameter, 
	 * @param relativeLocation the location whose coordinates will be copied
	 * @see #Location()
	 * @see #Location(double, double)
	 */
	public Location(com.spartanlaboratories.measurements.Location relativeLocation){
		super(relativeLocation);
	}
	/**
	 * Creates a Location at the given point on the screen as seem by the given camera.
	 * The Location will be created as a "real" location and not an on-screen one.
	 * @param locationOnScreen the location whose coordinates will be converted to "real" coordinates
	 * @param camera the camera that is viewing the location
	 * @see #Location() 
	 * @see #Location(double, double) 
	 * @see #Location(com.spartanlaboratories.measurements.Location)
	 */
	public Location(com.spartanlaboratories.measurements.Location locationOnScreen, StandardCamera camera){
		setFromScreen(locationOnScreen, camera);
	}
	/**
	 * Creates a {@link Location} with the coordinates (0,0)
	 * @see #Location(com.spartanlaboratories.measurements.Location)
	 * @see #Location(double, double)
	 */
	public Location(){}
	/**
	 * Returns at what point on the screen this location will be at as seen by the given {@link StandardCamera}
	 * 
	 * @deprecated
	 * @param camera the camera that will be viewing this location
	 * @return this location's coordinates on a screen stored in a new location
	 */
	public Location getScreenCoords(StandardCamera camera){ 
		return new Location(camera.monitorLocation.x - camera.worldLocation.x + x,
				camera.monitorLocation.y - camera.worldLocation.y + y);
	}
	/** 
	 * Returns a Location the coordinates of which are "real world" coordinates which correspond to the screen coordinates passed in.
	 * 
	 * @deprecated 
	 * @param locationOnScreen The location on screen whose coordinates are being converted to real world coordinates.
	 * @param camera The camera that is viewing the location on screen.
	 * @return a new Location with real world coordinates
	 */
	public static Location getLocationInWorld(com.spartanlaboratories.measurements.Location locationOnScreen, StandardCamera camera){
		return new
				Location(camera.worldLocation.x + locationOnScreen.x - camera.monitorLocation.x,
				camera.worldLocation.y - locationOnScreen.y + camera.monitorLocation.y);
	}
	/**
	 * Sets the coordinates of this location to be what the coordinates of the passed location are in the "real" world. For example if a location on screen
	 * is passed in with the coordinates (x1,y1) then first of all based on the camera the passed in location's coordinates will be converted to their "real"
	 * values which are say (x2,y2) and then this location's coordinates will be set to (x2, y2). 
	 * 
	 * @deprecated
	 * @param locationOnScreen the location on a screen whose "real" world coordinates will be assigned to this location
	 * @param camera the camera that is viewing the passed location on screen
	 */
	public void setFromScreen(com.spartanlaboratories.measurements.Location locationOnScreen, StandardCamera camera){
		setCoords(camera.worldLocation.x + locationOnScreen.x - camera.monitorLocation.x,
			camera.worldLocation.y - locationOnScreen.y + camera.monitorLocation.y);
	}
	/**
	 * Prints the {@link #toString()} value of this location to the passed in console.
	 * 
	 * @param console - The console that will be displaying this location
	 */
	public void printTo(Console console){
		console.out(toString());
	}
	public Location getReciprocal(){
		return new Location(super.getReciprocal());
	}
	public Location copy(){
		return new Location(super.copy());
	}
	public static Location parseLocation(String string){
		return new Location(com.spartanlaboratories.measurements.Location.parseLocation(string));
	}
}
