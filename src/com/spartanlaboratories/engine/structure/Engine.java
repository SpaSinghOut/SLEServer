 package com.spartanlaboratories.engine.structure;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.spartanlaboratories.engine.game.Actor;
import com.spartanlaboratories.engine.game.Alive;
import com.spartanlaboratories.engine.game.Aura;
import com.spartanlaboratories.engine.game.Buff;
import com.spartanlaboratories.engine.game.Creep;
import com.spartanlaboratories.engine.game.Effect;
import com.spartanlaboratories.engine.game.GameObject;
import com.spartanlaboratories.engine.game.Missile;
import com.spartanlaboratories.engine.game.Tower;
import com.spartanlaboratories.engine.game.VisibleObject;
import com.spartanlaboratories.engine.util.Location;

/**
 * The Spartan Laboratories Game Engine
 * @author Spartak
 *
 */
public class Engine{
	/**
	 * Controls whether or not the program is running. Does NOT pause the program when set to false, instead it will terminate it, for the pausing function
	 * refer to {@link #pause}
	 * Manual modification of this is best  avoided as even if this was used to terminate the program it would not terminate correctly.
	 */
	public boolean running;
	private static final int port = 7000;
	public boolean ambientUpdate;
	public final int heroPickSecondDelay = 0;
	public ArrayList<Controller> controllers = new ArrayList<Controller>();
	public ArrayList<Missile> missiles = new ArrayList<Missile>();
	public ArrayList<Actor> allActors = new ArrayList<Actor>();
	public Map map; //DO NOT INITIALIZE HERE, will not work due to opengl context requirement
	public int tickCount;
	/**
	 * One of the variables that controls the state of execution. Setting this to true would 
	 * "pause" execution, stopping the ticks of all game objects but continuing the rendering. Pressing the p key will pause as well as prompt the user
	 * that execution is paused.
	 */
	public boolean pause;
	public Quadtree<Double, VisibleObject>  qt = new Quadtree<Double, VisibleObject>(this);
	public Tracker tracker = new Tracker(this);
	public Util util = new Util(this);
	/** The rate at which the game updates in updates/second 
	 * */
	private static int tickRate;
	private static long period;
	public ArrayList<VisibleObject> visibleObjects = new ArrayList<VisibleObject>();
	public TypeHandler<StructureObject> typeHandler = new TypeHandler<StructureObject>();
	private final Location wrap = new Location(3000,2000);
	public byte numConnections;
	private ArrayList<GameObject> deleteThis = new ArrayList<GameObject>();
	static long time;
	private boolean noThread;
	ServerSocket server;
	public void goMulti(Map map, int numPlayers){
		try{
			noThread = true;
			// The server socket that the client is going to connect to
		    server = new ServerSocket(port);
			typeHandler.newEntry("map", map);
			this.map = (Map) typeHandler.typeGetter.get("map");
			for(int i = 0; i < numPlayers; i++){
				// Waits for and registers a connected client
			    Socket client = server.accept();
				// Writes to the aforementioned client
			    PrintWriter out = new PrintWriter(client.getOutputStream(), true);
				// Reads from the aforementioned client
			    BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
				HumanClient human = new HumanClient(this);
				new ClientListener(client, out, in, human);
				//initializeOpenGL();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		init();
	}
	private static class ClientThread implements Runnable{
		PrintWriter out;
		BufferedReader in;
		HumanClient human;
		ClientThread(Engine engine, Socket socket){
			try{
				out = new PrintWriter(socket.getOutputStream());
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				human = new HumanClient(engine);
				new ClientListener(socket, out, in, human);
			}catch(IOException e){
				e.printStackTrace();
			}
		}
		@Override
		public void run() {
			while(true)human.poll();
		}
	}
	private static class Server implements Runnable{
		Engine engine;
		ServerSocket server;
		ExecutorService connections = Executors.newFixedThreadPool(8);		// Creates a thread pool
		public Server(Engine engine){
			this.engine = engine;
			try {
				server = new ServerSocket(engine.port);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		public void run(){
			try{
				while(true){
					// Adds a new client handling thread to the thread pool for every connection
					Thread subthread = new Thread(new ClientThread(engine,server.accept()));
					subthread.setPriority(Thread.MIN_PRIORITY + 1);
					connections.execute(subthread);
					System.out.println("A new connection was detected");
				}
			}catch(IOException e){
				e.printStackTrace();
			}
		}
	}
	public void goMultiTest(Map map){
		
		// Other shit
		typeHandler.newEntry("map", map);
		this.map = (Map) typeHandler.typeGetter.get("map");
		init();
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		Thread server = new Thread(new Server(this));
		server.setPriority(Thread.MIN_PRIORITY);
		server.start();
		// End other shit
	}
	public void goSingle(Map map){
		new HumanSingle(this);
		typeHandler.newEntry("map", map);
		this.map = (Map) typeHandler.typeGetter.get("map");
		init();
	}
	public class TypeHandler<Type extends StructureObject>{
		private HashMap<String, StructureObject> typeGetter = new HashMap<String,StructureObject>();
		public void newEntry(String string, Type type){
			typeGetter.put(string, type);
		}
		public StructureObject getEntry(String string){
			return typeGetter.get(string);
		}
	}
	private void init(){
		ambientUpdate = false;
		tracker = new Tracker(this);
		tracker.setEntityTracked(Tracker.FUNC_TICK, true);
		SLEXMLException.engine = this;
		setTickRate(60);
		map.init();
	}
	private void finish(){
		System.out.println("terminating");
		tracker.closeWriter();
		if(server != null)
			try {
				server.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	public void run(){
		running = true;
		time = System.nanoTime();
		while(running){
			if(System.nanoTime() > time + period && !pause){
				time += period;
				tickCount++;
				tick();
			}
			else if(noThread)for(Human h:Human.players)h.poll();
			
		}
		finish();
	}
	public static int getTickRate(){
		return tickRate;
	}
	public static void setTickRate(int i){
		tickRate = i;
		period = 1000000000 / tickRate;
	}
	private void tick(){
		tracker.giveStartTime(Tracker.FUNC_TICK);
		tracker.giveStartTime(Tracker.TICK_RESET_TICK_VALUE);
		for(GameObject g: GameObject.gameObjects)g.ticked = false;
		tracker.giveEndTime(Tracker.TICK_RESET_TICK_VALUE);
		tracker.giveStartTime(Tracker.FUNC_QUADTREE_RESET);
		qt.clear();
		tracker.giveEndTime(Tracker.FUNC_QUADTREE_RESET);
		tracker.giveStartTime(Tracker.TICK_CONTROLLERS);
		for(Controller heroOwner: controllers)heroOwner.tick();
		tracker.giveEndTime(Tracker.TICK_CONTROLLERS);
		tracker.giveStartTime(Tracker.FUNC_MAP_TICK);
		map.tick();
		tracker.giveEndTime(Tracker.FUNC_MAP_TICK);
		tracker.giveStartTime(Tracker.FUNC_MISSILE_TICK);
		tickMissiles();
		tracker.giveEndTime(Tracker.FUNC_MISSILE_TICK);
		tracker.giveStartTime(Tracker.FUNC_AURA_TICK);
		tickAuras();
		tracker.giveEndTime(Tracker.FUNC_AURA_TICK);
		tracker.giveStartTime(Tracker.FUNC_ACTOR_DELETION);
		deleteStuff();
		tracker.giveEndTime(Tracker.FUNC_ACTOR_DELETION);
		tracker.giveStartTime(Tracker.TICK_AMBIENT);
		if(ambientUpdate)for(GameObject g: GameObject.gameObjects)if(!g.ticked)g.tick();
		tracker.giveEndTime(Tracker.TICK_AMBIENT);
		tracker.tick();
		tracker.giveEndTime(Tracker.FUNC_TICK);
	}
	private void tickMissiles(){
		for(Missile spell: missiles){
			if(!spell.tick())addToDeleteList(spell);
		}
	}
	private void tickAuras(){
		for(Aura a: Aura.auras)a.tick();
	}
	static double getRandom(){
		return Math.random();
	}
	public Location getWrap(){
		return wrap;
	}
	public ArrayList<Missile> getSpells(){
		return missiles;
	}
	private void deleteStuff(){
		for(GameObject a: deleteThis){
			if(a.getClass() == Missile.class && missiles.contains(a))
				missiles.remove(a);
			else if(a.getClass() == Buff.class)((Buff)(a)).owner.getBuffs().remove(a);
			else if(a.getClass() == Creep.class)Creep.allCreeps.remove(a);
			else if(a.getClass() == Effect.class);
			else if(a.getClass() == Tower.class);
			if(Alive.class.isAssignableFrom(a.getClass()))Alive.allAlives.remove(a);
			if(Actor.class.isAssignableFrom(a.getClass()))allActors.remove(a);
			if(VisibleObject.class.isAssignableFrom(a.getClass())){
				visibleObjects.remove(a);
				((VisibleObject)a).trashComponents();
			}
		}
		deleteThis.clear();
	}
	public ArrayList<GameObject> getDeleteList(){
		return deleteThis;
	}
	public void addToDeleteList(GameObject poorGuy){
		deleteThis.add(poorGuy);
	}
}
