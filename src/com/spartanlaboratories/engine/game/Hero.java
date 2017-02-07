package com.spartanlaboratories.engine.game;

import java.util.ArrayList;

import com.spartanlaboratories.engine.structure.SLEImproperInputException;
import com.spartanlaboratories.engine.structure.StandardCamera;
import com.spartanlaboratories.engine.structure.Controller;
import com.spartanlaboratories.engine.structure.Engine;
import com.spartanlaboratories.engine.util.Location;
import com.spartanlaboratories.util.Constants;

public class Hero<Element extends Ability> extends Alive{
	private int inventorySize = 6;
	int numberOfAbilities = 3;
	ArrayList<Element> abilities = new ArrayList<Element>();
	public HeroType heroType;
	VisibleObject manaBar;
	boolean initialized;
	Ability equippedSpell;
	public enum HeroType{
		RAZOR(true,600),
		;
		boolean ranged;
		double range;
		HeroType(boolean setRanged, double setRange){
			ranged = setRanged;
			range = setRange;
		}
	}
	public Hero(Engine engine, Controller controller) {
		super(engine, controller.faction);
		manaBar = new VisibleObject(engine);
		manaBar.setColor("blue");
		setSize(35,35);
		defaultColor = "white";
		setColor(defaultColor);
		shape = Actor.Shape.QUAD;
		setLocation(new Location(faction == Alive.Faction.RADIANT ? 500: 2500, 750));
		childSetsOwnMovement = true;
		setAttackState(AttackState.NONE);
		childSetsOwnMovement = false;
		initStats();
		inventory = new ItemList(inventorySize, ItemList.Type.INVENTORY, this);
		controller.addUnit(this);
	}
	@Deprecated
	public void initHeroType(HeroType setHeroType){
		heroType = setHeroType;
		setStat("mana", getStat("max mana"));
		missile = heroType.ranged;
		changeStat("attack range", heroType.range);
		initialized = true;
	}
	public boolean tick(){
		for(Ability a: abilities)a.tick();
		updateManaBar();
		return super.tick();
	}
	private void updateManaBar(){
		manaBar.setWidth(getWidth() * getRatio("mana"));
	}
	public void castSpell(Ability ability){
		if(ability.state != Ability.State.READY && ability.abilityStats.castType != Castable.CastType.TOGGLE)
			return;
		if(ability.abilityStats.castType == Castable.CastType.ALIVETARGET){
			if(owner.selectedUnit == null || !Alive.class.isAssignableFrom(owner.selectedUnit.getClass()))return;
			ability.setTarget((Alive)owner.selectedUnit);
		}
		try {
			ability.activate();
		} catch (SLEImproperInputException e) {
			System.out.println("The hero was unable to cast the spell due to improper input");
			e.printStackTrace();
		}
		for(Buff b: getBuffs())
			if(b.activationTrigger == Buff.TriggerType.ONSPELLCAST)
				b.trigger(this);
	}
	@Override
	public void die(){
		super.die();
		owner.setRespawnTimer(2);
		Alive.allAlives.remove(this);
	}
	public void setWidth(double width){
		super.setWidth(width);
		manaBar.setWidth(width);
	}
	public void setHeight(double height){
		super.setHeight(height);
		manaBar.setHeight((int)(height * 0.25));
	}
	protected void updateComponentLocation(){
		super.updateComponentLocation();
		manaBar.setLocation(getLocation().x - (1 - getRatio("mana")) * getWidth() / 2, healthBar.getLocation().y);
		manaBar.setWidth(getWidth() * getRatio("mana"));
		healthBar.changeLocation(0, -healthBar.getHeight());
	}
	void addAbility(Element element){
		abilities.add(element);
		element.owner = this;
	}
	public void copyTo(Hero<Element> hero){
		hero.equippedSpell = equippedSpell;
		hero.inventorySize = inventorySize;
		hero.numberOfAbilities = numberOfAbilities;
		hero.abilities = new ArrayList<Element>();
		for(Element e:abilities)hero.abilities.add(e);
	}
	public void leftClick(Location locationOnScreen, StandardCamera camera){
		castSpell(equippedSpell);
	}
	@Override
	public void trashComponents(){
		super.trashComponents();
		manaBar.active = false;
		engine.visibleObjects.remove(manaBar);
	}
	private void initStats(){
		changeBaseSpeed(300);
		changeStat("visibility range", 900);
		changeStat("max health", Constants.baseHealth);
		setStat("health", getStat("max health"));
		changeStat("max mana", 300);
		setStat("mana", getStat("max mana"));
		changeStat("mana regen", 1);
		changeStat("health regen", 0.1);
		changeStat("experience given", 200);
		changeStat("starting damage", 30);
		changeStat("ability points", 1);
		changeStat("attack speed", 400);
		setStat("base animation time",1);
		setStat("base attack time",1);
	}
	public Ability getAbility(int index){
		return abilities.get(index);
	}
}
