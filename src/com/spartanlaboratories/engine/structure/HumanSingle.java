package com.spartanlaboratories.engine.structure;

import java.util.ArrayList;

import com.spartanlaboratories.engine.game.Alive;
import com.spartanlaboratories.engine.util.Location;
import com.spartanlaboratories.graphics.ConnectionHandler;

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
	@Override
	public void notifyClient(String message) {}
	/* (non-Javadoc)
	 * @see com.spartanlaboratories.engine.structure.Human#sendUnitInfo()
	 */
	@Override
	protected void sendUnitInfo() {
		// TODO Auto-generated method stub
		
	}

}
class SinglePlayerHandler implements ConnectionHandler{
	HumanSingle owner;
	SinglePlayerHandler(HumanSingle owner){
		this.owner = owner;
	}
	/* (non-Javadoc)
	 * @see com.spartanlaboratories.graphics.ConnectionHandler#notifyOfKeyPress(com.spartanlaboratories.graphics.Input)
	 */
	@Override
	public void notifyOfKeyPress(com.spartanlaboratories.graphics.Input input) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see com.spartanlaboratories.graphics.ConnectionHandler#notifyOfMouseClick(com.spartanlaboratories.graphics.Input)
	 */
	@Override
	public void notifyOfMouseClick(com.spartanlaboratories.graphics.Input input) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see com.spartanlaboratories.graphics.ConnectionHandler#notifyOfMouseLocation(com.spartanlaboratories.graphics.Input)
	 */
	@Override
	public void notifyOfMouseLocation(com.spartanlaboratories.graphics.Input input) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see com.spartanlaboratories.graphics.ConnectionHandler#notifyOfMouseWheel(com.spartanlaboratories.graphics.Input)
	 */
	@Override
	public void notifyOfMouseWheel(com.spartanlaboratories.graphics.Input input) {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see com.spartanlaboratories.graphics.ConnectionHandler#getStat(java.lang.String)
	 */
	@Override
	public String getStat(String arg0) {
		// TODO Auto-generated method stub
		return null;
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
