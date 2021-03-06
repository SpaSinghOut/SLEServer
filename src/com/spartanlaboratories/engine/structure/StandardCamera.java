package com.spartanlaboratories.engine.structure;

import java.awt.event.KeyEvent;
import java.util.ArrayList;

import com.spartanlaboratories.engine.game.Actor;
import com.spartanlaboratories.engine.game.VisibleObject;
import com.spartanlaboratories.engine.util.Location;
/**
 * <h1>The StandardCamera Object</h1>
 * <p>Used by the <a href="Human.html">Human</a> object for viewing visible objects
 * @author Spartak
 */
public class StandardCamera extends StructureObject implements Camera{
	private ArrayList<Quad> quads = new ArrayList<Quad>();
	public EdgePanRules edgePanRules = new EdgePanRules();
	public class EdgePanRules{
		boolean panOn;
		boolean panAll;
		boolean panScreen;
		int panningSpeed;
		int panningRange;
		public EdgePanRules(){
			panningSpeed = 20;
			panningRange = 60;
		}
		public void setPan(boolean pan){
			panOn = pan;
		}
	}
	/**
	 * A {@linkplain Location} that designates the center of this StandardCamera's point of view in the "real" world
	 */
	public Location worldLocation;
	/**
	 * A <a href = "Location.html">Location</a> that designates where the center viewpoint of this camera is located on the monitor.
	 */
	public Location monitorLocation;
	/**
	 * A Location that stores how far away from the {@link #worldLocation} objects can be seen by this StandardCamera. The x coordinate represents the total width of view
	 * of the StandardCamera and the y coordinate represents the total height of view of the StandardCamera. 
	 * So if this Location's coordinates are (x,y) then the camera will be 
	 * able to see objects that are x/2 to the right or left of the {@link #worldLocation} and y/2 up or down from the 
	 * {@link #worldLocation}. When a camera is 
	 * created the dimensions location is set to the resolution of the monitor (unless a custom display size was specified) and it is usually better 
	 * to be left that way unless a single display is being used to render the view of multiple Cameras.
	 * 
	 * @see #monitorLocation
	 */
	public Location dimensions;
	private double additionalSpeed, acceleration;
	Location screenSize;
	/**<h1>The StandardCamera Constructor</h1>
	 * <p>
	 * Creates a camera at the specified location and creates default location values
	 * @param engine
	 * the game engine
	 * @param worldLocation
	 * the location in the "real" world at which the camera is located
	 * @param screenSize the size of the screen
	 */
	public StandardCamera(Engine engine, Location worldLocation, Location screenSize){
		super(engine);
		this.screenSize = screenSize;
		this.worldLocation = worldLocation;
		dimensions = new Location(screenSize.x, screenSize.y);
		monitorLocation = new Location(screenSize.x / 2, screenSize.y / 2);
		edgePanRules.panningSpeed = 600 / Engine.getTickRate();
		additionalSpeed = 0;
		acceleration = 1;
	}
	/**
	 * increases camera speed by camera acceleration and gives back the combined speed
	 * @return the total current camera speed
	 */
	public int getCameraSpeed(){
		return (int) (edgePanRules.panningSpeed + (additionalSpeed += acceleration));
	}
	/**
	 * Sets the camera speed back to default
	 */
	public void resetCameraSpeed(){
		additionalSpeed = 0;
	}
	/**
	 * Checks whether the camera is able to see the given object. Identical to {@link #withinBounds(VisibleObject)}.
	 * 
	 * @param vo - The <a href="VisibleObject.html">Visible Object</a> the visibility of which is being tested
	 * @return a boolean value which represents the object's visibility
	 */
	public boolean canSeeObject(VisibleObject vo){
		double
		wx = worldLocation.x, wy = worldLocation.y, dx = dimensions.x, dy = dimensions.y, 
		vx = vo.getLocation().x, vy = vo.getLocation().y,
		vxmin = vx - vo.getWidth() / 2, vxmax = vx + vo.getWidth() / 2,
		vymin = vy - vo.getHeight() / 2, vymax = vy + vo.getHeight() / 2;
		boolean 
		xmintest = vxmax > wx - dx / 2, xmaxtest = vxmin < wx + dx / 2, 
		ymintest = vymax > wy - dy / 2, ymaxtest = vymin < wy + dy / 2;
		return xmintest||xmaxtest||ymintest||ymaxtest;
	}
	/**
	 * Returns whether or not the parameter is completely covered by the view of this camera.
	 * 
	 * @param vo - The parameter the visibility of which is being tested.
	 * @return {@code true} if all of the passed in visible object is visible.
	 * 	<br> {@code false} if any part of the passed in visible object is not visible.
	 */
	public boolean fullyWithinBounds(VisibleObject vo){
		Location loc = this.getMonitorLocation(vo.getLocation());
		double x = loc.x, y = loc.y;
		return xMinBound(x - vo.getWidth() / 2)
				&& xMaxBound(x + vo.getWidth() / 2)
				&& yMinBound(y - vo.getWidth() / 2)
				&& yMaxBound(y + vo.getWidth() / 2)
				;
	}
	/**
	 * Checks if any part of the passed in object is within the bounds of this camera. Should be identical 
	 * in functionality to {@link #canSeeObject(VisibleObject)}. The difference is this method uses the parameter's location on screen rather than the 
	 * real world location. This method provides an alternate way of checking the visibility of an object and should be used in case the algorithm in 
	 * {@link #canSeeObject(VisibleObject)} is broken.
	 * 
	 * @param vo - The {@link VisibleObject} whose presence within the borders of this camera is being tested.
	 * @return - A boolean value that represents whether or not this the passed in object is within borders.
	 */
	public boolean withinBounds(VisibleObject vo){
		Location loc = getMonitorLocation(vo.getLocation());
		double x = loc.x, y = loc.y;
		return xMinBound(x + vo.getWidth() / 2)
				|| xMaxBound(x - vo.getWidth() / 2)
				|| yMinBound(y + vo.getWidth() / 2)
				|| yMaxBound(y - vo.getWidth() / 2)
				;
	}
	public boolean withinBounds(Location l){
		return xBound(l.x)&&yBound(l.y);
	}
	public boolean xBound(double x){
		return xMinBound(x)&&xMaxBound(x);
	}
	public boolean yBound(double y){
		return yMinBound(y)&&yMaxBound(y);
	}
	public boolean xMinBound(double x){
		return x >= monitorLocation.x - dimensions.x / 2;
	}
	public boolean xMaxBound(double x){
		return x <= monitorLocation.x + dimensions.x / 2;
	}
	public boolean yMinBound(double y){
		return y >= monitorLocation.y - dimensions.y / 2;
	}
	public boolean yMaxBound(double y){
		return y <= monitorLocation.y + dimensions.y / 2;
	}
	@Override
	public void generateQuad(VisibleObject visibleObject){
		String texture = visibleObject.getTexture();
		boolean hasTexture = texture != null;
		Location[] quadCorners = new Location[4], textureValues = new Location[4];
		quadCorners[0] = getMonitorLocation(visibleObject.getAreaCovered().northWest);
		quadCorners[1] = getMonitorLocation(visibleObject.getAreaCovered().northEast);
		quadCorners[2] = getMonitorLocation(visibleObject.getAreaCovered().southEast);
		quadCorners[3] = getMonitorLocation(visibleObject.getAreaCovered().southWest);
		textureValues[0] = new Location();
		textureValues[1] = hasTexture ? new Location(1, 0) : new Location();
		textureValues[2] = hasTexture ? new Location(1, 1): new Location();
		textureValues[3] = hasTexture ? new Location(0, 1) :new Location();
		Quad quad = new Quad(quadCorners, textureValues);
		quad.texture = visibleObject.getTextureInfo().namePath;
		quad.color = visibleObject.getColor();
		getQuadList().add(quad);
	}
	@Override
	public Actor unitAt(Location monitorLocation){
		Location location = getLocationInWorld(monitorLocation);
		final int searchRange = 200;
		ArrayList<Actor> actors = engine.qt.retriveActors(location.x - searchRange, location.y - searchRange, location.x + searchRange, location.y + searchRange);
		for(Actor a: actors)
			if(engine.util.checkPointCollision(a, location))
				return a;
		return null;
	}
	@Override
	public void handleClick(int mouseButton) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void handleMouseLocation(Location locationOnScreen) {
		if(!edgePanRules.panOn)return;
		int range = edgePanRules.panningRange;
		int speed = getCameraSpeed();
		if(locationOnScreen.x < range)worldLocation.changeX(-speed);
		else if(locationOnScreen.x > screenSize.x - range)
			worldLocation.changeX(speed);
		if(locationOnScreen.y < range)worldLocation.changeY(speed);
		else if(locationOnScreen.y > screenSize.y - range)worldLocation.changeY(-speed);
		if(locationOnScreen.x > range && locationOnScreen.x < screenSize.x - range &&
				locationOnScreen.y > range && locationOnScreen.y < screenSize.y - range)
			resetCameraSpeed();
	}
	@Override
	public Location getLocationInWorld(Location locationOnScreen) {
		return new Location(worldLocation.x + locationOnScreen.x - monitorLocation.x,
							worldLocation.y + locationOnScreen.y - monitorLocation.y);
	}
	public Location getMonitorLocation(com.spartanlaboratories.measurements.Location northWest){
		return new Location(monitorLocation.x - worldLocation.x + northWest.x,
				monitorLocation.y - worldLocation.y + northWest.y);
	}
	@Override
	public ArrayList<VisibleObject> getQualifiedObjects() {
		return engine.qt.retrieveBox(worldLocation.x - dimensions.x / 2, worldLocation.y - dimensions.y / 2, 
				worldLocation.x + dimensions.x / 2, worldLocation.y + dimensions.y / 2);
	}
	@Override
	public boolean coversMonitorLocation(Location locationOnScreen) {
		return withinBounds(locationOnScreen);
	}
	@Override
	public void handleKeyPress(KeyEvent keyEvent) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void handleMouseWheel(int change, Location locationOnScreen) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public ArrayList<Quad> getQuadList() {
		// TODO Auto-generated method stub
		return quads;
	}
}
