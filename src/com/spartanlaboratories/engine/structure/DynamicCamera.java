package com.spartanlaboratories.engine.structure;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;

import com.spartanlaboratories.engine.game.Actor;
import com.spartanlaboratories.engine.game.VisibleObject;
import com.spartanlaboratories.engine.util.Location;
import com.spartanlaboratories.measurements.Rectangle;

/**
 * 
 * @category general
 * @author Spartak
 *
 */
public class DynamicCamera extends StructureObject implements Camera{
	// FIELD DECLARATIONS
	private ArrayList<Quad> quads = new ArrayList<Quad>();
	private Rectangle monitor;
	private final Location standardZoomValues = new Location();
	private final Location zoomBind = new Location();
	private Location zoomLevel = new Location();
	private Location amplification = new Location();
	private HashMap<Number, VisibleObject> objectBinds = new HashMap<Number, VisibleObject>();
	private HashMap<Number, Location> locationBinds = new HashMap<Number, Location>();
	private Location defaultZoomAmount = new Location();
	private double zoomMouseImpact;
	private boolean acceleratedVOSearch;
	public final Zoom zoom = new Zoom();
	public final Pan pan = new Pan();
	public final World world = new World();
	public class Zoom{
		private boolean central, holdPoint;
		private Zoom(){}
		public void setCentralZoom(boolean flag){
			central = flag;
		}
		public void followMouseOnZoom(boolean flag){
			central = !flag;
		}
		public void holdPointOnZoom(boolean flag){
			holdPoint = flag;
		}
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
			world.magnify(amplitude);
			if(!updateZoom())
				world.magnify(amplitude.getReciprocal());
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
			world.magnify(new Location(ratio, ratio).getReciprocal());
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
	}
	public class Pan{
		private double speed, additionalSpeed, acceleration, panningRange;
		private Pan(){}
		public void setCameraSpeed(double speed){
			this.speed = speed / (double)Engine.getTickRate();
		}
		public void setCameraAcceleration(double acceleration){
			this.acceleration = acceleration / (double)Engine.getTickRate();
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
	}
	public class World{
		private final Rectangle world = new Rectangle(new Location(), new Location());
		private World(){}
		public void set(Rectangle world){
			this.world.duplicate(world);
		}
		// BASIC GETTERS AND SETTERS
		/**
		 * Returns a new {@link Rectangle} that represents the area of the map that can currently be seen by the camera
		 * 
		 * @category getter
		 * @return a Rectangle that represents the world area
		 */
		public Rectangle get(){
			return world.copy();
		}
		/**
		 * Sets where on the map this camera is located. The parameter will be the new absolute location of the camera, if the new location
		 * needs to be relative to the current location of the camera then use {@link #move(Location)} instead.
		 * 
		 * @category setter
		 * @param location the location on the map at which this camera is to be placed.
		 */
		public void setLocation(com.spartanlaboratories.measurements.Location location){
			world.setCenter(location);
		}
		private void setStats(Location center, Location size){
			set(new Rectangle(center, size.x, size.y));
		}
		/**
		 * @return
		 * @see com.spartanlaboratories.measurements.Rectangle#copy()
		 */
		public Rectangle copy() {
			return world.copy();
		}
		/**
		 * 
		 * @see com.spartanlaboratories.measurements.Rectangle#clear()
		 */
		public void clear() {
			world.clear();
		}
		/**
		 * @param element
		 * @see com.spartanlaboratories.measurements.Rectangle#copyTo(com.spartanlaboratories.measurements.Rectangle)
		 */
		public void copyTo(Rectangle element) {
			world.copyTo(element);
		}
		/**
		 * @param arg0
		 * @return
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object arg0) {
			return world.equals(arg0);
		}
		/**
		 * @return
		 * @see com.spartanlaboratories.measurements.Rectangle#getSize()
		 */
		public com.spartanlaboratories.measurements.Location getSize() {
			return world.getSize();
		}
		/**
		 * @param location
		 * @see com.spartanlaboratories.measurements.Rectangle#setSize(com.spartanlaboratories.measurements.Location)
		 */
		private void setSize(com.spartanlaboratories.measurements.Location location) {
			world.setSize(location);
		}
		/**
		 * @return
		 * @see com.spartanlaboratories.measurements.Rectangle#getCenter()
		 */
		public com.spartanlaboratories.measurements.Location getCenter() {
			return world.getCenter();
		}
		/**
		 * @return
		 * @see com.spartanlaboratories.measurements.Rectangle#getXMin()
		 */
		public double getXMin() {
			return world.getXMin();
		}
		/**
		 * @return
		 * @see com.spartanlaboratories.measurements.Rectangle#getXMax()
		 */
		public double getXMax() {
			return world.getXMax();
		}
		/**
		 * @return
		 * @see com.spartanlaboratories.measurements.Rectangle#getYMin()
		 */
		public double getYMin() {
			return world.getYMin();
		}
		/**
		 * @return
		 * @see com.spartanlaboratories.measurements.Rectangle#getYMax()
		 */
		public double getYMax() {
			return world.getYMax();
		}
		/**
		 * @param element
		 * @return
		 * @see com.spartanlaboratories.measurements.Rectangle#equals(com.spartanlaboratories.measurements.Rectangle)
		 */
		public boolean equals(Rectangle element) {
			return world.equals(element);
		}
		private void magnify(com.spartanlaboratories.measurements.Location location){
			com.spartanlaboratories.measurements.Location worldSize = world.getSize();
			worldSize.magnify(location);
			setSize(worldSize);
			//setWorldSize(new Location(world.getSize().x * location.x, world.getSize().y * location.y));
		}
		/**
		 * "Moves" the camera by the coordinates in the given location. Similar to {@link #setWorldLocation(Location)} except instead of setting
		 * the absolute location of the camera this method changes the current location by the amount given in the parameter.
		 * 
		 * @category modifier
		 * @param locChange the amount by which the location of this camera changes.
		 */
		public void move(Location locChange){
			Location location = new Location();
			location.duplicate(world.getCenter());
			location.change(locChange);
			setLocation(location);
		}
		public void move(double x, double y){
			move(new Location(x,y));
		}
		// DYNAMIC CAMERA SPECIFIC METHODS
		public boolean isWorldBound(com.spartanlaboratories.measurements.Location northWest){
			return withinWorldXBounds(new Location(northWest)) && withinWorldYBounds(new Location(northWest));
		}
		
	}
	
	/**
	 * Creates a new Dynamic camera. The world and monitor coverage of the camera is going to match that of the arguments.
	 * 
	 * @param engine the game engine
	 * @param world the area of the world that is going to be seen by this camera
	 * @param monitor the area of the monitor that this camera is going to cover up
	 * @category Constructor
	 */
	public DynamicCamera(Engine engine, Rectangle world, Rectangle monitor){
		super(engine);
		this.world.set(world);
		this.monitor = monitor.copy();
		zoomBind.setCoords(0, 1000);
		zoomMouseImpact = 0;
		zoom.setDefaultZoomAmount(1.05);
		zoom.setStandardZoom();
		zoom.updateZoom();
		zoom.zoomOut(1);
	}
	/**
	 * Creates a DynamicCamera object with default properties. The default properties are: the location of the display 
	 * is the center of the monitor, the size
	 * of the display is the size of the monitor, the size of the world display has a 1:1 ratio to the size of the 
	 * display, the world center is (0,0).
	 * 
	 * @param engine the game engine
	 * @param screenSize the size of the screen
	 * @category Constructor
	 */
	public DynamicCamera(Engine engine, Location screenSize){
		this(engine, 	new Rectangle(new Location(), screenSize.x, screenSize.y), 
						new Rectangle(new Location(screenSize.x / 2, screenSize.y / 2), 
						screenSize.x, screenSize.y));
	}
	
	/**
	 * Returns a new {@link Rectangle} that represents the area of the monitor that is currently being drawn on by this camera.
	 * 
	 * @category getter
	 * @return a Rectangle that represents monitor area
	 */
	public Rectangle getMonitorArea(){
		return monitor.copy();
	}
	/**
	 * Sets the center location and the size of the monitor area that is being drawn on by this camera. Warning, this will change the way 
	 * that quads are drawn as their size will adjust to the monitor size. If the goal is to adjust the draw area while keeping the quads
	 * the same size then its best to either use this method in conjunction with {@link Zoom}
	 * or use the method {@link #setDrawArea(Rectangle)} which will adjust the world size automatically.
	 * 
	 * @category setter
	 * @param center the center of the monitor area
	 * @param size the size of the monitor area
	 */
	public void setMonitorStats(Location center, Location size){
		monitor = new Rectangle(center, size.x, size.y);
	}
	
	/**
	 * Sets the monitor size to the specifed value. Like {@link #setMonitorStats(Location, Location)} this will not have a relative effect 
	 * on the size of the world and will therefore skew drawn elements. {@link Zoom}
	 * could be used in conjunction with this to negate that effect. or {@link #setDrawArea(Rectangle)} could be used instead as it automatically
	 * scales the world to fit the screen as it did before.
	 * 
	 * @category setter
	 * @param newSize
	 */
	public void setMonitorSize(Location newSize){
		monitor.setSize(newSize);
	}
	public void setDrawArea(Rectangle rectangle){
		Location zoomChange = new Location(rectangle.getSize().x / monitor.getSize().x, rectangle.getSize().y / monitor.getSize().y);
		world.magnify(zoomChange);
		zoom.setStandardZoomRelative(zoomChange);
		monitor.duplicate(rectangle);
		zoom.updateZoom();
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
	public Location getRelativeScreenLocation(Location locationOnScreen){
		com.spartanlaboratories.measurements.Location relativeLocation = locationOnScreen.copy();
		relativeLocation.change(monitor.getCenter().getOpposite());
		return new Location(relativeLocation);
	}
	/**
	 * Returns the value of the acceleratedVOSearch variable.
	 *
	 * @category getter
	 * @return the acceleratedVOSearch
	 */
	public boolean isAcceleratedVOSearch() {
		return acceleratedVOSearch;
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
		zoom.zoomOut(new Location((defaultZoomAmount.x - 1) * change + 1, (defaultZoomAmount.y - 1) * change + 1));
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
		double x = human.getScreenSize().x, y = human.getScreenSize().y;
		return	monitor.getXMin() < x 
			&&	monitor.getXMax() > 0 
			&&	monitor.getYMin() > 0 
			&&	monitor.getYMax() < y;
	}
	public boolean canSeeObjectPartially(VisibleObject visibleObject){
		Rectangle area = visibleObject.getAreaCovered();
		return world.isWorldBound(area.northWest)
			|| world.isWorldBound(area.northEast)
			|| world.isWorldBound(area.southWest)
			|| world.isWorldBound(area.southEast);
	}
	public boolean canSeeObjectCenter(VisibleObject visibleObject){
		return world.isWorldBound(visibleObject.getLocation());
	}
	public boolean canSeeObjectWholly(VisibleObject visibleObject){
		Rectangle area = visibleObject.getAreaCovered();
		return withinWorldXBounds(area.getXMin())
			&& withinWorldXBounds(area.getXMax())
			&& withinWorldYBounds(area.getYMin())
			&& withinWorldYBounds(area.getYMax());
	}
	
	// Method overrides and other generic API
	/**
	 * @category Object class overrides
	 */
	public String toString(){
		return "In world: " + world.getCenter() + world.getSize() + " On monitor: " + monitor.getCenter() + monitor.getSize();
	}
	public DynamicCamera copy(){
		return new DynamicCamera(engine, world.get(), monitor);
	}
	public void copyTo(DynamicCamera camera){
		camera.duplicate(this);
	}
	public void duplicate(DynamicCamera camera){
		world.setLocation(camera.world.getCenter());
		world.setSize(camera.world.getSize());
		monitor.setCenter(camera.getMonitorArea().getCenter());
		monitor.setSize(camera.getMonitorArea().getSize());
		standardZoomValues.duplicate(camera.zoom.getStandardZoomValues());
		zoomBind.duplicate(camera.zoom.getZoomBounds());
		zoom.updateZoom();
		zoom.updateAmplification();
	}
	public void print(){
		System.out.println(toString());
	}
	public boolean equals(DynamicCamera camera){
		return world.equals(camera.world.get()) && monitor.equals(camera.getMonitorArea());
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
		Location location = getLocationInWorld(monitorLocation);
		System.out.println("In world: " + location);
		final int searchRange = 200;
		ArrayList<Actor> actors = engine.qt.retriveActors(location.x - searchRange, location.y - searchRange, location.x + searchRange, location.y + searchRange);
		for(Actor a: actors)
			if(engine.util.checkPointCollision(a, location))
				return a;
		System.out.println("did not find actor");
		return null;
	}

	@Override
	public void handleClick(int mouseButton) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleMouseLocation(Location monitorLocation) {
		if(monitorLocation.x > pan.panningRange && monitorLocation.x < monitor.getXMax() - pan.panningRange 
		&& monitorLocation.y > pan.panningRange && monitorLocation.y < monitor.getYMax() - pan.panningRange){
			pan.resetSpeed();
			return;
		}
		if(monitorLocation.x < pan.panningRange)world.move(-pan.getSpeed(), 0d);
		else if(monitorLocation.x > monitor.getXMax() - pan.panningRange)world.move(pan.getSpeed(),0d);
		if(monitorLocation.y < pan.panningRange)world.move(0d,pan.getSpeed());
		else if(monitorLocation.y > monitor.getYMax() - pan.panningRange)world.move(0d, -pan.getSpeed());
	}

	@Override
	public Location getLocationInWorld(Location locationOnScreen) {
		Location l = getRelativeScreenLocation(locationOnScreen);
		l.magnify(amplification.getReciprocal());
		Location worldLocation = new Location();
		worldLocation.duplicate(world.getCenter());
		worldLocation.change(l.x, -l.y);
		return worldLocation;
	}

	@Override
	public ArrayList<VisibleObject> getQualifiedObjects() {
		ArrayList<VisibleObject> potential = 
						acceleratedVOSearch ? 
						engine.qt.retrieveBox(world.getXMin() - 100, world.getYMin() - 100, world.getXMax() + 100, world.getYMax() + 100):
						engine.visibleObjects,
								 real = new ArrayList<VisibleObject>();
		for(VisibleObject vo:potential)
			if(canSeeObjectPartially(vo))
				real.add(vo);
		return real;
	}
	@Override
	public void handleMouseWheel(int change, Location locationOnScreen){
		handleMouseWheel(change);
		if(zoom.central)return;
		Location locChange = getLocationInWorld(locationOnScreen);
		locChange.revert(world.getCenter());
		locChange.magnify(!zoom.holdPoint ? zoomMouseImpact : (change < 0 ? defaultZoomAmount.x - 1 : -(defaultZoomAmount.y - 1)));
		world.move(locChange);
	}
	@Override
	public boolean coversMonitorLocation(Location locationOnScreen) {
		return isMonitorBound(locationOnScreen);
	}
	@Override
	public void handleKeyPress(KeyEvent keyEvent) {
		// First press goes to the location
		if(locationBinds.containsKey(keyEvent.getKeyCode()) && !world.getCenter().equals(locationBinds.get(keyEvent.getKeyCode())))
			world.setLocation(locationBinds.get(keyEvent.getKeyCode()));
		// Second press goes to the object
		else if(objectBinds.containsKey(keyEvent.getKeyCode()))
			world.setLocation(objectBinds.get(keyEvent.getKeyCode()).getLocation());
		
		switch(keyEvent.getKeyCode()){
		case KeyEvent.VK_RIGHT:
			world.move(pan.getSpeed()*3,0);
			break;
		case KeyEvent.VK_LEFT:
			world.move(-pan.getSpeed()*3,0);
			break;
		case KeyEvent.VK_UP:
			world.move(0,-pan.getSpeed()*3);
			break;
		case KeyEvent.VK_DOWN:
			world.move(0, pan.getSpeed()*3);
			break;
		}
	}
	@Override
	public ArrayList<Quad> getQuadList() {
		return quads;
	}
}
