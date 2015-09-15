package com.spartanlaboratories.engine.game;

import java.util.ArrayList;

import com.spartanlaboratories.engine.structure.Engine;

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
}
