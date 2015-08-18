package com.spartanlaboratories.engine.structure;

import com.spartanlaboratories.engine.game.Actor;
import com.spartanlaboratories.engine.game.Alive;
import com.spartanlaboratories.engine.game.Missile;
import com.spartanlaboratories.engine.game.VisibleObject;
import com.spartanlaboratories.engine.util.Location;
/**
 * <b> The Engine's Utility Class </b>
 * <p>
 * Hold a variety of utility methods that don't have a place within an object.
 *
 * @author Spartak
 *
 */
public final class Util extends StructureObject{
	Util(Engine engine){
		super(engine);
	}
	public boolean checkForCollision(VisibleObject first, VisibleObject second){
		if((first.getLocation().x < (second.getLocation().x + second.getWidth() / 2 + first.getWidth() / 2) &&
			first.getLocation().x > (second.getLocation().x - second.getWidth() / 2 - first.getWidth() / 2)) &&
			(first.getLocation().y < (second.getLocation().y + second.getHeight() / 2 + first.getHeight() / 2)&&
			first.getLocation().y > (second.getLocation().y - second.getHeight() / 2 - first.getHeight() / 2))){
			return true;
		}
		return false;
	}
	public boolean everySecond(double secondRate){
		if(engine.tickCount % (engine.getTickRate() * secondRate) == 0)return true;
		return false;
	}
	public double getXDistance(Actor a, Actor b){
		if(a.getLocation().x > b.getLocation().x)
			return a.getLocation().x
					- b.getLocation().x;
		else if(a.getLocation().x < b.getLocation().x)return b.getLocation().x - a.getLocation().x;
		return 0;
	}
	public double getXDistance(Actor a, Location target) {
		if(a.getLocation().x > target.x){
			return a.getLocation().x
					- target.x;
		}
		else if(a.getLocation().x < target.x){
			return target.x - a.getLocation().x;
		}
		return 0;
	}
	public double getYDistance(Actor a, Actor b){
		if(a.getLocation().y > b.getLocation().y)return a.getLocation().y - b.getLocation().y;
		else if(a.getLocation().y < b.getLocation().y)return b.getLocation().y - a.getLocation().y;
		return 0;
	}
	public double getYDistance(Actor a, Location target) {
		if(a.getLocation().y > target.y){
			return a.getLocation().y - target.y;
		}
		else if(a.getLocation().y < target.y){
			return target.y - a.getLocation().y;
		}
		return 0;
	}
	public double getLongestAxialCentralDistance(Actor a, Actor b){
		double xDist = getXDistance(a,b);
		double yDist = getYDistance(a,b);
		if(xDist > yDist)return xDist;
		else if(yDist > xDist)return yDist;
		return xDist;
	}
	public double getRealCentralDistance(Actor a, Actor b){
		double xDist = getXDistance(a,b);
		double yDist = getYDistance(a,b);
		return Math.hypot(xDist, yDist);
	}
	public double getDistanceFromCenter(Actor a, Location l){
		double xDist = getXDistance(a,l);
		double yDist = getYDistance(a,l);
		return Math.hypot(xDist, yDist);
	}
	public double getDistanceTangent(Actor a, Actor b){
		return getYDistance(a,b) / getXDistance(a,b);
	}
	public boolean missileDeath(Missile a) {
		for(Actor b : engine.allActors){
			if(b != null && b != a && b != a.parent && b.solid && checkForCollision(a, b)){
				if(b.getClass() == Alive.class)if(((Alive)b).getStat(Constants.health) < 0)return false;
				return true;
			}
		}
		return false;
	}
	public boolean checkPointCollision(VisibleObject a, Location l){
		if(l.x > a.getLocation().x - (a.getWidth() / 2) 
		&& l.x < a.getLocation().x + (a.getWidth() / 2)
		&& l.y > a.getLocation().y - (a.getHeight() / 2)
		&& l.y < a.getLocation().y + (a.getHeight() / 2))
		return true;
		return false;
	}
}
