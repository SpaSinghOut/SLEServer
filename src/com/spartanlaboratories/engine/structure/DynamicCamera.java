package com.spartanlaboratories.engine.structure;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;

import com.spartanlaboratories.engine.game.Actor;
import com.spartanlaboratories.engine.game.VisibleObject;
import com.spartanlaboratories.engine.util.Location;
import com.spartanlaboratories.measurements.Rectangle;

public class DynamicCamera extends StructureObject implements Camera{
	// FIELD DECLARATIONS
	private ArrayList<Quad> quads = new ArrayList<Quad>();
	private Rectangle world;
	private Rectangle monitor;
	private final Location standardZoomValues = new Location();
	private final Location zoomBind = new Location();
	private Location zoomLevel = new Location();
	private Location amplification = new Location();
	private HashMap<Number, VisibleObject> objectBinds = new HashMap<Number, VisibleObject>();
	private HashMap<Number, Location> locationBinds = new HashMap<Number, Location>();
	private Location defaultZoomAmount = new Location();
	private double zoomMouseImpact;
	private double speed, additionalSpeed, acceleration, panningRange;
	private boolean centralZoom, holdPointZoom;
	
	// CONSTRUCTORS
	
	public DynamicCamera(Engine engine, Rectangle world, Rectangle monitor){
		super(engine);
		this.world = world.copy();
		this.monitor = monitor.copy();
		zoomBind.setCoords(0, 1000);
		zoomMouseImpact = 0;
		setDefaultZoomAmount(1.05);
		setStandardZoom();
		updateZoom();
		zoomOut(1);
	}
	/**
	 * Creates a DynamicCamera object with default properties. The default properties are: the location of the display is the center of the monitor, the size
	 * of the display is the size of the monitor, the size of the world display has a 1:1 ratio to the size of the display, the world center is (0,0).
	 * 
	 * @param engine
	 */
	public DynamicCamera(Engine engine, Location screenSize){
		this(engine, 	new Rectangle(new Location(), screenSize.x, screenSize.y), 
						new Rectangle(new Location(screenSize.x / 2, screenSize.y / 2), 
						screenSize.x, screenSize.y));
	}
	
	// BASIC GETTERS AND SETTERS
	
	public Rectangle getWorldArea(){
		return world.copy();
	}
	public Rectangle getMonitorArea(){
		return monitor.copy();
	}
	public void setMonitorStats(Location center, Location size){
		monitor = new Rectangle(center, size.x, size.y);
	}
	public void setWorldLocation(Location center){
		world.setCenter(center);
	}
	public void changeWorldLocation(Location locChange){
		Location location = new Location();
		location.duplicate(world.getCenter());
		location.change(locChange);
		world.setCenter(location);
	}
	@SuppressWarnings("unused")
	private void setWorldStats(Location center, Location size){
		world = new Rectangle(center, size.x, size.y);
	}
	private void setWorldSize(com.spartanlaboratories.measurements.Location worldSize){
		world.setSize(worldSize);
	}
	private void magnifyWorld(com.spartanlaboratories.measurements.Location location){
		com.spartanlaboratories.measurements.Location worldSize = world.getSize();
		worldSize.magnify(location);
		setWorldSize(worldSize);
		//setWorldSize(new Location(world.getSize().x * location.x, world.getSize().y * location.y));
	}
	public void setMonitorSize(Location newSize){
		monitor.setSize(newSize);
	}
	public void setDrawArea(Rectangle rectangle){
		Location zoomChange = new Location(rectangle.getSize().x / monitor.getSize().x, rectangle.getSize().y / monitor.getSize().y);
		magnifyWorld(zoomChange);
		setStandardZoomRelative(zoomChange);
		monitor.duplicate(rectangle);
		updateZoom();
	}
	public void setDefaultZoomAmount(double d) {
		setDefaultZoomAmount(d,d);
	}
	public void setDefaultZoomAmount(Location location){
		setDefaultZoomAmount(location.x, location.y);
	}
	public void setDefaultZoomAmount(double x, double y){
		defaultZoomAmount.setCoords(x,y);
	}
	public void setZoomMouseImpact(double impact){
		zoomMouseImpact = impact;
	}
	public void setCameraSpeed(int speed){
		this.speed = ((double)speed) / Engine.getTickRate();
	}
	public void setCameraAcceleration(int acceleration){
		this.acceleration = ((double)acceleration) / Engine.getTickRate();
	}
	public void setPanningRange(int numPixels){
		panningRange = numPixels;
	}
	private double getSpeed(){
		return speed + (additionalSpeed += acceleration);
	}
	private void resetSpeed(){
		additionalSpeed = 0;
	}
	public void setCentralZoom(boolean flag){
		centralZoom = flag;
	}
	public void followMouseOnZoom(boolean flag){
		centralZoom = !flag;
	}
	public void holdPointOnZoom(boolean flag){
		holdPointZoom = flag;
	}
	
	//*********************************   ZOOM METHODS   ************************************//
	
	public void zoomOut(){
		zoomOut(1.05);
	}
	public void zoomIn(){
		zoomIn(1.05);
	}
	public void zoomOut(double amplitude){
		zoomOut(new Location(amplitude, amplitude));
	}
	public void zoomIn(double amplitude){
		zoomIn(new Location(amplitude, amplitude));
	}
	public void zoomOut(Location amplitude){
		magnifyWorld(amplitude);
		if(!updateZoom())
			magnifyWorld(amplitude.getReciprocal());
	}
	public void zoomIn(Location amplitude){
		zoomOut(amplitude.getReciprocal());
	}
	public void setZoom(double newZoomValue){
		toStandardZoom();
		zoomIn(newZoomValue);
	}
	public void setZoomAbsolute(Location ratio){
		zoomOut(amplification);
		zoomIn(ratio);
	}
	public void toStandardZoom(){
		zoomOut(zoomLevel);
	}
	
	
	public void setStandardZoom(){
		standardZoomValues.setCoords(world.getSize().x, world.getSize().y);
	}
	public void setStandardZoomRelative(double ratio){
		magnifyWorld(new Location(ratio, ratio).getReciprocal());
		setStandardZoom();
	}
	public void setStandardZoomRelative(Location location){
		standardZoomValues.magnify(location);
	}
	public void setStandardZoomAbsolute(double ratio){
		resetStandardZoom();
		standardZoomValues.magnify(ratio);
		updateZoom();
	}
	public void resetStandardZoom(){
		standardZoomValues.duplicate(getMonitorArea().getSize());
		updateZoom();
	}

	
	private boolean updateZoom(){
		zoomLevel.setCoords(world.getSize().x / standardZoomValues.x, world.getSize().y / standardZoomValues.y);
		updateAmplification();
		return zoomLevel.x > zoomBind.x && zoomLevel.y > zoomBind.x && zoomLevel.x < zoomBind.y && zoomLevel.y < zoomBind.y;
	}
	@SuppressWarnings("unused")
	private void updateZoom(double ratio){
		zoomLevel.magnify(ratio);
	}
	private void updateAmplification(){
		amplification.setCoords(monitor.getSize().x / world.getSize().x, monitor.getSize().y / world.getSize().y);
	}
	public Location getZoomLevel(){
		return zoomLevel;
	}
	public Location getAmplification(){
		return amplification;
	}
	public com.spartanlaboratories.measurements.Location getStandardZoomValues(){
		return standardZoomValues.copy();
	}
	public com.spartanlaboratories.measurements.Location getZoomBounds(){
		return zoomBind.copy();
	}
	public void setZoomBounds(Location location){
		zoomBind.duplicate(location);
	}
	public void setZoomBounds(double x, double y){
		setZoomBounds(new Location(x,y));
	}
	
	//**********************************   END ZOOM METHODS   ******************************//
	
	// DYNAMIC CAMERA SPECIFIC METHODS
	public boolean isWorldBound(com.spartanlaboratories.measurements.Location northWest){
		return withinWorldXBounds(new Location(northWest)) && withinWorldYBounds(new Location(northWest));
	}
	public boolean isMonitorBound(Location location){
		return withinMonitorXBounds(location) && withinMonitorYBounds(location);
	}
	private boolean withinWorldXBounds(Location location){
		return withinWorldXBounds(location.x);
	}
	private boolean withinWorldYBounds(Location location){
		return withinWorldYBounds(location.y);
	}
	private boolean withinMonitorXBounds(Location location){
		return withinMonitorXBounds(location.x);
	}
	private boolean withinMonitorYBounds(Location location){
		return withinMonitorYBounds(location.y);
	}
	private boolean withinWorldXBounds(double x){
		return x <= world.getXMax() && x >= world.getXMin();
	}
	private boolean withinWorldYBounds(double y){
		return y >= world.getYMin() && y <= world.getYMax();
	}
	private boolean withinMonitorXBounds(double x){
		return x <= monitor.getXMax() && x >= monitor.getXMin();
	}
	private boolean withinMonitorYBounds(double y){
		return y <= monitor.getYMax() && y >= monitor.getYMin();
	}
	
	public void moveWorld(Location locChange){
		moveWorld(locChange.x, locChange.y);
	}
	public void moveWorld(double x, double y){
		world.setCenter(new Location(world.getCenter().x + x, world.getCenter().y + y));
	}

	public Location getRelativeScreenLocation(Location locationOnScreen){
		com.spartanlaboratories.measurements.Location relativeLocation = locationOnScreen.copy();
		relativeLocation.change(monitor.getCenter().getOpposite());
		return new Location(relativeLocation);
	}
	public Location getMonitorLocation(com.spartanlaboratories.measurements.Location northWest){
		Location relativeLocation = new Location();
		relativeLocation.duplicate(northWest);
		relativeLocation.change(world.getCenter().getOpposite());
		relativeLocation.magnify(amplification);
		relativeLocation.change(monitor.getCenter());
		return relativeLocation;
	}
	public void addObjectKeyBind(VisibleObject object, Number key){
		if(objectBinds.putIfAbsent(key, object) != null)
			System.out.println("That key is already being used for something else.");
	}
	public void addLocationKeyBind(Location location, Number key){
		if(locationBinds.putIfAbsent(key, location) != null)
			System.out.println("That key is already being used for something else.");
	}
	public void clearKeyBind(Number key){
		objectBinds.remove(key);
		locationBinds.remove(key);
	}
	public void handleMouseWheel(int change){
		System.out.println(defaultZoomAmount);
		zoomOut(new Location((defaultZoomAmount.x - 1) * change + 1, (defaultZoomAmount.y - 1) * change + 1));
	}
	
	// ENGINE INTEGRATION
	public boolean isAtWorldLocation(Location worldLocation){
		return world.getCenter().equals(worldLocation);
	}
	public void printTo(Console console){
		console.out(toString());
	}
	public boolean isOwnedBy(HumanClient human){
		return human.cameras.contains(this);
	}
	public boolean isOnMonitor(HumanClient human){
		int x = (int) human.getScreenSize().x, y = (int) human.getScreenSize().y;
		return	monitor.getXMin() < x 
			&&	monitor.getXMax() > 0 
			&&	monitor.getYMin() > 0 
			&&	monitor.getYMax() < y;
	}
	public boolean canSeeObjectPartially(VisibleObject visibleObject){
		Rectangle area = visibleObject.getAreaCovered();
		return isWorldBound(area.northWest)
			|| isWorldBound(area.northEast)
			|| isWorldBound(area.southWest)
			|| isWorldBound(area.southEast);
	}
	public boolean canSeeObjectCenter(VisibleObject visibleObject){
		return isWorldBound(visibleObject.getLocation());
	}
	public boolean canSeeObjectWholly(VisibleObject visibleObject){
		Rectangle area = visibleObject.getAreaCovered();
		return withinWorldXBounds(area.getXMin())
				&& withinWorldXBounds(area.getXMax())
				&& withinWorldYBounds(area.getYMin())
				&& withinWorldYBounds(area.getYMax());
	}
	
	// Method overrides and other generic API
	public String toString(){
		return "In world: " + world.getCenter() + world.getSize() + " On monitor: " + monitor.getCenter() + monitor.getSize();
	}
	public DynamicCamera copy(){
		return new DynamicCamera(engine, world, monitor);
	}
	public void copyTo(DynamicCamera camera){
		camera.duplicate(this);
	}
	public void duplicate(DynamicCamera camera){
		world.setCenter(camera.getWorldArea().getCenter());
		world.setSize(camera.getWorldArea().getSize());
		monitor.setCenter(camera.getMonitorArea().getCenter());
		monitor.setSize(camera.getMonitorArea().getSize());
		standardZoomValues.duplicate(camera.getStandardZoomValues());
		zoomBind.duplicate(camera.getZoomBounds());
		updateZoom();
		updateAmplification();
	}
	public void print(){
		System.out.println(toString());
	}
	public boolean equals(DynamicCamera camera){
		return world.equals(camera.getWorldArea()) && monitor.equals(camera.getMonitorArea());
	}
	
	// Camera interface implementation
	
	@Override
	public void generateQuad(VisibleObject visibleObject){
		String texture = visibleObject.getTextureNE();
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
		
		if((!canSeeObjectWholly(visibleObject)) && canSeeObjectPartially(visibleObject)){
			Rectangle r = visibleObject.getAreaCovered();
			if(r.getXMax() > world.getXMax()){
				quadCorners[1].setX(monitor.getXMax());
				quadCorners[2].setX(monitor.getXMax());
				textureValues[1] = new Location((world.getXMax() - r.getXMin()) / r.getSize().x,1);
				textureValues[2] = new Location((world.getXMax() - r.getXMin()) / r.getSize().x,1);
			}
			if(r.getXMin() < world.getXMin()){
				quadCorners[0].setX(monitor.getXMin());
				quadCorners[3].setX(monitor.getXMin());
				double missingPortion = (world.getXMin() - r.getXMin()) / r.getSize().x;
				textureValues[0].setX((texture != null ? missingPortion : missingPortion));
				textureValues[3].setX((texture != null ? missingPortion : missingPortion));
			}
			if(r.getYMax() > world.getYMax()){
				quadCorners[0].setY(monitor.getYMax());
				quadCorners[1].setY(monitor.getYMax());
				double missingPortion = (r.getYMax() - world.getYMax()) / r.getSize().y;
				textureValues[2].setY((texture != null ? 1 - missingPortion : missingPortion));
				textureValues[3].setY((texture != null ? 1 - missingPortion : missingPortion));
			}
			if(r.getYMin() < world.getYMin()){
				quadCorners[2].setY(monitor.getYMin());
				quadCorners[3].setY(monitor.getYMin());
				double missingPortion = - (r.getYMin() - world.getYMin()) / r.getSize().y;
				textureValues[0].setY((texture != null ? missingPortion : (1 / missingPortion)));
				textureValues[1].setY((texture != null ? missingPortion : (1 / missingPortion)));
			} 
		}
		
		
		Quad quad = new Quad(quadCorners, textureValues);
		quad.texture = visibleObject.getTextureInfo().namePath;
		quad.color = visibleObject.getColor();
		getQuadList().add(quad);
	}
	
	@Override
	public Actor unitAt(Location monitorLocation) {
		Location location = getWorldLocation(monitorLocation);
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
	public void handleMouseLocation(Location monitorLocation) {
		if(monitorLocation.x > panningRange && monitorLocation.x < monitor.getXMax() - panningRange 
		&& monitorLocation.y > panningRange && monitorLocation.y < monitor.getYMax() - panningRange){
			resetSpeed();
			return;
		}
		if(monitorLocation.x < panningRange)moveWorld(-getSpeed(), 0);
		else if(monitorLocation.x > monitor.getXMax() - panningRange)moveWorld(getSpeed(),0);
		if(monitorLocation.y < panningRange)moveWorld(0,getSpeed());
		else if(monitorLocation.y > monitor.getYMax() - panningRange)moveWorld(0, -getSpeed());
	}

	@Override
	public Location getWorldLocation(Location locationOnScreen) {
		Location l = getRelativeScreenLocation(locationOnScreen);
		l.magnify(amplification.getReciprocal());
		Location worldLocation = new Location();
		worldLocation.duplicate(world.getCenter());
		worldLocation.change(l.x, -l.y);
		return worldLocation;
	}

	@Override
	public ArrayList<VisibleObject> getQualifiedObjects() {
		ArrayList<VisibleObject> potential = engine.qt.retrieveBox(world.getXMin() - 100, world.getYMin() - 100, world.getXMax() + 100, world.getYMax() + 100),
								 real = new ArrayList<VisibleObject>();
		for(VisibleObject vo:potential)
			if(canSeeObjectPartially(vo))
				real.add(vo);
		return real;
	}
	@Override
	public void handleMouseWheel(int change, Location locationOnScreen){
		handleMouseWheel(change);
		if(centralZoom)return;
		Location locChange = getWorldLocation(locationOnScreen);
		locChange.revert(world.getCenter());
		locChange.magnify(!holdPointZoom ? zoomMouseImpact : (change < 0 ? defaultZoomAmount.x - 1 : -(defaultZoomAmount.y - 1)));
		moveWorld(locChange);
	}
	@Override
	public boolean coversMonitorLocation(Location locationOnScreen) {
		return isMonitorBound(locationOnScreen);
	}
	@Override
	public void handleKeyPress(KeyEvent keyEvent) {
		// First press goes to the location
		if(locationBinds.containsKey(keyEvent.getKeyCode()) && !world.getCenter().equals(locationBinds.get(keyEvent.getKeyCode())))
			world.setCenter(locationBinds.get(keyEvent.getKeyCode()));
		// Second press goes to the object
		else if(objectBinds.containsKey(keyEvent.getKeyCode()))
			world.setCenter(objectBinds.get(keyEvent.getKeyCode()).getLocation());
		
		switch(keyEvent.getKeyCode()){
		case KeyEvent.VK_RIGHT:
			moveWorld(getSpeed()*3,0);
			break;
		case KeyEvent.VK_LEFT:
			moveWorld(-getSpeed()*3,0);
			break;
		case KeyEvent.VK_UP:
			moveWorld(0,-getSpeed()*3);
			break;
		case KeyEvent.VK_DOWN:
			moveWorld(0, getSpeed()*3);
			break;
		}
	}
	@Override
	public ArrayList<Quad> getQuadList() {
		return quads;
	}
}
