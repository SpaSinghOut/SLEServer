package com.spartanlaboratories.engine.structure;

import com.spartanlaboratories.engine.game.Actor;
import com.spartanlaboratories.engine.game.Alive;
import com.spartanlaboratories.engine.game.Missile;
import com.spartanlaboratories.engine.game.VisibleObject;
import com.spartanlaboratories.engine.util.Location;
import com.spartanlaboratories.util.Constants;
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
		return first.getLocation().x < second.getLocation().x + second.getWidth() / 2 + first.getWidth() / 2 
			&& first.getLocation().x > second.getLocation().x - second.getWidth() / 2 - first.getWidth() / 2 
			&& first.getLocation().y < second.getLocation().y + second.getHeight() / 2 + first.getHeight() / 2 
			&& first.getLocation().y > second.getLocation().y - second.getHeight() / 2 - first.getHeight() / 2;
	}
	public boolean everySecond(double secondRate){
		return engine.tickCount % (engine.getTickRate() * secondRate) == 0;
	}
	public double getXDistance(Actor a, Actor b){
		return Math.abs(a.getLocation().x- b.getLocation().x);
	}
	public double getXDistance(Actor a, Location target) {
		return Math.abs(a.getLocation().x - target.x);
	}
	public double getYDistance(Actor a, Actor b){
		return Math.abs(a.getLocation().y - b.getLocation().y);
	}
	public double getYDistance(Actor a, Location target) {
		return Math.abs(a.getLocation().y - target.y);
	}
	public double getLongestAxialCentralDistance(Actor a, Actor b){
		double xDist = getXDistance(a,b);
		double yDist = getYDistance(a,b);
		return xDist > yDist ? xDist : yDist;
	}
	public double getRealCentralDistance(Actor a, Actor b){
		return Math.hypot(getXDistance(a,b),getYDistance(a,b));
	}
	public double getDistanceFromCenter(Actor a, Location l){
		return Math.hypot(getXDistance(a,l),getYDistance(a,l));
	}
	public double getDistanceTangent(Actor a, Actor b){
		return getYDistance(a,b) / getXDistance(a,b);
	}
	public boolean missileDeath(Missile a) {
		for(Actor b : engine.allActors)
			if(b != null && b != a && b != a.parent && b.solid && checkForCollision(a, b))
				return true;
		return false;
	}
	public boolean checkPointCollision(VisibleObject a, Location l){
		return l.x > a.getLocation().x - a.getWidth() / 2
			&& l.x < a.getLocation().x + a.getWidth() / 2
			&& l.y > a.getLocation().y - a.getHeight() / 2
			&& l.y < a.getLocation().y + a.getHeight() / 2;
	}
}
