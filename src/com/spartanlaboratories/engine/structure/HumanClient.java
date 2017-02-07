package com.spartanlaboratories.engine.structure;

import java.util.ArrayList;
import java.util.HashMap;

import com.spartanlaboratories.engine.game.Alive;
import com.spartanlaboratories.engine.game.Hero;
import com.spartanlaboratories.engine.game.VisibleObject;
import com.spartanlaboratories.util.Constants;

public class HumanClient extends Human{
	public ClientListener client;
	public HumanClient(Engine engine) {
		super(engine, Alive.Faction.RADIANT);
	}
	@Override
	public void poll(){
		client.readIncomingData();
	}
	private void sendQuad(Quad quad){
		client.sendQuadInfo(String.valueOf(quad.quadValues[0].x));
		client.sendQuadInfo(String.valueOf(quad.quadValues[0].y));
		client.sendQuadInfo(String.valueOf(quad.quadValues[1].x));
		client.sendQuadInfo(String.valueOf(quad.quadValues[1].y));
		client.sendQuadInfo(String.valueOf(quad.quadValues[2].x));
		client.sendQuadInfo(String.valueOf(quad.quadValues[2].y));
		client.sendQuadInfo(String.valueOf(quad.quadValues[3].x));
		client.sendQuadInfo(String.valueOf(quad.quadValues[3].y));
		client.sendQuadInfo(String.valueOf(quad.textureValues[0].x));
		client.sendQuadInfo(String.valueOf(quad.textureValues[0].y));
		client.sendQuadInfo(String.valueOf(quad.textureValues[1].x));
		client.sendQuadInfo(String.valueOf(quad.textureValues[1].y));
		client.sendQuadInfo(String.valueOf(quad.textureValues[2].x));
		client.sendQuadInfo(String.valueOf(quad.textureValues[2].y));
		client.sendQuadInfo(String.valueOf(quad.textureValues[3].x));
		client.sendQuadInfo(String.valueOf(quad.textureValues[3].y));
		client.sendQuadInfo(quad.color);
		client.sendQuadInfo(quad.texture);
	}
	
	public void out(String string) {
		
	}
	@Override
	protected void processQuadInfo() {
		for(Camera c: cameras){
			for(VisibleObject vo: c.getQualifiedObjects())
				c.generateQuad(vo);
			for(Quad q: c.getQuadList())
				sendQuad(q);
			c.getQuadList().clear();
		}
		client.out.println("end");
	}
	
	@Override
	protected void sendUnitInfo(){
		if(selectedUnit != null){
			HashMap<String, String> values = selectedUnit.getMap();
			client.out.println("unitinfo");
			client.out.println(values.keySet().size());
			for(String key: values.keySet()){
				client.out.println(key);
				client.out.println(values.get(key));
			}
		}
	}
	
	@Override
	public void notifyClient(String message) {
		client.out.println(message);
	}
	/**
	 * 
	 * @param buttonData
	 */
	void receiveButtonInput(String[] buttonData) {
		switch(buttonData[0].toLowerCase()){
		case "ability":
			if(Hero.class.isAssignableFrom(selectedUnit.getClass()))
				((Hero)selectedUnit).getAbility(Integer.parseInt(buttonData[1])).cast();
			break;
		default:
			System.out.print("Unable to process button data: ");
			System.out.println(buttonData);
			break;
		}
	}
	
}
