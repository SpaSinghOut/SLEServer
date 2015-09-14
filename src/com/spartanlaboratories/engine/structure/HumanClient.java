package com.spartanlaboratories.engine.structure;

import com.spartanlaboratories.engine.game.Alive;
import com.spartanlaboratories.engine.game.VisibleObject;

public class HumanClient extends Human{
	ClientListener client;
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
	public void processQuadInfo() {
		for(Camera c: cameras){
			for(VisibleObject vo: c.getQualifiedObjects())
				c.generateQuad(vo);
			for(Quad q: c.getQuadList())
				sendQuad(q);
			c.getQuadList().clear();
		}
		client.out.println("end");
	}
	
}
