package com.spartanlaboratories.engine.structure;

import com.spartanlaboratories.engine.util.Location;

public final class Quad {
	public String texture;
	public String color;
	public Location[] quadValues = new Location[4], textureValues = new Location[4];
	public Quad(Location[] quadValues, Location[] textureValues){
		this.quadValues = quadValues;
		this.textureValues = textureValues;
	}
}
