package com.spartanlaboratories.engine.game;

import java.io.IOException;
import java.util.ArrayList;

import com.spartanlaboratories.engine.structure.Engine;
import com.spartanlaboratories.engine.util.Location;
import com.spartanlaboratories.util.Constants;

/**
 * The Creep object is a special type of alive that is meant to behave like a mindless monster or "creep"
 * that simply follows a set of directions.
 * @author Spartak
 * @since Pre-A
 */
public class Creep extends Alive{
	private boolean[] checkPoints = new boolean[engine.map.numberOfMovePoints];
	/**
	 * An ArrayList that contains all of the currently active creeps
	 */
	public static ArrayList<Creep> allCreeps = new ArrayList<Creep>();
	/**
	 * A constants that is used as the default for the size and width of creeps
	 */
	public static final int creepSize = 25;
	private Location[] movePoints;
	private MovementRule creepMovementRule;
	private static MovementRule globalMovementRule;
	private AggressionRule aggressionRule;
	private int followsRuleSet;
	private Location sentry;
	private CMPRule cmprule;
	/**
	 * A subset of rules for creeps to follow if their movement type is constant move points.
	 * @author Spartak
	 * @since A1
	 */
	public enum CMPRule{
		/***/CYCLE, DIEFREE, RANDOMCYCLE, RANDOMDIE, RANDOMNOREPEAT,DIEBOUND,;
		public void set(Creep c){
			c.cmprule = this;
		}
	}
	/**
	 * The type of movement that a creep has.
	 * @author Spartak
	 * @since A1
	 */
	public enum MovementRule{
		/**Creep will not reconfigure its movement target unless explicitly told to do so externally.*/
		NONE, 
		/**Creep will store its current location as a location to return to. It might leave it if it aggros on something of if it has another movement
		 * target but will always return to the sentry(guard) point. */
		SENTRY,
		/**Creep will do its best to move randomly*/
		RANDOM, 
		/**Creep will follow a set of constant move points. Its behaviour after completing the set is configured by {@link Creep.CMPRule}.*/
		CMP, 
		/**Makes the creep follow a CMP rule set and sets {@link Creep.CMPRule} to {@link CMPRule#CYCLE}*/
		PATROL,;
		/**
		 * Configures the passed in Creep to have this movement rule.
		 * @param c The Creep that will have its movement rule changed to this one.
		 */
		public void set(Creep c){
			c.creepMovementRule = this;
			switch(this){
			case CMP:
				c.keepTarget = false;
				CMPRule.DIEFREE.set(c);
				c.movePoints = c.engine.map.movePoints[c.faction.ordinal()][c.followsRuleSet];
				c.setTarget(c.getNextMovePoint());
				break;
			case NONE:
				c.setTarget(null);
				break;
			case SENTRY:
				c.setTarget(c.getLocation());
				c.sentry.duplicate(c.target);
				c.keepTarget = false;
				break;
			case RANDOM:
				c.keepTarget = false;
				break;
			case PATROL:
				CMP.set(c);
				CMPRule.CYCLE.set(c);
				break;
			}
		}
	}
	protected enum AggressionRule{
		COWARD, GANDHI, RETALIATION, MASSMURDER;
	}
	public Creep(Engine engine, Faction setFaction) {
		super(engine, setFaction);
		setWidth(Creep.creepSize);
		setHeight(Creep.creepSize);
		changeStat("max health", 300);
		changeStat("health", 300);
		changeBaseSpeed(250);
		needToMove = false;
		childSetsOwnMovement = false;
		changeStat("visibility range", 450);
		changeStat("attack range", 12);
		changeStat("starting damage", 30);
		changeStat("experience given", 62);
		changeStat("health regen", 0.3);
		changeStat("gold given", ((int)(Math.random() * 8)) + 36); 
		setColor("white");
		allCreeps.add(this);
		initializeWithDefaultRules();
		try {
			setTexture();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public boolean tick(){
		creepAI();
		return super.tick();
	}
	private void configureMovement(){
		switch(creepMovementRule){
		case CMP:case PATROL:
			if(reachedPoint())
				checkPoint();
			setTarget(getNextMovePoint());
			break;
		case NONE:
			break;
		case RANDOM:
			if(reachedTarget())
				target.setCoords(getLocation().x + Math.random()*100, getLocation().y + Math.random() * 100);
			break;
		case SENTRY:
			if(target == null)target = sentry;
			break;
		}
	}
	private void configureAggro(){
		switch(aggressionRule){
		case COWARD: case GANDHI: case RETALIATION:
			break;
		case MASSMURDER:
			aggroOn(checkForPotentialAttackTarget());
			break;
		}
	}
	private Alive checkForPotentialAttackTarget(){
		Alive potentialAttackTarget = null;
		for(Alive a: Alive.allAlives){
			if(a.faction != this.faction 
			&& engine.util.getRealCentralDistance(this, a) < getStat("visibility range")){
				if(potentialAttackTarget == null)potentialAttackTarget = a;
				else if(engine.util.getRealCentralDistance(potentialAttackTarget, this)>
						engine.util.getRealCentralDistance(a, this))
							potentialAttackTarget = a;
			}
		}
		return potentialAttackTarget;
	}
	private Location getNextMovePoint(){
		for(int i = 0; i < engine.map.numberOfMovePoints; i++)
			if(!checkPoints[i])
				return movePoints[i];
		for(int i = 0; i < checkPoints.length;i++)
			checkPoints[i] = false;
		switch(cmprule){
		case CYCLE:
			return getNextMovePoint();
		case DIEBOUND:
			sentry.duplicate(target);
		case DIEFREE:
			return keepTarget?target:null;
		default: return null;
		}
	}
	/**
	 * Marks the next not reached move point as reached
	 */
	private void checkPoint(){
		for(int i = 0; i < engine.map.numberOfMovePoints; i++)
			if(!checkPoints[i]){
				checkPoints[i] = true;
				return;
			}
	}
	private void creepAI(){
		Alive pat = checkForPotentialAttackTarget();
		if(engine.util.everySecond(2))
			reAggro();
		if(getAttackState() == Alive.AttackState.NONE)
			configureMovement();
	}
	public void reAggro(){
		Alive pat = checkForPotentialAttackTarget();
		if(getAttackTarget() != null && getAttackTarget().alive && !lostTarget() && !isCloserThanAttackTarget(pat))
			return;
		aggroOn(pat);
	}
	private boolean lostTarget(){
		return !canSee(getAttackTarget()) && isInForgetfulState();
	}
	private boolean isCloserThanAttackTarget(Alive alive){
		return engine.util.getRealCentralDistance(this, alive) < engine.util.getRealCentralDistance(this, getAttackTarget());
	}
	private boolean reachedPoint(){
		Location movePoint = getNextMovePoint(); 				// Gets what the next move point is.
		return movePoint == null ? true :						// If the move point is null returns true (reached point)
		getLocation().x < movePoint.x + getWidth() / 2			// If the move point is not null
		&& getLocation().x > movePoint.x - getWidth() / 2		// Then performs calculations to see if it was reached
		&& getLocation().y < movePoint.y + getHeight() / 2		// And if reached returns true (reached point)
		&& getLocation().y > movePoint.y - getHeight() / 2;		// Or if not reached the false (did not reach point)
	}
	/**
	 * Returns a new Creep that is a copy of this Creep
	 * 
	 * @category ObjectMethodOverrides
	 * @return c a new Creep that is a copy of this Creep
	 * @see #copyTo(Creep)
	 */
	public Creep copy(){
		Creep c = new Creep(engine, faction);
		copyTo(c);
		return c;
	}
	protected void copyTo(Creep c){
		super.copyTo(c);
		c.checkPoints = new boolean[checkPoints.length];
		for(int i = 0; i < checkPoints.length; i++)
			c.checkPoints[i] = checkPoints[i];
		creepMovementRule.set(c);
		c.aggressionRule = aggressionRule;
		cmprule.set(c);
		c.sentry.duplicate(sentry);
		c.followsRuleSet(followsRuleSet);//will set both followsRuleSet and movePoints
	}
	public void initializeWithDefaultRules(){
		if(globalMovementRule != null)globalMovementRule.set(this);
		else MovementRule.NONE.set(this);
	}
	public void setRule(MovementRule movementRule){
		movementRule.set(this);
	}
	public static void setGlobalRule(MovementRule movementRule){
		globalMovementRule = movementRule;
	}
	/**
	 * Makes this creep follow the passed in set of 
 	 *	constant movement point rules.
	 * @param ruleSet this Creep's new set of cmp rules 
	 */
	public void followsRuleSet(int ruleSet){
		followsRuleSet = ruleSet;
		movePoints = engine.map.movePoints[faction.ordinal()][followsRuleSet];
		checkPoints = new boolean[getRealArraySize(movePoints)];
	}
	public void setCMPRule(CMPRule cmpRule){
		cmpRule.set(this);
	}
	private int getRealArraySize(Object[] array){
		int count = 0;
		for(Object o:array)
			count+=o!=null?1:0;
		return count;
	}
}
