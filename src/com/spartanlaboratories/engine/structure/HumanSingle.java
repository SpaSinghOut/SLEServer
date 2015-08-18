package com.spartanlaboratories.engine.structure;

import java.util.ArrayList;

import com.spartanlaboratories.engine.game.Alive;
import com.spartanlaboratories.engine.util.Location;

public class HumanSingle extends Human{
	ArrayList<Input> input = new ArrayList<Input>();
	SinglePlayerHandler gui;
	public HumanSingle(Engine engine) {
		super(engine, Alive.Faction.RADIANT);
		gui = new SinglePlayerHandler(this);
	}
	@Override
	public void tick(){
		super.tick();
	}
	@Override
	public void poll() {
		for(Input i: input)switch(i.source){
		case KEYBOARD:
			receiveKeyInput(i.button, i.type.toString().toLowerCase());
			break;
		case MOUSE:
			receiveMouseInput(i.button, i.type.toString().toLowerCase());
			if(i.location == null) break;
		case MOUSELOCATION:
			receiveMouseInput(MOUSELOCATION, i.location.toString());
			break;
		case MOUSEWHEEL:
			receiveMouseInput(MOUSEWHEEL, String.valueOf(i.button));
			break;
		default:
			System.out.println("Single player version of the human class received bad input");
			break;
		
		}
	}

	@Override
	public void processQuadInfo() {
		
	}

	@Override
	public void out(String message) {
		
	}

}
class SinglePlayerHandler{
	HumanSingle owner;
	SinglePlayerHandler(HumanSingle owner){
		this.owner = owner;
	}
	
	
}
class Input{
	enum Type{
		PRESS, RELEASE, FULL,;
	}
	enum Source{
		MOUSE, MOUSEWHEEL, MOUSELOCATION, KEYBOARD,;
	}
	Type type;
	Source source;
	int button;
	Location location;
}
