package com.spartanlaboratories.engine.structure;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.spartanlaboratories.engine.util.Location;

public final class Tracker extends StructureObject {
	private int tickRate = Engine.getTickRate();
	private double notifyPeriod;
	private static int numberOfTrackedEntities = 20;
	private int[] timesCalled = new int[numberOfTrackedEntities];
	private boolean[] trackedEntities = new boolean[numberOfTrackedEntities];
	Location[] recordedTimes = new Location[numberOfTrackedEntities];
	Location[] entityStats = new Location[numberOfTrackedEntities];
	String[] entityNames = new String[numberOfTrackedEntities];
	Location[] lifespanStats = new Location[numberOfTrackedEntities];
	File file = new File("log.txt");
	BufferedWriter writer;
	int ticksTracked;
	private int secondTracked;
	public static final int ALG_UNIT_SELECTION = 0, FUNC_TICK = 1, FUNC_RENDER = 2, FUNC_QUADTREE_RESET = 3, TICK_CONTROLLERS = 4,
	FUNC_MAP_TICK = 5, FUNC_MISSILE_TICK = 6, FUNC_AURA_TICK = 7, FUNC_ACTOR_DELETION = 8,  REND_PLAYER = 11, 
	REND_HUMAN_GUI = 13, REND_PORTRAIT = 14, REND_QUADS = 15, TICK_AMBIENT = 16, TICK_RESET_TICK_VALUE = 17, TICK_RECEIVE_NETWORK_INFO = 18,
	FUNC_RUN = 19;
	Tracker(Engine engine){
		super(engine);
		setNotifyPeriod(15);
	}
	public enum TrackerPreset{
		PRESET_RUN, PRESET_TICK, PRESET_RENDER, PRESET_REND_HUMAN;
	}
	public void initialize(){
		try {
			writer = new BufferedWriter(new FileWriter(file));
		} catch (IOException e) {
			System.out.println("The tracker was not able to be initialized");
			e.printStackTrace();
		}
		ticksTracked = 0;
		secondTracked = 0;
		initEntityNames();
		for(int i = 0; i < numberOfTrackedEntities; i++){
			recordedTimes[i] = new Location(0,0);
			entityStats[i] = new Location(0,0);
			lifespanStats[i] = new Location(0,0);
		}
	}
	public void initialize(TrackerPreset trackerPreset){
		switch(trackerPreset){
		case PRESET_RUN:
			setEntityTracked(FUNC_TICK, true);
			setEntityTracked(FUNC_RENDER, true);
			break;
		case PRESET_TICK:
			setEntityTracked(FUNC_QUADTREE_RESET, true);
			setEntityTracked(TICK_CONTROLLERS, true);
			setEntityTracked(FUNC_MAP_TICK, true);
			setEntityTracked(FUNC_MISSILE_TICK, true);
			setEntityTracked(FUNC_AURA_TICK, true);
			setEntityTracked(FUNC_ACTOR_DELETION, true);
			setEntityTracked(TICK_AMBIENT, true);
			setEntityTracked(TICK_RECEIVE_NETWORK_INFO, true);
			setEntityTracked(TICK_RESET_TICK_VALUE, true);
			break;
		case PRESET_RENDER:
			setEntityTracked(REND_PLAYER, true);
			break;
		case PRESET_REND_HUMAN:
			setEntityTracked(REND_HUMAN_GUI, true);
			setEntityTracked(REND_PORTRAIT, true);
			setEntityTracked(REND_QUADS, true);
			break;
		}
		initialize();
	}
	public void clearTrackedEntities(){
		for(int i = 0; i < trackedEntities.length; i++){
			setEntityTracked(i, false);
		}
	}
	public void setNotifyPeriod(int seconds){
		notifyPeriod = seconds;
	}
	public void tick(){
		if(engine.util.everySecond(notifyPeriod)){
			for(int i = 0; i < numberOfTrackedEntities; i++)
				if(trackedEntities[i])
					displayLifespanStats(i);
			showComparison();
			logLifespanStats();
		}
		if(engine.util.everySecond(5))logLifespanStats();
		for(int i = 0; i < numberOfTrackedEntities; i++){
			if(!trackedEntities[i])continue;
			lifespanStats[i].x += entityStats[i].x;
			lifespanStats[i].y = lifespanStats[i].x * tickRate * 100 / ++ticksTracked;
			entityStats[i] = new Location(0,0);
		}
		if(trackedEntities[Tracker.FUNC_TICK])log();
	}
	public void setEntityTracked(int entityIdentity, boolean toTrackOrNotToTrack){
		trackedEntities[entityIdentity] = toTrackOrNotToTrack;
	}
	public void giveStartTime(int entityIdentity){
		++timesCalled[entityIdentity];
		if(trackedEntities[entityIdentity])
		recordedTimes[entityIdentity].x = System.nanoTime();
	}
	public void giveEndTime(int entityIdentity){
		if(!trackedEntities[entityIdentity])return;
		recordedTimes[entityIdentity].y = System.nanoTime();
		calculateEntityStats(entityIdentity);
	}
	public void displayEntityStats(int entityIdentity){
		System.out.print("Entity ");
		System.out.print(entityNames[entityIdentity] != null ? entityNames[entityIdentity] : ("number: "+ entityIdentity)); 
		System.out.println(" took " + entityStats[entityIdentity].x + " seconds\n" + "which is "
		+ entityStats[entityIdentity].y + "% of the time appropriated for a single tick");
	}
	public void displayLifespanStats(int entityIdentity){
		System.out.print("Entity ");
		System.out.print(entityNames[entityIdentity] != null ? entityNames[entityIdentity] : ("number: "+ entityIdentity)); 
		System.out.println(" took " + String.format("%.9f", (lifespanStats[entityIdentity].x / ticksTracked)) + " seconds\n" + "which is "
		+ String.format("%.2f", lifespanStats[entityIdentity].y) + "% of the time appropriated for a single tick");
	}
	public void closeWriter(){
		try {
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void log(String string){
		try {
			writer.write(string);
			writer.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void printAndLog(String string){
		System.out.println(string);
		log(string);
	}
	public int addEntity(){
		++numberOfTrackedEntities;
		timesCalled = new int[numberOfTrackedEntities];
		trackedEntities = new boolean[numberOfTrackedEntities];
		recordedTimes = new Location[numberOfTrackedEntities];
		entityStats = new Location[numberOfTrackedEntities];
		entityNames = new String[numberOfTrackedEntities];
		lifespanStats = new Location[numberOfTrackedEntities];
		return numberOfTrackedEntities - 1;
	}
	private void initEntityNames(){
		entityNames[0] = "Unit Selection"; entityNames[1] = "Tick"; entityNames[2] = "Render"; entityNames[3] = "Quadtree reset";
		entityNames[4] = "Hero owner tick"; entityNames[5] = "Map Tick"; 
		entityNames[9] = "Render Missile"; entityNames[10] = "Render Map"; entityNames[11] = "Render Heroes"; entityNames[12] = "Render Human"; 
	}
	private void calculateEntityStats(int entityIdentity){
		entityStats[entityIdentity].x += (recordedTimes[entityIdentity].y - recordedTimes[entityIdentity].x) / (1000 * 1000 * 1000);
		entityStats[entityIdentity].y = entityStats[entityIdentity].x * tickRate * 100;
	}
	private void log(){
		if(engine.util.everySecond(1))try{
			writer.write("At Second number " + ++secondTracked + ", " + lifespanStats[FUNC_TICK].x + " seconds were used for the tick function");
			writer.newLine();
		}catch(IOException e){
			System.out.println("There is a problem with the tracker's log() function");
		}
	}
	private void logLifespanStats(){
		for(int i = 0; i < numberOfTrackedEntities; i++)if(this.trackedEntities[i]){
			log("Entity " + getEntityName(i) + " took " + String.format("%.9f", (lifespanStats[i].x / ticksTracked)) + " seconds");
			log("which is " + String.format("%.2f", lifespanStats[i].y) + "% of the time appropriated for a single tick");
		}
	}
	public void showComparison(int entity1, int entity2){
		System.out.printf("Entity 1 took %.2f as much as entity 2 in their lifetime\n", lifespanStats[entity1].x / lifespanStats[entity2].x);
	}
	public void showComparison(){
		double averageTime, totalTime = 0, numEntities = 0;
		for(int i = 0; i < trackedEntities.length; i++)
			if(trackedEntities[i]){
				++numEntities;
				totalTime += lifespanStats[i].x;
			}
		averageTime = totalTime / numEntities;
		for(int i = 0; i < numberOfTrackedEntities; i++)if(trackedEntities[i]){
			double percentage = 100 * ((1 - lifespanStats[i].x / averageTime) / (lifespanStats[i].x / averageTime));
			System.out.printf("Entity %s was %.2f%% %s than the average\n", getEntityName(i), Math.abs(percentage),percentage > 0 ? "faster" : "slower");
		}
	}
	private String getEntityName(int index){
		return entityNames[index] != null ? entityNames[index] : ("number: "+ index);
	}
}
