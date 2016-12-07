package game;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_KP_2;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_KP_4;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_KP_5;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_KP_6;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_KP_8;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_M;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_UP;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL20.*;
import game.TrafficInfo.Node;
import highscore.Entry;
import highscore.HighScoreClient;
import highscore.HighScoreData;

import java.awt.Frame;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import org.lwjgl.glfw.GLFW;

import physics.Physics;
import radio.Radio;
import radio.Radio.RadioButton;
import sound.Sound;
import GLEngine.GLSLProgram;
import GLEngine.InputHandler;
import GLEngine.Matrix4f;
import GLEngine.Mesh;
import GLEngine.MeshBatch;
import GLEngine.SpriteBatch;
import GLEngine.Texture;
import GLEngine.Utilities;
import GLEngine.Vector3f;
import GLEngine.Window;
import editor.FileHandler;
import entity.AIVehicle;
import entity.Entity;
import entity.Entity.State;
import entity.PlayerVehicle;

public class Game {
	public static boolean DEBUG_KEYS = false;
	public static boolean DEBUG_SHOW_TRAFFIC_NODES = false;
	public static boolean DEBUG_SHOW_VEHICLE_NEXT_NODE = false;
	public static boolean DEBUG_SHOW_TRAFFIC_PATH = false;
	public static boolean DEBUG_SHOW_AI_BREAK = false;
	public static boolean DEBUG_SHOW_CAMERA_POS = false;
	public static boolean DEBUG_NO_TRAFFIC = false;
	public static boolean DEBUG_TEST_RESTART = false;
	public static boolean DEBUG_PRINT_HIGHSCORE = false;
	public static boolean DEBUG_SHORT_SPAWN_TIME = false;
	public static boolean DEBUG_WINDOW_SIZE = false;
	public static boolean DEBUG_SHOW_FPS = true;
	
	private Physics physics;
	private ArrayList<Entity> entities = new ArrayList<Entity>();
	private PlayerVehicle playerCar;
	private Sound sound;
	private Radio radio;
	private GLSLProgram activeProgram;
	private GLSLProgram activeProgram2D;
	private GLSLProgram activeProgramMap;
	
	private MeshBatch meshBatch;
	private SpriteBatch spriteBatch;
	private Window window;
	private Camera camera;
	private FileHandler fileHandler;
	
	private HighScoreClient highScoreClient;

	
	private float fov = 70;
	private float current_FPS = 0;
	public static final int FPS = 60;
	
	private enum Mode{
		FREE_ROAM, DRIVE_CAR;
	}
	
	private enum GameState{
		MAIN_MENU, INGAME, END, SHOW_HIGHSCORE, INTRO, HELP;
	}
	private int mainMenuSelected = 0;
	private Entry[] lastHighScoreList = new Entry[0];
	private GameState state = GameState.MAIN_MENU;
	
	private Mode mode = Mode.DRIVE_CAR;
	private TrafficInfo trafficInfo;
	private MissionHandler missionHandler;
	private Settings settings;
	public Game(Settings settings) {
		this.settings = settings;
		oneTimeInit();
		//restart();
		loop();
		free();
		System.exit(0);
	}
	
	public void oneTimeInit(){
		window = new Window("Sten taxi", settings.width, settings.height);
		window.init(settings.aa, settings.full);
		
		activeProgram = new GLSLProgram("/shaders/vertex.vert", "/shaders/fragment.frag");
		activeProgram2D = new GLSLProgram("/shaders/vertex2D.vert", "/shaders/fragment2D.frag");
		activeProgramMap = new GLSLProgram("/shaders/vertMap.vert", "/shaders/fragMap.frag");
		
		spriteBatch = new SpriteBatch();
		spriteBatch.init();
		renderLoadingScreen();
		initMeshBatch();
		
		sound = new Sound(settings.volume, settings.volume);
		highScoreClient = new HighScoreClient();
	}
	
	public void initMeshBatch(){
		meshBatch = new MeshBatch();
		meshBatch.init();
		meshBatch.preAllocateVAO(Mesh.TRASHCAN);
		meshBatch.preAllocateVAO(Mesh.SKYSCRAPER);
	}
	
	private void newRound(){
		cleanUp();
		restart();
	}
	
	private void cleanUp(){
		for (Entity e : entities) {
			e.cleanUp();
		}
		entities.clear();
	}
	
	private void restart() {
		
		physics = new Physics();
		camera = new Camera(physics);
		camera.pos = new Vector3f(30, 30, 30);
		camera.rot.x = -45;
		trafficInfo = new TrafficInfo();
		fileHandler = new FileHandler();
		fileHandler.load();
		ArrayList<Vector3f> customerNodes = new ArrayList<Vector3f>();
		BufferedImage minimap = fileHandler.createDynamicEntities(physics, entities, trafficInfo, customerNodes);
		Texture.createMinimap(minimap);
		
		trafficInfo.createFinalInfo();
		createWorld();
		updateEntityStates();
		
		/*ArrayList<Vector3f> spawns = new ArrayList<Vector3f>();
		ArrayList<Vector3f> targets = new ArrayList<Vector3f>();
		spawns.add(new Vector3f(10, 1, 10));
		targets.add(new Vector3f(30, 1, 10));*/
		if(missionHandler != null) missionHandler.free();
		missionHandler = new MissionHandler(playerCar, customerNodes, sound);
		
		if (radio != null) {
			radio.close();
		}
		radio = new Radio(settings.radioVolume, true);
	}
	
	private void createWorld() {
		/*int groundSize = 10; // Om saker är förstora verkar de kunna börja darra, iaf hjulen, http://www.bulletphysics.org/mediawiki-1.5.8/index.php?title=Scaling_The_World
		int worldSize = 1000;
		for (int x = -worldSize / 2; x <= worldSize/2; x += groundSize) {
			for (int z = -worldSize / 2; z <= worldSize/2; z += groundSize) {
				physics.createStaticBox(new javax.vecmath.Vector3f(groundSize, groundSize, groundSize) , new javax.vecmath.Vector3f(x, -groundSize, z)); // mark
			}
		}*/
		/*for (int i = 0; i < 10; i++) {
			entities.add(new TrashCan(physics, new javax.vecmath.Vector3f(	(float) (-10 + Math.random() * 20),
																			(float) (10 + Math.random() * 20),
																			(float) (-10 + Math.random() * 20))));
			entities.add(new TexturedBoxTest(physics, new javax.vecmath.Vector3f(	(float) (-10 + Math.random() * 20),
					(float) (10 + Math.random() * 20),
					(float) (-10 + Math.random() * 20))));
		}
		entities.add(new Skyscraper(physics, new javax.vecmath.Vector3f(25, 0, 15)));
		entities.add(new Ramp(physics, new javax.vecmath.Vector3f(-2, 0, 15)));
		entities.add(new Ramp(physics, new javax.vecmath.Vector3f(-30, 0, 15)));*/
		/*for (int i = 0; i < 1; i++) {
			entities.add(new AIVehicle(physics, new javax.vecmath.Vector3f(	(float) (10 + Math.random() * 20),
																			(float) (10 + Math.random() * 20),
																			(float) (10 + Math.random() * 20)),trafficInfo, trafficInfo.getNodes().get(0)));
		}*/
		
		
		double thres = 500; //40 100
		double sum = 0;
		for (Node node : trafficInfo.getNodes()) {
			for (Node next : node.getAllNext()) {
				sum += node.pos.distanceTo(next.pos);
				if (sum > thres && !DEBUG_NO_TRAFFIC) {
					entities.add(new AIVehicle(physics, new javax.vecmath.Vector3f(node.pos.x, 0.5f ,node.pos.z), trafficInfo, next));
					sum = 0;
				}
			}
		}
		playerCar = new PlayerVehicle(physics, new javax.vecmath.Vector3f(20, 5, 20), sound, camera);
		entities.add(playerCar);
		trafficInfo.addVehicle(playerCar);
	}
	
	
	public void loop(){
		long tick = 0;
        while(!window.shouldClose()){
            long time0 = System.nanoTime();
            tick++;
            
            InputHandler.update();
        	window.pollEvents();
        	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        	
            if(state == GameState.INGAME){
            	update(tick);
            	render(tick);
            } else if(state == GameState.MAIN_MENU){
            	updateMainMenu();
            } else if (state == GameState.HELP) {
            	if (InputHandler.pressed(GLFW.GLFW_KEY_ENTER)) {
            		state = GameState.MAIN_MENU;
            	}
            	renderHelp();
            } else if (state == GameState.SHOW_HIGHSCORE) {
            	if (InputHandler.pressed(GLFW.GLFW_KEY_ENTER)) {
            		state = GameState.MAIN_MENU;
            	}
            	renderHighscore(false);
            } else if(state == GameState.END){
            	if (InputHandler.pressed(GLFW.GLFW_KEY_ENTER)) {
            		state = GameState.MAIN_MENU;
            	}
            	renderHighscore(true);
            }
            

            long dt = System.nanoTime() - time0;
            
            if(tick % 60 == 0 && dt!= 0){
            //	System.out.println("FPS: " + 1.0/((float)dt/1E9));
            	current_FPS = (float) (1.0/(dt/1E9));
            }
            
        }
	}
	


	private void updateHighscore() {
		try {
			lastHighScoreList = highScoreClient.getList();
		} catch (IOException e) {
			lastHighScoreList = new Entry[]{new Entry("Could not connect to highscore server", 0)};
		}
	}

	private void updateMainMenu() {
		/*if (InputHandler.pressed(GLFW_KEY_S)) {
    		
    	}*/
    	boolean render = true;
    	if (InputHandler.pressed(GLFW_KEY_UP)) {
    		mainMenuSelected--;
    		if (mainMenuSelected == -1) {
    			mainMenuSelected = 3;
    		}
    	}
    	if (InputHandler.pressed(GLFW_KEY_DOWN)) {
    		mainMenuSelected++;
    		if (mainMenuSelected == 4) {
    			mainMenuSelected = 0;
    		}
    	}
    	
    	if (InputHandler.pressed(GLFW.GLFW_KEY_ENTER)) {
    		if (mainMenuSelected == 0) {
    			renderLoadingScreen();
    			render = false;
    			state = GameState.INGAME;
        		newRound();
    		}else if (mainMenuSelected == 1) {
    			updateHighscore();
    			state = GameState.SHOW_HIGHSCORE;
    		}else if (mainMenuSelected == 2) {
    			state = GameState.HELP;
    		}else{
    			window.setShouldClose(true);
    		}
    	}
    	if(render) renderMainMenu();
	}

	public void update(long tick){
		physics.update();
		for (Entity e: entities) {
			if (e.getState() == State.VISUAL_PHYSICS) {
				e.update();
			}
		}
		//if (tick % 60 == 0) {
			updateEntityStates();
		//}
		updateInput(tick);
		if (mode == Mode.DRIVE_CAR) {
			camera.follow(playerCar);
		} else if (mode == Mode.FREE_ROAM) {
			camera.stopFollowing();
		}
		
		playerCar.updateStats(tick, missionHandler);
		boolean gameOver = missionHandler.update(tick);
		boolean escape = InputHandler.down(GLFW_KEY_ESCAPE);
		if (gameOver || escape || (DEBUG_TEST_RESTART && InputHandler.pressed(GLFW.GLFW_KEY_R))) {
			if (!InputHandler.down(GLFW_KEY_ESCAPE)) {
				try {
					highScoreClient.postNew(settings.name, missionHandler.getScore());
					if (DEBUG_PRINT_HIGHSCORE) {
						Entry[] topList = highScoreClient.getList();
						for (int i = 0; i < topList.length; i++) {
							System.out.println("" + (i + 1) + ": " + topList[i].name + " : " + topList[i].score);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				} 
				
				updateHighscore();
			}
			sound.stopMusic();
			radio.close();
			if (escape) {
				state = GameState.MAIN_MENU;
			}else{
				state = GameState.END;
			}
		}
	}
	
	private void updateEntityStates(){
		for (Entity e: entities) {
			//Vector3f a = camera.pos;
			javax.vecmath.Vector3f b = e.getCenter();
			Vector3f ent = new Vector3f(e.getCenter().x, e.getCenter().y, e.getCenter().z);
			javax.vecmath.Vector3f a = playerCar.getCenter();
			Vector3f dir = (camera.target.subtract(camera.pos)).normalized();
			dir.y = 0;
			Vector3f edif = (ent.subtract(camera.pos)).normalized();
			edif.y = 0;
			double dx = a.x - b.x;
			double dy = a.y - b.y;
			double dz = a.z - b.z;
			double dis = Math.sqrt(dx * dx + dy * dy + dz * dz); // Tänk på att kameran kan vara en bit bort från bilen
			//500 200 +50
			
			if(dis < 75){
				e.setState(Entity.State.VISUAL_PHYSICS);
			}else if(edif.dot(dir) < 0){
				if(dis < 75){
					e.setState(Entity.State.VISUAL_ONLY);
				}else{
					e.setState(Entity.State.HIDDEN);
				}
			}else if(dis < 50 || (Math.acos(edif.dot(dir)) < Math.toRadians(fov/2) && dis < 150)){
				e.setState(Entity.State.VISUAL_PHYSICS);
			}else if(dis < 500){
				e.setState(Entity.State.VISUAL_ONLY);
			}
			else{
				e.setState(Entity.State.HIDDEN);
			}
			/*if (dis > 500 || (dis > 30 + (e.isStatic() ? 100 : 0)  && Math.acos(edif.dot(dir)) >= Math.toRadians(fov))) {
				e.setState(Entity.State.HIDDEN);
			} else if (dis > (75 + (e.isStatic() ? 100 : 0))){ //,  TODO denna bör vara större än diagonalen på den största tilen, typ
				e.setState(Entity.State.VISUAL_ONLY);
			} else {
				e.setState(Entity.State.VISUAL_PHYSICS);
			}
			*/
		}
	}
	
    private void updateInput(long tick) {
    	
		
    	if(InputHandler.down(GLFW_KEY_ESCAPE)){
        	//window.setShouldClose(true);
        }
    	
    	if (DEBUG_TEST_RESTART && InputHandler.pressed(GLFW.GLFW_KEY_R)) {
    		//newRound();
    		//state = GameState.END;
    	}
    	
    	if (InputHandler.pressed(GLFW_KEY_M) && DEBUG_KEYS) {
    		mode = mode == Mode.FREE_ROAM ? Mode.DRIVE_CAR : Mode.FREE_ROAM;
    	}
    	
    	if (mode == Mode.FREE_ROAM) {
    		float speed = 0.2f;
        	if (InputHandler.down(GLFW_KEY_DOWN)) {
    			camera.pos.x += speed * Math.sin(Math.toRadians(camera.rot.y));
    			camera.pos.z += speed * Math.sin(Math.toRadians(camera.rot.y + 90));
    		}
    		if (InputHandler.down(GLFW_KEY_UP)) {
    			camera.pos.x -= speed * Math.sin(Math.toRadians(camera.rot.y));
    			camera.pos.z -= speed * Math.sin(Math.toRadians(camera.rot.y + 90));
    		}
    		if (InputHandler.down(GLFW_KEY_RIGHT)) {
    			camera.pos.z -= speed * Math.sin(Math.toRadians(camera.rot.y));
    			camera.pos.x += speed * Math.sin(Math.toRadians(camera.rot.y + 90));
    		}
    		if (InputHandler.down(GLFW_KEY_LEFT)) {
    			camera.pos.z += speed * Math.sin(Math.toRadians(camera.rot.y));
    			camera.pos.x -= speed * Math.sin(Math.toRadians(camera.rot.y + 90));
    		}
    		
            if(InputHandler.down(GLFW_KEY_W)){
            	camera.rot.x += 1;
            }
            if(InputHandler.down(GLFW_KEY_S)){
            	camera.rot.x -= 1;
            }
            if(InputHandler.down(GLFW_KEY_A)){
            	camera.rot.y += 1;
            }
            if(InputHandler.down(GLFW_KEY_D)){
            	camera.rot.y -= 1;
            }
            if(InputHandler.down(GLFW_KEY_SPACE)){
            	camera.pos.y+=speed;
            }
            if(InputHandler.down(GLFW_KEY_LEFT_CONTROL)){
            	camera.pos.y-=speed;
            }
    	} else if (mode == Mode.DRIVE_CAR) {
    		playerCar.updateInput();
    		missionHandler.updateInput(tick);
    	}
    	
        
        
        if(InputHandler.pressed(GLFW.GLFW_KEY_Q)){
        	radio.pressButton(RadioButton.ON_OFF);
        }
        if(InputHandler.pressed(GLFW.GLFW_KEY_S)){
        	radio.pressButton(RadioButton.STATION_PREVIOUS);
        }
        if(InputHandler.pressed(GLFW.GLFW_KEY_D)){
        	radio.pressButton(RadioButton.STATION_NEXT);
        }
        if(InputHandler.pressed(GLFW.GLFW_KEY_W)){
        	radio.pressButton(RadioButton.VOLUME_UP);
        }
        if(InputHandler.pressed(GLFW.GLFW_KEY_E)){
        	radio.pressButton(RadioButton.VOLUME_DOWN);
        }
        
	}
    
    public void shadowMapPass(){
    	
    	
    }
    
    public void renderPass(){
    	
    }	
	
    private void renderHighscore(boolean end){
    	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		initOrtho();
        spriteBatch.begin(activeProgram2D);
        if (end) {
        	spriteBatch.renderString("Game Over! You got: " + missionHandler.score, new Vector3f(Window.width/2 - 400, settings.height/2 - 300, 0), 0.5f, new Vector3f(1,1,0));
        }
    	spriteBatch.renderString("Global highscore", new Vector3f(settings.width/2 - 500, settings.height/2 - 250, 0), 0.8f, new Vector3f(1,1,0));
    	for (int i = 0; i < lastHighScoreList.length; i++) {
    		Entry e = lastHighScoreList[i];
    		float x = settings.width/2 - 500;
    		float y = settings.height/2 - 160+ i*40;
    		spriteBatch.renderString("" + (i + 1) + ". " + e.score + " " + e.name, new Vector3f(x,  y , 0), 0.3f);
    	}
    	spriteBatch.renderString("Press enter to return to main menu", new Vector3f(settings.height/2 - 150, settings.height/2 + 300,0), 0.3f);
        spriteBatch.end();
        activeProgram2D.disable();
    	window.swapBuffers();
    }
    
    private void renderHelp(){
    	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		initOrtho();
        spriteBatch.begin(activeProgram2D);
        Matrix4f scale = Matrix4f.scaleX(700).multiply(Matrix4f.scaleY(700));
        Matrix4f m = Matrix4f.translate(new Vector3f((settings.width - 700) / 2, (settings.height - 700) / 2, 0)).multiply(scale);
    	//spriteBatch.render(Texture.MENU_HELP, m);
    	
        float x = (settings.width)/2 - 210;
        float y = (settings.height)/2 - 50;
        
        spriteBatch.renderString("Description", new Vector3f(x-50, y-300, 0), 0.9f, new Vector3f(1,1,0));
        spriteBatch.renderString("Get as many customers to their destination as possible.", new Vector3f(x-300, y-200, 0), 0.3f);
        spriteBatch.renderString("You can have multiple customers in the taxi at the same time.", new Vector3f(x-300, y-160, 0), 0.3f);
        spriteBatch.renderString("You drive faster and get more score for each waiting customer.", new Vector3f(x-300, y-120, 0), 0.3f);
        spriteBatch.renderString("Each customer have a personal timer.", new Vector3f(x-300, y-80, 0), 0.3f);
        spriteBatch.renderString("If any timer runs to zero, game over.", new Vector3f(x-300, y-40, 0), 0.3f);
        spriteBatch.renderString("Controls", new Vector3f(x, y+30, 0), 0.9f, new Vector3f(1,1,0));
        spriteBatch.renderString("Key Up: Drive", new Vector3f(x-100,y+130,0), 0.3f);
        spriteBatch.renderString("Key Left/Right: Turn", new Vector3f(x-100,y+160,0), 0.3f);
        spriteBatch.renderString("Key Down: Brake", new Vector3f(x-100,y+190,0), 0.3f);
        spriteBatch.renderString("Space: Accept customer", new Vector3f(x-100,y+220,0), 0.3f);
        spriteBatch.renderString("S/D: Change radio channel", new Vector3f(x-100,y+250,0), 0.3f);
        spriteBatch.renderString("Press enter to return to main menu", new Vector3f(x-150,y+340,0), 0.3f);
        
        spriteBatch.end();
        activeProgram2D.disable();
    	window.swapBuffers();
    }
    private void renderMainMenu() {
    	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		initOrtho();
        spriteBatch.begin(activeProgram2D);
       // Matrix4f scale = Matrix4f.scaleX(570).multiply(Matrix4f.scaleY(85));
        //Matrix4f m = Matrix4f.translate(new Vector3f(100, 0, 0)).multiply(scale);
        //spriteBatch.render(Texture.YELLOW, m);
        Matrix4f scale = Matrix4f.scaleX(500).multiply(Matrix4f.scaleY(100));
        //Matrix4f m = Matrix4f.translate(new Vector3f(x,y,0)).multiply(scale);
        
        float x = (settings.width)/2 - 80;
        float y = (settings.height)/2 - 50;
        
        Vector3f white = new Vector3f(1,1,1);
        Vector3f gray = new Vector3f(32/255f,32/255f,32/255f);
        Matrix4f rect = Matrix4f.translate(new Vector3f(x-150, y-3+60*mainMenuSelected,0)).multiply(Matrix4f.scaleX(450)).multiply(Matrix4f.scaleY(50));
        spriteBatch.render(Texture.RECTANGLE, rect, new Vector3f(1,1,0));
        spriteBatch.renderString("Wolley Taxi", new Vector3f(x-230, y - 150,0), 1, new Vector3f(1,1,0));
        spriteBatch.renderString("Start", new Vector3f(x, y, 0), 0.5f, mainMenuSelected == 0 ? gray : white);
        spriteBatch.renderString("Highscore", new Vector3f(x-60,y+60,0),0.5f, mainMenuSelected == 1 ? gray : white);
        spriteBatch.renderString("Help", new Vector3f(x+10,y+120,0), 0.5f, mainMenuSelected == 2 ? gray : white);
        spriteBatch.renderString("Exit", new Vector3f(x+15,y+180,0), 0.5f, mainMenuSelected == 3 ? gray : white);
        spriteBatch.end();
        activeProgram2D.disable();
    	window.swapBuffers();
		
	}
    
    private void renderLoadingScreen() {
   
    	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		initOrtho();
        spriteBatch.begin(activeProgram2D);
        Matrix4f scale = Matrix4f.scaleX(settings.width).multiply(Matrix4f.scaleY(settings.height));
        Matrix4f m = Matrix4f.translate(new Vector3f(0, 0, 0)).multiply(scale);
       ///////////////
        String path = "textures/loadingscreen2.png";
        int width = 0;
        int height = 0;
        int[] pixels = null;
		try {

			InputStream in = Utilities.class.getResourceAsStream("/" + path);
			BufferedImage image =  ImageIO.read(in);

			width = image.getWidth();
			height = image.getHeight();
			pixels = new int[width * height];
			image.getRGB(0, 0, width, height, pixels, 0, width);
		} catch (IOException e) {
			e.printStackTrace();
		}

		int[] data = new int[width * height];
		for (int i = 0; i < width * height; i++) {
			int a = (pixels[i] & 0xff000000) >> 24;
			int r = (pixels[i] & 0xff0000) >> 16;
			int g = (pixels[i] & 0xff00) >> 8;
			int b = (pixels[i] & 0xff);

			data[i] = a << 24 | b << 16 | g << 8 | r;
		}

		int result = glGenTextures();
		
		glBindTexture(GL_TEXTURE_2D, result);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		
		
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, Utilities.createIntBuffer(data));
		
		glBindTexture(GL_TEXTURE_2D, 0);
        
        
        spriteBatch.render(result, m);
        spriteBatch.end();
        activeProgram2D.disable();
        window.swapBuffers();

	}
    
    int i = 0;
    

    
    
    public void initOrtho(){
    	 //2D rendering
    	int textureUniform;
    	int pv;
        activeProgram2D.enable();

        glActiveTexture(GL_TEXTURE0);
        textureUniform = activeProgram2D.getUniform("texSampler");
        glUniform1i(textureUniform, 0);
		Matrix4f proj = Matrix4f.orthoProjection(window.getWidth(), 0, 0, window.getHeight(), 0.1f, 100f); 
		pv = activeProgram2D.getUniform("PV");
		glUniformMatrix4fv(pv, true, proj.toFloatBuffer());
		
    }
    
	public void render(long tick){
		i++;
		//glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
       
		meshBatch.begin(activeProgram, camera, window);
        if (DEBUG_SHOW_TRAFFIC_NODES) {
        	for (Node n : trafficInfo.getNodes()) {
        		javax.vecmath.Matrix4f mat =  new javax.vecmath.Matrix4f();
    			mat.setIdentity();
    			mat.setScale(0.5f);
    			mat.setTranslation(new javax.vecmath.Vector3f(n.pos.x ,1, n.pos.z));
    			meshBatch.render(Mesh.TEXTURED_BOX_TEST, Texture.RED, mat);
    		}
        }
        if (DEBUG_SHOW_TRAFFIC_PATH) {
        	float p2 = (float) (((System.currentTimeMillis() * 0.1) % 100) * 0.01);
        	float p1 = 1 - p2;
        	for (Node n1 : trafficInfo.getNodes()) {
        		for (Node n2 : n1.next) {
        			javax.vecmath.Matrix4f mat =  new javax.vecmath.Matrix4f();
        			mat.setIdentity();
        			mat.setScale(0.5f);
        			mat.setTranslation(new javax.vecmath.Vector3f(n1.pos.x * p1 + n2.pos.x * p2 ,1, n1.pos.z * p1 + n2.pos.z * p2));
        			meshBatch.render(Mesh.TEXTURED_BOX_TEST, Texture.RED, mat);
        		}
        	}
        }
        
        if (DEBUG_SHOW_CAMERA_POS) {
        	javax.vecmath.Vector3f from = playerCar.getCenter();
        	from.y += 3; // Så att man inte träffar bilen
        	javax.vecmath.Vector3f v = physics.rayTest(from, new javax.vecmath.Vector3f(camera.pos.x, camera.pos.y, camera.pos.z));
        	if (v != null) {
        		javax.vecmath.Matrix4f mat =  new javax.vecmath.Matrix4f();
        		mat.setIdentity();
        		mat.setScale(0.5f);
        		mat.setTranslation(v);
        		meshBatch.render(Mesh.TEXTURED_BOX_TEST, Texture.RED, mat);
        	}
        }
        
        for (Entity e: entities) {
        	if (e.getState() != Entity.State.HIDDEN) {
        		e.render(meshBatch);
        	}
		}
        
        javax.vecmath.Matrix4f mat =  new javax.vecmath.Matrix4f();
		mat.setIdentity();
		mat.setScale(8000f);
		meshBatch.render(Mesh.SKYBOX, Texture.SKYBOX, mat);
        
		missionHandler.render3D(meshBatch);
		Matrix4f m = Matrix4f.translate(new Vector3f(-10,0,0)).multiply(Matrix4f.scale(20));
		//meshBatch.render(Mesh.QUAD, meshBatch.fbo.shadowmap, m);
        meshBatch.end();

 
        spriteBatch.begin(activeProgram2D);
        
		initOrtho();

		missionHandler.renderMiniMap(spriteBatch, activeProgram2D, activeProgramMap, tick);
		if (DEBUG_SHOW_FPS) {
			spriteBatch.renderString(""+current_FPS, new Vector3f(window.getWidth()-200,window.getHeight()-300,0.6f));
		}
		missionHandler.render2D(spriteBatch, tick);
		playerCar.render2D(spriteBatch);
		radio.render2D(spriteBatch, settings);
        spriteBatch.end();
        activeProgram2D.disable();
        window.swapBuffers();
        
    }
	
	
	public void free(){
		//radio.close();
		if(missionHandler != null) missionHandler.free();
		activeProgram.free();
		activeProgram2D.free();
		activeProgramMap.free();
		spriteBatch.free();
    	meshBatch.free();
    	window.destroy();
	}
	

	
	/*public static void main(String[] args) {
		new Game();
	}*/
}
