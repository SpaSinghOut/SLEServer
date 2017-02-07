package com.spartanlaboratories.engine.game;

import java.util.ArrayList;
import java.util.HashMap;

import com.spartanlaboratories.engine.structure.Camera;
import com.spartanlaboratories.engine.structure.Engine;
import com.spartanlaboratories.engine.util.Location;
import com.spartanlaboratories.util.Constants;

public class Alive extends Actor{
	private String[] statStrings = {"health", "mana", "max health", "max mana", "experience", "level", "starting damage", "base damage", "bonus damage",
			"damage", "armor", "evasion", "attack speed", "base attack speed", "base animation time", "base attack time", "animation cooldown", 
			"attack cooldown", "gold", "gold given", "experience given", "visibility range", "attack range", "health regen", "mana regen", 
			"ability points", "retraction cooldown"};
	static int experienceRange = 650;
	boolean invulnerable;
	int invulnerabilityCount;
	double damageMultiplier;
	private HashMap<String, Double> stats = new HashMap<String, Double>();
	public Faction faction;
	protected Alive attackTarget;
	private AttackState attackState;
	protected ArrayList<Buff> buffs = new ArrayList<Buff>();
	public static ArrayList<Alive> allAlives = new ArrayList<Alive>();
	VisibleObject healthBar;
	Alive lastHitter;
	public boolean alive;
	Buff mainUAM;
	boolean missile;
	MissileStats attackMissileType;
	boolean noRetraction;
	protected boolean[] permissions = new boolean[Constants.numberOfPermissions];
	public ItemList inventory;
	private static int statsSize = Constants.getStatsSize();
	public Alive(Engine engine, Faction setFaction){
		super(engine);
		for(String string: statStrings)
			stats.put(string, 0d);
		damageMultiplier = 1;
		shape = Actor.Shape.QUAD;
		faction = setFaction;
		setAttackState(AttackState.NONE);
		needToMove = false;
		initHealthBar();
		alive = true;
		setStat("max health", 1);
		setStat("health", 1);
		allAlives.add(this);
		for(int i = 0; i < permissions.length; i++)
			permissions[i] = true;
		attackOrientedInit();
		resetTexture = false;
		solid = true;
		attackMissileType = new MissileStats("auto");
	}
	protected void initHealthBar(){
		healthBar = new VisibleObject(engine);
		healthBar.solid = false;
		switch(faction){
		case RADIANT:
			healthBar.setColor("green");
			break;
		case DIRE:
			healthBar.setColor("red");
			break;
		case NEUTRAL:
			healthBar.setColor("yellow");
			break;
		}
	}
	public enum AttackState{
		NONE, SELECTED, MOVING, ANIMATION, RETRACTION, WAIT,;
	}
	public enum Direction{
		LEFT, RIGHT, UP, DOWN,;
	}
	public enum Faction{
		RADIANT, DIRE, NEUTRAL,;
	}
	public enum DamageType{
		PHYSICAL, MAGICAL, PURE, UNIVERSAL, HPREMOVAL,;
	}
	public boolean tick(){
		if (invulnerabilityCount-->0);
		else invulnerable = false;
		regen();
		for(Buff buff: getBuffs())if(!buff.tick())engine.addToDeleteList(buff);
		needToMove = permissions[Constants.movementAllowed];
		if(permissions[Constants.autoAttackAllowed])configureAttack();
		if(attackState == AttackState.ANIMATION || 
		attackState == AttackState.RETRACTION || attackState == AttackState.WAIT)
			changePermissions(Constants.movementAllowed, false);
		else changePermissions(Constants.movementAllowed, true);
		alive = getStat("health") > 0;
		updateHealthBar();
		return super.tick() && alive;
	}
	private void updateHealthBar() {
		healthBar.setWidth(getWidth() * getRatio("health"));
		healthBar.setLocation(getLocation().x - getWidth() * (1-getRatio("health")) / 2, healthBar.getLocation().y);
	}
	public void heal(int heal){
		changeStat("health", heal);
	}
	public void setFaction(Faction setFaction){
		faction = setFaction;
	}
	public void dealDamage(Alive attacking, double damageDealt, DamageType setDamageType){
		for(Buff b: getBuffs())
			if(b.activationTrigger == Buff.TriggerType.ONDEALINGDAMAGE)
				b.trigger(attackTarget);
		double calculateRealDamage = damageDealt < 0 ? 0 : damageDealt;
		attacking.takeDamage(this, calculateRealDamage, setDamageType );
	}
	public void takeDamage(Alive attacker, double d, DamageType damageType){
		for(Buff b: getBuffs())
			if(b.activationTrigger == Buff.TriggerType.ONTAKINGDAMAGE)
				b.trigger(attackTarget);
		d *= 1 - ( getStat("armor") * .06 ) / ( 1 + getStat("armor") * .06);
		changeStat("health", -d);
		if(getStat("health") <= 0 && alive){
			lastHitter = attacker;
			die();
			attacker.kill(this);
		}
	}
	final public void setStat(String string, double newValue){
		stats.put(statCheck(string), newValue);
	}
	private String statCheck(String string){
		string = string.trim().toLowerCase();
		if(!stats.containsKey(string))throw new IllegalArgumentException(string);
		return string;
	}
	final protected boolean canSee(Actor seen){
		return (engine.util.getRealCentralDistance(this, seen) < getStat("visibility range"));
	}
	final protected boolean isAttackTargetWithinAttackRange(){
		return (engine.util.getRealCentralDistance(this, attackTarget) < getStat("attack range") + getWidth() / 2 + attackTarget.getWidth() / 2);
	}
	final protected void issueAttack(Alive attacking){
		if(attacking.attackState == AttackState.NONE || attacking.attackState == AttackState.SELECTED ||
		attacking.attackState == AttackState.MOVING)
			if(attacking.faction != this.faction)
		for(Buff b: getBuffs())
			if(b.activationTrigger == Buff.TriggerType.ONATTACKDECLARATION)
				b.trigger(attackTarget);
	}
	final  protected void getTargeted(Alive attacker){
		for(Buff b: getBuffs())
			if(b.activationTrigger == Buff.TriggerType.ONBEINGTARGETED)
				b.trigger(attackTarget);
	}
	protected void doAttack(Alive attacking){
		if(mainUAM != null && mainUAM.activationTrigger == Buff.TriggerType.ORBATTACK)
			mainUAM.trigger(attacking);
		else if(mainUAM != null && mainUAM.activationTrigger == Buff.TriggerType.ORBHIT)
			mainUAM.costTrigger();
		for(Buff b: getBuffs())
			if(b.activationTrigger == Buff.TriggerType.ONATTACK)
				b.trigger(attacking);
		if(missile){
			Missile attackMissile = new Missile(this, attacking);
			attackMissile.setDamage(getStat("damage"));
		}
		else attacking.getAttacked(this);
	}
	protected void getAttacked(Alive attacker){
		for(Buff b: getBuffs())
			if(b.activationTrigger == Buff.TriggerType.ONBEINGATTACKED)
				b.trigger(attacker);
		if(100d * Math.random() > getStat("evasion")){
			attacker.hit(this);
		}
	}
	protected void hit(Alive attacking){
		if(mainUAM != null && mainUAM.activationTrigger != Buff.TriggerType.ORBATTACK)
			this.mainUAM.trigger(attacking);
		for(Buff b: getBuffs())
			if(b.activationTrigger == Buff.TriggerType.ONHIT)
				b.trigger(attacking);
		attacking.getHit(this);
		dealDamage(attacking, getStat("damage"), DamageType.PHYSICAL);
	}
	protected void getHit(Alive attacker){
		for(Buff b: getBuffs())
			if(b.activationTrigger == Buff.TriggerType.ONBEINGHIT)
				b.trigger(attackTarget);
	}
	protected void getSpellTargeted(){
		for(Buff b: getBuffs())
			if(b.activationTrigger == Buff.TriggerType.ONSPELLTARGETED)
				b.trigger(this);
	}
	protected void getSpellAffected(Ability ability, Alive caster){
		for(Buff b: getBuffs())
			if(b.activationTrigger == Buff.TriggerType.ONSPELLAFFECTED)
				b.trigger(caster);		
	}
	protected void changeStat(String stat, double netChange){
		switch(statCheck(stat)){
		case "experience":
			modifyStat(stat, netChange);
			changeStat("level", levelsAchieved());
			break;
		case "level":
			modifyStat(stat, netChange);
			changeStat("ability points", netChange);
			break;
		case "base attack speed":
			modifyStat(stat, netChange);
			changeStat("attack speed", netChange);
			break;
		case "health":
			modifyStat(stat, netChange);
			if(getStat("health") > getStat("max health"))
				setStat("health", getStat("max health"));
			break;
		case "mana":
			modifyStat("mana", netChange);
			if(getStat("mana") > getStat("max mana"))
				setStat("mana", getStat("max mana"));
			else if(getStat("mana") < 0)
				setStat("mana", 0);
			break;
		case "starting damage":
			modifyStat("starting damage", netChange);
			changeStat("base damage", netChange);
			break;
		case "base damage":
			modifyStat("base damage", netChange);
			changeStat("damage", netChange);
			break;
		case "bonus damage":
			modifyStat("bonus damage", netChange);
			changeStat("bonus damage", netChange);
			break;
		case "max health":
			double healthRatio = getRatio("health");
			modifyStat("max health", netChange);
			setStat("health", healthRatio * getStat("max health"));
			break;
		case "max mana":
			double manaRatio = getRatio("mana");
			modifyStat("max mana", netChange);
			setStat("mana", manaRatio * getStat("max mana"));
			break;
		default:
			modifyStat(stat,netChange);
			break;
		}
	}
	private void modifyStat(String stat, double netChange){
		assert stats.containsKey(stat) : stat;
		stats.put(stat, stats.get(stat) + netChange);
	}
	protected double levelsAchieved(){
		final double levelRequirement = getLevelUpExperienceRequirement();
		if(getStat("experience") > levelRequirement){
			changeStat("experince", -levelRequirement);
			return 1 + levelsAchieved();
		}
		return 0;
	}
	protected double getLevelUpExperienceRequirement(){
		return 100 + 100 * getStat("level");
	}
	public void setRanged(boolean isRanged){
		missile = isRanged;
	}
	public double getStat(String string){
		switch(statCheck(string)){
		case "max health":
			return stats.get("max health") > 1 ? stats.get("max health"):1;
		case "max mana":
			return stats.get("max mana") > 1 ? stats.get("max mana") : 1;
		default:
			return stats.get(string);
		}
	}
	public void kill(Alive fallen){
		for(Buff b: getBuffs())
			if(b.activationTrigger == Buff.TriggerType.ONKILL)
				b.trigger(attackTarget);
	}
	public void die(){
		alive = false;
		for(Buff b: getBuffs())
			if(b.activationTrigger == Buff.TriggerType.ONDEATH)
				b.trigger(attackTarget);
		ArrayList<Hero<Ability>> receivers = new ArrayList<Hero<Ability>>();
		for(Alive a : Alive.allAlives){
			if(Hero.class.isAssignableFrom(a.getClass())
			&& a.faction != this.faction 
			&& engine.util.getRealCentralDistance(a, this) < Alive.experienceRange)
				receivers.add((Hero)a);
		}
		if(!receivers.contains(lastHitter) && Hero.class.isAssignableFrom(lastHitter.getClass()))
			receivers.add((Hero)lastHitter);
		for(Hero a: receivers)if(a != null)
			a.changeStat("experience", getStat("experience given") / (receivers.size()));
		double goldGiven = getStat("gold given") * 0.9 + (int)(Math.random() * (getStat("gold given") * 0.2));
		lastHitter.changeStat("gold", goldGiven );
	}
	protected void setAttackState(AttackState setAttackState){
		attackState = setAttackState;
	}
	public AttackState getAttackState(){
		return attackState;
	}
	protected void resetAnimationCD(){
		setStat("animation cooldown", getStat("base attack time")/(getStat("attack speed")/100)
										* (int)engine.getTickRate()*getStat("base animation time"));
	}
	protected void resetRetractionCD(){
		setStat("retraction cooldown", getStat("animation cooldown") / 3);
	}
	protected void resetAttackCD(){
		setStat("attack cooldown", engine.getTickRate() / 5);
	}
	protected void resetAllAttackCDs(){
		this.resetAnimationCD();
		this.resetAttackCD();
		this.resetRetractionCD();
	}
	protected boolean isInForgetfulState(){
		switch(attackState){
		case ANIMATION:
			return false;
		default: return true;
		}
	}
	public float getRatio(String ratioType){
		switch(ratioType.toLowerCase()){
		case "health":
			return (float)(getStat("health") / getStat("max health"));
		case "mana":
			return (float)(getStat("mana") / getStat("max mana"));
		case "animation":
			return (float)(getStat("animation cooldown") / getStat("base animation time"));
		case "experience":
			return (float)(getStat("experience") / getLevelUpExperienceRequirement());
		}
		return 0.0f;
	}
	/**
	 * Makes this Alive consider the passed in Alive as its attack target. Will change the alive's attack state to selected.
	 * Might trigger buffs.
	 * @param setTarget - The Alive that will become the attack target of this Alive
	 */
	public void aggroOn(Alive setTarget){
		if(setTarget == attackTarget)return;
		if(setTarget != null){
			setAttackState(AttackState.SELECTED);
			for(Buff b: getBuffs())
				if(b.activationTrigger == Buff.TriggerType.ONAGGRO)
					b.trigger(setTarget);
		}
		else{
			setAttackState(AttackState.NONE);
			resetAllAttackCDs();
		}
		attackTarget = setTarget;
	}
	public Buff[] getBuffsOfType(String bn){
		Buff[] ba = new Buff[getBuffs().size()];
		int i = 0;
		for(Buff b:getBuffs())if(b.buffName == bn)
			ba[i++] = b;
		return ba;
	}
	public void changePermissions(int permission, boolean allowed){
		switch(permission){
		case Constants.movementAllowed:
			if(allowed)
				for(Buff b:getBuffs())
					if(b.active && b.buffName == "stun")allowed = false;
			permissions[permission] = allowed;
			needToMove = allowed;
			break;
		case Constants.spellCastAllowed:
			if(allowed)for(Buff b:getBuffs())
				if(b.buffName == "stun" || b.buffName == "silence");
				else permissions[permission] = allowed;
			break;
		case Constants.channelingAllowed:
			if(allowed)for(Buff b:getBuffs())
				if(b.buffName == "stun" || b.buffName == "silence");
				else permissions[permission] = allowed;
			break;
		case Constants.autoAttackAllowed:
			if(allowed)for(Buff b:getBuffs())
				if(b.buffName == "stun");
				else permissions[permission] = allowed;
			break;
		default: permissions[permission] = allowed;
		}
	}
	public boolean getPermissions(int permission) {
		return permissions[permission];
	}
	public Alive getAttackTarget() {
		return attackTarget;
	}
	protected boolean hasBuffType(String setBuffType){
		for(Buff b: getBuffs())if(b.buffName == setBuffType)return true;
		return false;
	}
	public boolean hasAttackTarget(){
		return attackTarget != null;
	}

	// ******  MODIFICATION NEEDED!!!! ******
	// the getter method should give a buff array to avoid external list modification
	public ArrayList<Buff> getBuffs() {
		return  buffs;
	}
	public void setWidth(double width){
		super.setWidth(width);
		healthBar.setWidth(width);
	}
	public void setHeight(double height){
		super.setHeight(height);
		healthBar.setHeight((int)(height * .25));
	}
	protected void updateComponentLocation(){
		super.updateComponentLocation();
		healthBar.setLocation(
				getLocation().x - getWidth() / 2 + getRatio("health") * getWidth() / 2,
				getLocation().y - getHeight() / 2 - healthBar.getHeight() / 2);
	}
	public void goTo(Location setTarget){
		super.goTo(setTarget);
		changePermissions(Constants.movementAllowed, true);
	}
	/**
	 * Copies this object
	 * @return A new Alive that is a copy of this Alive
	 */
	public Alive copy(){
		Alive a = new Alive(engine, faction);
		copyTo(a);
		return a;
	}
	protected void copyTo(Alive a){
		super.copyTo(a);
		a.alive = alive;
		a.attackState = attackState;
		a.attackTarget = attackTarget;
		for(Buff b: buffs)a.buffs.add(b);
		a.faction = faction;
		a.invulnerabilityCount = invulnerabilityCount;
		a.invulnerable = invulnerable;
		a.lastHitter = lastHitter;
		a.mainUAM = mainUAM;
		a.missile = missile;
		a.attackMissileType = attackMissileType;
		a.noRetraction = noRetraction;
		a.permissions = permissions;
		for(String key: stats.keySet())a.stats.put(key, stats.get(key));
	}
	@Override
	public void rightClick(Location locationOnScreen, Camera camera){
		Actor selected = camera.unitAt(locationOnScreen.copy());
		Alive targetedUnit = (Alive) (selected==null?null:Alive.class.isAssignableFrom(selected.getClass())?(selected.equals(this)?null:selected):null);
		aggroOn(targetedUnit);
		if(targetedUnit == null) super.rightClick(locationOnScreen, camera);
	}
	@Override
	public void trashComponents(){
		super.trashComponents();
		healthBar.active = false;
		engine.visibleObjects.remove(healthBar);
	}
	private void regen(){
		regen("health", getStat("health regen"));
		regen("mana", getStat("mana regen"));
	}
	protected void regen(String stat, double amount){
		changeStat(stat, amount / engine.getTickRate());
	}
	private void attackOrientedInit() {
		permissions[Constants.autoAttackAllowed] = true;
		changeStat("base attack time", 1.7);
		changeStat("base attack speed", 100);
		changeStat("base animation time", 1);
		this.resetAllAttackCDs();
	}
	private void configureAttack(){
		needToMove = attackTarget == null && target != null;
		changePermissions(Constants.movementAllowed, attackTarget == null && target != null);
		if(attackTarget == null || !attackTarget.alive){
			setAttackState(Alive.AttackState.NONE);
			needToMove = true;
		}
		else if(attackTarget != null && !attackTarget.active)
			aggroOn(null);
		else if(attackState == AttackState.SELECTED){
			if(!canSee(attackTarget)){
				aggroOn(null);
				needToMove = false;
			}
			else if(isAttackTargetWithinAttackRange()){
					issueAttack(attackTarget);
					attackTarget.getTargeted(this);
					setAttackState(Alive.AttackState.ANIMATION);
			}
			else if(!isAttackTargetWithinAttackRange()){
				System.out.println(getStat("attack range"));
				setAttackState(AttackState.MOVING);
				needToMove = true;
			}
		}
		else if(this.attackState == Alive.AttackState.MOVING)
			if(!isAttackTargetWithinAttackRange()){
				needToMove = true;
				target = attackTarget.getLocation();
			}
			else {
				target = null;
				issueAttack(attackTarget);
				setAttackState(Alive.AttackState.ANIMATION);
			}
		else if(this.attackState == Alive.AttackState.ANIMATION && getStat("animation cooldown") >   0)
			changeStat("animation cooldown", -1);
		else if(this.attackState == Alive.AttackState.ANIMATION && getStat("animation cooldown") <= 0){
			resetAnimationCD();
			setAttackState(Alive.AttackState.RETRACTION);
			doAttack(attackTarget);
		}
		else if(attackState == AttackState.RETRACTION && getStat("retraction cooldown") > 0){
			if(noRetraction)setAttackState(AttackState.WAIT);
			changeStat("retraction cooldown", -1);
		}
		else if(attackState == AttackState.RETRACTION && getStat("retraction cooldown") <= 0){
			resetRetractionCD();
			setAttackState(Alive.AttackState.WAIT);
		}
		else if(attackState == AttackState.WAIT && getStat("attack cooldown") > 0)
			changeStat("attack cooldown", -1);
		else if(attackState == AttackState.WAIT && getStat("attack cooldown") == 0){
			if(attackTarget != null)setAttackState(AttackState.SELECTED);
			else setAttackState(AttackState.NONE);
			resetAttackCD();
		}
		else assert false;
	}
	public static void changeStatsSize(int number){
		statsSize += number;
	}
	public static int getStatsSize() {
		return statsSize;
	}
	@Override
	protected void addToMap(){
		super.addToMap();
		for(String key: stats.keySet())
			values.put(key, String.valueOf(stats.get(key)));
		values.put("faction", String.valueOf(faction));
		values.put("invulnerable", String.valueOf(invulnerable));
		values.put("ranged", String.valueOf(missile));
	}
}
