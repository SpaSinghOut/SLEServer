package com.spartanlaboratories.engine;

import com.spartanlaboratories.engine.game.Ability;
import com.spartanlaboratories.engine.game.Hero;
import com.spartanlaboratories.engine.structure.Controller;
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
		testMultiplayer(engine);
		//testSinglePlayer(engine);
	}
	private static void testMultiplayer(Engine engine){
		engine.goMulti(new UnitTest(engine),numberOfPlayers);
		//engine.goMultiTest(new UnitTest(engine));
		start(engine);
	}
	private static void testSinglePlayer(Engine engine){
		engine.goSingle(new UnitTest(engine));
		start(engine);
	}
	private static void start(Engine engine){
		engine.tracker.initialize(Tracker.TrackerPreset.PRESET_TICK);
		engine.run();
	}
	public UnitTest(Engine engine) {
		super(engine);
		// TODO Auto-generated constructor stub
	}
	@Override
	protected void update() {
		// TODO Auto-generated method stub
		
	}
	@Override
	protected void playerStartAction(Controller controller) {
		Hero<Spell> hero = new Hero<Spell>(engine, controller);
		hero.setTexture("res/test.png");
		hero.setSize(60,60);
		hero.setLocation(0,0);
		hero.setColor("white");
		controller.addUnit(hero);
		
		if(!Human.class.isAssignableFrom(controller.getClass()))return;
		
		//Waits until the human has information about the screen size
		pauseUntilScreenReady((Human)controller);
		
		DynamicCamera mainCamera = new DynamicCamera(engine, ((Human)controller).getScreenSize());
		
		mainCamera.world.setLocation(new Location(0,0));
		//mainCamera.setDrawArea(new Rectangle(new Location(1000,500),new Location(1000,500)));
		//mainCamera.setMonitorSize(new Location(1000,500));
		mainCamera.zoom.setDefaultZoomAmount(1.1);
		mainCamera.zoom.followMouseOnZoom(true);
		//mainCamera.setZoomMouseImpact(.1);
		mainCamera.zoom.holdPointOnZoom(true);
		mainCamera.zoom.setZoomBounds(0.1,10);
		
		mainCamera.pan.setCameraAcceleration(2);
		mainCamera.pan.setCameraSpeed(30);
		mainCamera.pan.setPanningRange(60);
		
		((Human)controller).addCamera(mainCamera);
		new Spell("fireball",hero);
	}
}
class Spell extends Ability{

	public Spell(String abilityName, Hero<Spell> setOwner) {
		super(abilityName, setOwner);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void cast() {
		System.out.println("test");
		owner.goTo(new Location(owner.getLocation().x + 100,owner.getLocation().y + 100));
	}
	
}
