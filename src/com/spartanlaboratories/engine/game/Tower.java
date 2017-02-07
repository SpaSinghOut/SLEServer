package com.spartanlaboratories.engine.game;

import java.io.IOException;

import com.spartanlaboratories.engine.structure.Engine;
import com.spartanlaboratories.engine.util.Location;
import com.spartanlaboratories.util.Constants;

public class Tower extends Alive {
	private static final int towerHP = 1600;
	private static final int towerSize = 85;
	public Tower(Engine engine, Faction setFaction) {
		super(engine, setFaction);
		setWidth(towerSize);
		setHeight(towerSize);
		changePermissions(Constants.movementAllowed, false);
		setColor("white");
		setStat("max health", towerHP);
		setStat("health", getStat("max health"));
		changeStat("visibility range", 700);
		changeStat("attack range", 700);
		immobile = true;
		changeStat("starting damage", 70);
		setStat("base animation time", 1);
		setStat("base attack time", 1);
		changeStat("attack speed", 100);
		missile = true;
		attackMissileType = new MissileStats("auto");
		changePermissions(Constants.autoAttackAllowed, true);
		noRetraction = true;
		try {
			setTexture();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public boolean tick(){
		if(!super.tick())return false;
		if(attackTarget != null)
			if(!attackTarget.active || 
			engine.util.getRealCentralDistance(this, attackTarget) > getStat("visibility range"))
				attackTarget = null;
		if(attackTarget == null || !attackTarget.active || !attackTarget.alive)findAttackTarget();
		return active;
	}
	private void findAttackTarget(){
		Alive potentialAttackTarget = null;
		for(Alive a: Alive.allAlives){
			if(a.faction != this.faction && getStat("visibility range") > engine.util.getRealCentralDistance(a, this)){
				if(potentialAttackTarget == null)potentialAttackTarget = a;
				else{
					if(engine.util.getRealCentralDistance(potentialAttackTarget, this)>
					engine.util.getRealCentralDistance(a, this))
						potentialAttackTarget = a;
				}
			}
		}
		aggroOn(potentialAttackTarget);
	}
	
}
