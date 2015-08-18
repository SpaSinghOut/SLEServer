package com.spartanlaboratories.engine.game;

import com.spartanlaboratories.engine.structure.StandardCamera;
import com.spartanlaboratories.engine.structure.Engine;
import com.spartanlaboratories.engine.structure.Util;

public class TerrainObject extends VisibleObject{
	public static final int defaultTerrainSize = 30;
	static final String defaultColor = "purple";
	public TerrainObject(Engine engine){
		super(engine);
		immobile = true;
		solid = true;
		setWidth(defaultTerrainSize);
		setHeight(defaultTerrainSize);
		setColor(defaultColor);
	}
}
