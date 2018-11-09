package com.spartanlaboratories.engine.game;

import java.util.ArrayList;

import com.spartanlaboratories.engine.structure.Engine;
import com.spartanlaboratories.measurements.Location;

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
	}
	public TerrainObject(Engine engine, Location topLeft, Location bottomRight){
		this(engine);
		setLocation((topLeft.x + bottomRight.x) / 2, (topLeft.y + bottomRight.y) / 2);
		setSize( bottomRight.x - topLeft.x, topLeft.y - bottomRight.y);
	}
}
