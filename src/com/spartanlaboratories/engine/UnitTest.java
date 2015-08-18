package com.spartanlaboratories.engine;

import com.spartanlaboratories.engine.game.Hero;
import com.spartanlaboratories.engine.structure.DynamicCamera;
import com.spartanlaboratories.engine.structure.Engine;
import com.spartanlaboratories.engine.structure.Human;
import com.spartanlaboratories.engine.structure.Map;
import com.spartanlaboratories.engine.structure.Tracker;
import com.spartanlaboratories.engine.util.Location;

public class UnitTest extends Map{
	private static final int numberOfPlayers = 1;
	public static void main(String[] args){
		Engine engine = new Engine();
		engine.goMultiTest(new UnitTest(engine));
	}
	public UnitTest(Engine engine) {
		super(engine);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void init() {
		engine.tracker.initialize(Tracker.TrackerPreset.PRESET_TICK);
	}
	@Override
	protected void update() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void forConnected(Human human) {
		Hero hero = new Hero(engine, Hero.HeroType.RAZOR, human);
		hero.setTexture("res/test.png");
		hero.setSize(60,60);
		hero.setLocation(0,0);
		hero.setColor("white");
		while(human.getScreenSize().equals(new Location()));
		
		DynamicCamera mainCamera = new DynamicCamera(engine, human.getScreenSize());
		
		mainCamera.setWorldLocation(new Location(0,0));
		//mainCamera.setDrawArea(new Rectangle(new Location(1000,500),new Location(1000,500)));
		//mainCamera.setMonitorSize(new Location(1000,500));
		mainCamera.setDefaultZoomAmount(1.1);
		
		mainCamera.setCameraAcceleration(2);
		mainCamera.setCameraSpeed(30);
		mainCamera.setPanningRange(60);
		
		human.addCamera(mainCamera);
	}
	
}
