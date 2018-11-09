
package com.spartanlaboratories.engine.game;

import java.io.IOException;
import java.util.ArrayList;

import com.spartanlaboratories.engine.structure.Engine;
import com.spartanlaboratories.engine.util.Location;
import com.spartanlaboratories.measurements.Rectangle;

public class VisibleObject extends GameObject{
	public Shape shape;
	private String color;
	public String defaultColor;
	public boolean solid;
	public boolean immobile;
	public boolean resetTexture;
	private double height;
	private double width;
	private String texture;
	private final Rectangle areaCovered = new Rectangle(new Location(), new Location(0,0));
	ArrayList<Effect> effects = new ArrayList<Effect>(); // Maybe unused
	private TextureInfo textureInfo = new TextureInfo();
	public enum Shape{
		QUAD, TRI,;
	}
	public class TextureInfo{
		TextureInfo(){
			namePath = null;
		}
		public boolean updateNeeded;
		public String textureFormat;
		public String namePath;
	}
	public TextureInfo getTextureInfo(){
		return textureInfo;
	}
	public VisibleObject(Engine engine){
		super(engine);
		engine.visibleObjects.add(this);
		shape = Shape.QUAD;
		textureInfo = new TextureInfo();
	}
	@Override
	public boolean tick(){
		if(textureInfo.updateNeeded || resetTexture)updateTexture();
		return super.tick();
	}
	/**
	 * Performs actions that should be taken every time the engine is updated. Does nothing at this
	 * level in the game object tree and is meant to be overridden by subclasses that wish to perform 
	 * some sort of action every time the game updates. It is preferable that this method is the one
	 * that is being overriden by subclasses rather than {@link #tick()} which should be left to the 
	 * engine.
	 */
	@Override
	public void update() {
		// TODO Auto-generated method stub
		
	}
	/** Returns the width of this object. 
	 * 
	 * @return -the width of this object.
	 * @see #setWidth(double)
	 * @see #getHeight()
	 */
	public double getWidth() {
		return width;
	}
	/**Sets the width value of this object. The width value cannot be accessed directly and must be 
	 * changed through this function. It is important that the width of a new {@link VisibleObject}
	 * is set because otherwise it will default to a value of 0 and cause the entire object to not 
	 * be visible.
	 * 
	 * @param width - The new width of this {@link VisibleObject}
	 * @see #getWidth()
	 * @see #setHeight(double)
	 */
	public void setWidth(double width) {
		this.width = width;
		areaCovered.setSize(new Location(width, height));
	}
	/**
	 * Returns the height value of this {@link VisibleObject}
	 * @return the height value of this {@link VisibleObject}
	 * @see #setHeight(double)
	 * @see #getWidth()
	 */
	public double getHeight() {
		return height;
	}
	public Rectangle getAreaCovered(){
		return areaCovered.copy();
	}
	/** Sets the height value of this object. The height value cannot be accessed directly and must be 
	 * changed through this function. It is important that the height of a new {@link VisibleObject}
	 * is set because otherwise it will default to a value of 0 and cause the entire object to not 
	 * be visible.
	 * 
	 * @param height - The new height of this object.
	 * @see #setWidth(double)
	 * @see #getHeight()
	 */
	public void setHeight(double height) {
		this.height = height;
		areaCovered.setSize(new Location(width, height));
	}
	/**
	 * Sets this object's texture to a resource that is found by using the information given
	 * by the parameters. The first argument should be the format of the texture (the file extension) and the second parameter should be the full name and
	 * location of the texture file. 
	 * @param format - The format of the texture.
	 * @param pathName - The full location and file name
	 * @return true
	 */
	public boolean setTexture(String format, String pathName){
		textureInfo.updateNeeded = true;
		textureInfo.textureFormat = format;
		textureInfo.namePath = pathName;
		return true;
	}
	/**
	 * Attempts to set the texture of this object 
	 * by using the string that was passed in as the location and name of the file.
	 * <p>
	 * Example: if the full name of the file (including the extension) is "test.jpg" and it is inside a folder named "resources" then the string
	 * that should be passed in as an argument when calling this function should be "resources/test.jpg".
	 * @param pathName - A String objects that represents the location and name of the texture.
	 * @return A boolean value that represents whether or not the function succeeded at setting the 
	 * texture.
	 */
	public boolean setTexture(String pathName){
		if(!pathName.contains("."))return false;
		String txt = "";
		for(int i = pathName.indexOf(".") + 1;i < pathName.length();)
			txt += pathName.toCharArray()[i++];
		return setTexture(txt, pathName);
	}
	/**
	 * Returns this object's texture value if it has been initialized. If it has not then this method
	 * will return a blank white texture.
	 * 
	 * @return - This object's texture.
	 */
	public String getTexture(){
		/*String rTexture = texture;
		try {
			rTexture = texture == null ? TextureLoader.getTexture("JPG", ResourceLoader.getResourceAsStream("res/black.jpg")) : texture;
		} catch (IOException e) {
			System.out.println("getTexture exception");
		}*/
		return texture;
	}
	/**
	 * Returns a copy of this visible object. Might not work perfectly if this object contains 
	 * instances of other objects as those objects themselves might get referenced and not copied.
	 */
	public VisibleObject copy(){
		VisibleObject vo = new VisibleObject(engine);
		super.copyTo(vo);
		copyTo(vo);
		return vo;
	}
	/** 
	 * Changes the color of this object back to its default value.
	 * 
	 * @see #color
	 * @see #defaultColor
	 */
	public void resetColor(){
		color = defaultColor;
	}
	public String getColor(){
		return new String(color != null ? color : "white");
	}
	public void setColor(String color){
		this.color = color;
	}
	
	protected void copyTo(VisibleObject vo){
		super.copyTo(vo);
		vo.height = height;
		vo.shape = shape;
		vo.width = width;
		vo.texture = texture;
		vo.color = getColor();
		vo.defaultColor = new String(defaultColor);
		vo.solid = solid;
		vo.immobile = immobile;
		for(Effect e:effects)vo.effects.add(e);
		vo.resetTexture = resetTexture;
	}
	@Override
	protected void updateComponentLocation(){
		areaCovered.setCenter(getLocation());
	}
	protected void setTexture() throws IOException{
		if(this.getClass() == Hero.class){
			String heroNameString = ((Hero)this).heroType.toString().toLowerCase();
			if(heroNameString != "none")
				setTexture("/res/" + heroNameString + ".jpg");
		}
		else if(this.getClass() == Creep.class)
			setTexture("/res/radiant creep.png");
		else if(this.getClass() == Tower.class)
			if(((Tower)this).faction == Alive.Faction.RADIANT)
				setTexture("/res/radiant tower.jpg");
			else if(((Tower)this).faction == Alive.Faction.DIRE)
				setTexture("/res/dire tower.png");
	}
	private void updateTexture(){
		texture = textureInfo.namePath;//TextureLoader.getTexture(textureInfo.textureFormat.toUpperCase(), ResourceLoader.getResourceAsStream(textureInfo.namePath));
		resetTexture = false;
		textureInfo.updateNeeded = false;
	}
	public void trashComponents(){}
	public void setSize(double width, double height){
		setHeight(height);
		setWidth(width);
	}
	public void setSize(Location size){
		setWidth(size.x);
		setHeight(size.y);
	}
	public void setSize(double size){
		setHeight(size);
		setWidth(size);
	}
	public Location getSize(){
		return new Location(getHeight(), getWidth());
	}
	public String getTextureNE() {
		return texture;
	}
	@Override
	public String toString() {
		return areaCovered.toString() + "\nColor: " + color + "\n Texture: " + texture;
	}
}
