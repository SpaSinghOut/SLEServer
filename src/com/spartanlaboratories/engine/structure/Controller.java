package com.spartanlaboratories.engine.structure;

import java.util.ArrayList;

import com.spartanlaboratories.engine.game.Actor;
import com.spartanlaboratories.engine.game.Alive;
import com.spartanlaboratories.engine.game.Alive.Faction;
/**
 * A class that controls units. Can be a human player or AI
 * @author spart_000
 *
 */
public abstract class Controller extends StructureObject{
	/** An Alive object that this controller currently has selected. This will be the target of unit-targeting abilities and other controller and hero behaviors.
	 */
	public Actor selectedUnit;
	protected ArrayList<Actor> controlledUnits = new ArrayList<Actor>();
	int respawnTimer;
	/**
	 * The faction to which this controller currently belongs.
	 */
	public Alive.Faction faction;
	/**
	 * <h1>The Controller Constructor</h1>
	 * Creates a new controller owned by the passed in engine and belonging to the passed in faction. Sets the selected unit to be the controller's hero.
	 * @param engine the game engine
	 * @param setFaction the faction which this controller will belong to
	 */
	public Controller(Engine engine, Faction setFaction){
		super(engine);
		faction = setFaction;
		engine.controllers.add(this);
	}
	/**
	 * Makes this object "update" or perform one set of its regular operations.
	 */
	public abstract void tick();
	/**
	 * Starts a countdown timer that will cause the respawn of this controller's hero when it reaches 0.
	 * @param i the time that is remaining until the respawn of the hero in seconds
	 */
	public void setRespawnTimer(int i) {
		respawnTimer = i * (int)Engine.getTickRate();
	}
	public void addUnit(Actor actor){
		controlledUnits.add(actor);
		actor.owner = this;
	}
	public void removeUnit(Actor actor){
		controlledUnits.remove(actor);
		actor.owner = null;
	}
	public Actor[] controlledUnits(){
		Actor[] controlledUnits = new Actor[this.controlledUnits.size()];
		for(int i = 0; i < controlledUnits.length; i++)
			controlledUnits[i] = this.controlledUnits.get(i);
		return controlledUnits;
	}
	public void setSelectedUnit(Actor actor){
		selectedUnit = actor;
	}
}
