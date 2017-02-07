package com.spartanlaboratories.engine.game;

import java.util.ArrayList;

import com.spartanlaboratories.engine.structure.Engine;
import com.spartanlaboratories.engine.util.Location;

public class TerrainObject extends VisibleObject{
	public static final int defaultTerrainSize = 30;
	static final String defaultColor = "purple";
	public static final ArrayList<TerrainObject> allTerrain = new ArrayList<TerrainObject>();
	public TerrainObject(Engine engine){
		super(engine);
		immobile = true;
		solid = true;
		setWidth(defaultTerrainSize);
		setHeight(defaultTerrainSize);
		setColor(defaultColor);
		allTerrain.add(this);
		setTexture("res/black.jpg");
	}
	public TerrainObject(Engine engine, Location topLeft, Location bottomRight){
		this(engine);
		double  locX 	= (bottomRight.x + topLeft.x) / 2,
				locY 	= (bottomRight.y + topLeft.y) / 2,
				width	=  bottomRight.x - topLeft.x,
				height	=  bottomRight.y - topLeft.y;
		setLocation(locX,locY);
		setSize(width, height);
	}
}
