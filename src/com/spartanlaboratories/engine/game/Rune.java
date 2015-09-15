package com.spartanlaboratories.engine.game;

import com.spartanlaboratories.engine.structure.Engine;

public class Rune extends VisibleObject{
	PowerType powerType;
	public Rune(Engine engine, PowerType setPowerType){
		super(engine);
		setWidth(40); setHeight(40);
		solid = false;
		powerType = setPowerType;
		shape = Actor.Shape.TRI;
		setColor("yellow");
	}
	public enum PowerType{
		NUKE, EXTRALIFE, DOUBLEDAMAGE, HASTE, FRIDGE;
	}
	public void use(Hero hero){
		active = false;
		engine.addToDeleteList(this);
	}
}
