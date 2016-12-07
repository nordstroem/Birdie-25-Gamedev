package game;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL20.glUniform4fv;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

import java.util.ArrayList;
import java.util.Collections;

import javax.sound.sampled.Clip;

import sound.Sound;
import GLEngine.FBO;
import GLEngine.GLSLProgram;
import GLEngine.InputHandler;
import GLEngine.Matrix4f;
import GLEngine.Mesh;
import GLEngine.MeshBatch;
import GLEngine.SpriteBatch;
import GLEngine.SpriteBatch.VAO;
import GLEngine.Texture;
import GLEngine.Utilities;
import GLEngine.Vector3f;
import GLEngine.Window;
import entity.PlayerVehicle;
public class MissionHandler {
	
	private Sound sound;
	public long score = 0;
	private ArrayList<WaitingCustomer> waitingCustomers = new ArrayList<WaitingCustomer>();
	private ArrayList<InCarCustomer> inCarCustomers = new ArrayList<InCarCustomer>();
	private PlayerVehicle playerVehicle;
	private ArrayList<Vector3f> nodes;
	//private ArrayList<Vector3f> targets;
	static FBO mapFBO;
	
	private static String names[] = {"Skalle-Per", "Bengt", "Lola", "Bosse", "Greta", "Anna", "Stenar", "Britt-Marie", "Sixten", "Hanna", "Ingrid", "Rudolf", "Anders", "T0rsteN",
		"Jocke", "Eva", "Cho", "Benny", "Lotta", "Johan", "Greger", "Fiolina", "Oskar", "Hans", "Ping", "Adam", "Lova", "Karin", "Georgios", "Milad", "Lina", "Tengil", "Erik", "Felix", 
		"Rolf", "Gunnar", "Emma", "Maja", "Sofia", "Frida", "Cecilia", "Kim", "Lisa", "Kajsa", "Dimitrios", "Chi", "David", "Janis", "Per", "Elisabeth", "Linnea", "Klara", "Lille-Mor",
		"Berit", "Neo", "Tilda", "Ali", "Jasmine", "Markus"};
	
	private static Vector3f colors[] = {new Vector3f(255/255f,0,0), new Vector3f(255/255f,255/255f, 0), new Vector3f(0,255/255f,59/255f), new Vector3f(0, 38/255f, 255/255f),
		new Vector3f(203/255f, 0, 255/255f), new Vector3f(0, 255/255f, 255/255f), new Vector3f(255/255f, 106/255f, 0), new Vector3f(1,1,1), new Vector3f(255/255f, 122/255f, 157/255f)};
	private int currentColor = 0;
	
	private static final float TARGET_RADIUS = 6.0f;
	
	public MissionHandler(PlayerVehicle playerVehicle, ArrayList<Vector3f> nodes, Sound sound) {
		this.playerVehicle = playerVehicle;
		this.nodes = nodes;
		mapFBO = new FBO(200,200, true);
		this.sound = sound;
	}
	
	public ScorePopup scorePopup = new ScorePopup();
	
	class ScorePopup{
		public int change = -0;
		public long startTick = -1000;
	}
	
	abstract class Customer implements Comparable<Customer>{
		
		String name = "";
		long deadline; 
		Vector3f targetPosition;
		boolean shouldBeRemoved = false;
		Vector3f color;
		
		public Customer(Vector3f color, String name){
			this.color = color;
			this.name = name;
		}
		
		public boolean tooLate(long tick) {
			return tick > deadline;
		}
		
		public int compareTo(Customer c) {
			if (this.deadline < c.deadline){
				return -1;
			}else if(this.deadline > c.deadline){
				return 1;
			}
			return 0;
		}
	}
	
	class InCarCustomer extends Customer{
		
		
		public InCarCustomer(WaitingCustomer wc, long tick, PlayerVehicle pv) {
			super(wc.color, wc.name);
			targetPosition = wc.targetPosition;
			float dist = targetPosition.distanceTo(new Vector3f(pv.getCenter().x, pv.getCenter().y, pv.getCenter().z));
			//deadline = wc.deadline + (int)(Math.random() * 20 + 40) * Game.FPS;
			deadline = getDeadLine(tick, dist);
			pv.pickUpCustomer();
			sound.play(Sound.PICK_UP);
		}

		public void update(long tick){
			if(targetPosition.distanceTo(Vector3f.toVector(playerVehicle.getCenter())) < TARGET_RADIUS){
				shouldBeRemoved = true;
				score += 100 * getCustomersInCar();
				scorePopup.change =  100 * getCustomersInCar();
				scorePopup.startTick = tick;
				sound.play(Sound.SCORE);
			}
		}



		
	}
	
	public long getDeadLine(long currentTick, float dist){
		return currentTick + (int)(15 + dist/10) * Game.FPS;
	}
	
	class WaitingCustomer extends Customer{
		Vector3f position;

		public WaitingCustomer(long createdTick, Vector3f color) {
			super(color, names[(int)(Math.random()*names.length)]);

			position = nodes.get((int)(Math.random() * nodes.size()));
			int i = (int)(Math.random() * nodes.size());
			int i0 = i;
			int minDis = 100;
			if(position.distanceTo(nodes.get(i)) < minDis){
				for(int j = i; j < nodes.size()*2; j++){
					if(nodes.get(i).distanceTo(nodes.get(j % nodes.size())) > minDis){ // TODO fick en out of bounds här, la in % nodes.size()
						i = j % nodes.size();
						break;
					}
				}
				if(i == i0){
					throw new RuntimeException("Can not find a good node");
				}
			}

			targetPosition = nodes.get(i);
			float dist = position.distanceTo(new Vector3f(playerVehicle.getCenter().x, playerVehicle.getCenter().y, playerVehicle.getCenter().z));
			deadline = getDeadLine(createdTick, dist);
		}
		
		
		public void update(long tick){

			if(position.distanceTo(Vector3f.toVector(playerVehicle.getCenter())) < TARGET_RADIUS){
				shouldBeRemoved = true;
				inCarCustomers.add(new InCarCustomer(this, tick, playerVehicle));
			}
		}
		
	}
	
	
	private long nextCounter = Game.FPS * 5;
	private boolean hasPotentialCustomer = false;
	private long potentialCounter;
	private Clip phoneClip = null;
	public boolean update(long tick) {
		potentialCounter--;

		if (potentialCounter == 0 && hasPotentialCustomer) {
			score -= 50;
			score = score < 0 ? 0 : score;
			scorePopup.change = -50;
			scorePopup.startTick = tick;
			sound.play(Sound.ERROR);
		}
		if (potentialCounter < 0) {
			hasPotentialCustomer = false;
		}
		
		nextCounter--;
		
		
		
		if (nextCounter <= 0 && !hasPotentialCustomer) {
			hasPotentialCustomer = true;
			potentialCounter = Game.FPS * 5;
			if (Game.DEBUG_SHORT_SPAWN_TIME) {
				nextCounter = (long)(Game.FPS * 5 * Math.random()) + 5 * Game.FPS;
			}else{
				nextCounter = (long)(/*Game.FPS * 15*Math.random()) + */15*Game.FPS);
			}
			phoneClip = sound.play(Sound.PHONE);
		}
		
		
		for (WaitingCustomer c : waitingCustomers) {
			if (c.tooLate(tick)) {
				return true;
			}
			c.update(tick);
		}
		
		for (InCarCustomer c : inCarCustomers) {
			if (c.tooLate(tick)) {
				return true;
			}
			c.update(tick);
		}
		
		for (int i = 0; i < waitingCustomers.size(); i++) {
			if (waitingCustomers.get(i).shouldBeRemoved) {
				waitingCustomers.remove(i);
				i--;
			}
		}
		
		for (int i = 0; i < inCarCustomers.size(); i++) {
			if (inCarCustomers.get(i).shouldBeRemoved) {
				inCarCustomers.remove(i);
				i--;
			}
		}
		return false;
		
	}

	public void updateInput(long tick) {
		if (InputHandler.down(GLFW_KEY_SPACE) && hasPotentialCustomer) {
			WaitingCustomer wc = new WaitingCustomer(tick, colors[currentColor % colors.length]);
			currentColor++;
			waitingCustomers.add(wc);
			hasPotentialCustomer = false;
			if (phoneClip != null) {
				phoneClip.close();
			}
		}
	}

	public void render2D(SpriteBatch spriteBatch, long tick) {
		
		
		Vector3f renderPos = new Vector3f(80, 20, 0);
		
		spriteBatch.renderString("Score:"+score, renderPos, 0.6f);
		if (hasPotentialCustomer && (potentialCounter > 60 * 2 || potentialCounter % 20 > 10)) {
			float xpos = Window.width/2 + 120;
			float ypos = Window.height/2 + 50;
			Matrix4f m = Matrix4f.translate(new Vector3f(xpos, ypos, 0)).multiply(Matrix4f.scale(50));
			//spriteBatch.render(Texture.NEW_CUSTOMER, m);
			spriteBatch.renderString("New customer!", new Vector3f(xpos -320, ypos + 50, 0), 0.6f, new Vector3f(1,1,1));
			spriteBatch.renderString("Accept with Space", new Vector3f(xpos -360, ypos + 100, 0), 0.6f, new Vector3f(1,1,1));
		}
		
		ArrayList<Customer> allCustomers = new ArrayList<Customer>();
		allCustomers.addAll(waitingCustomers);
		allCustomers.addAll(inCarCustomers);
		Collections.sort(allCustomers);
		
		for(int i = 0; i < allCustomers.size(); i++){
			Customer c = allCustomers.get(i);
			float ypos = 150 + i*100;
			float xpos = 20;
			Matrix4f m = Matrix4f.translate(new Vector3f(10, ypos, 0)).multiply(Matrix4f.scale(50));
			long timeLeft = (c.deadline - tick)/Game.FPS;
			Vector3f white = new Vector3f(1,1,1);
			Vector3f red = new Vector3f(1,0,0);
			if(c instanceof WaitingCustomer){
				spriteBatch.render(Texture.CIRCLE, m, c.color);
				spriteBatch.renderString(""+timeLeft, new Vector3f(xpos + 60, ypos + -8, 0), 0.7f, timeLeft <= 10 ? red : white);
				spriteBatch.renderString("Get " + c.name, new Vector3f(xpos -5, ypos + 60, 0), 0.3f);
			}else{
				spriteBatch.render(Texture.RECTANGLE, m, c.color);	
				spriteBatch.renderString(""+timeLeft, new Vector3f(xpos + 60, ypos + -8, 0), 0.7f, timeLeft <= 10 ? red : white);
				spriteBatch.renderString("Leave " + c.name, new Vector3f(xpos -5, ypos + 60, 0), 0.3f);
			}
		}
		

		//Render scorepopup
		Vector3f green = new Vector3f(0,1,0);
		Vector3f red = new Vector3f(1,0,0);
		if(tick - scorePopup.startTick <= 1*Game.FPS){
			String pre = scorePopup.change >= 0 ? "+" : "  -";
			spriteBatch.renderString(pre+Math.abs(scorePopup.change), new Vector3f(Window.width/2 - 100, Window.height/2 - 150, 0), 0.7f, scorePopup.change < 0 ? red : green);
		}
	}
	
	public void renderMiniMap(SpriteBatch spriteBatch, GLSLProgram shader, GLSLProgram activeProgramMap, long tick){
		
		int mapWidth = 300;
		float zoom = 0.75f;
		float scale = (float)Texture.MINIMAP_SIZE/mapWidth*zoom;
		float iconSize = 20;
		shader.disable();
		activeProgramMap.enable();
		glBindFramebuffer(GL_FRAMEBUFFER, mapFBO.fbo);
		glEnable(GL_BLEND);

		Matrix4f proj = Matrix4f.orthoProjection(Texture.MINIMAP_SIZE, 0, 0, Texture.MINIMAP_SIZE, 0.1f, 100f); 
		int pv = shader.getUniform("PV");
		int c = activeProgramMap.getUniform("color");
		
		glUniformMatrix4fv(pv, true, proj.toFloatBuffer());

		
		glViewport(0,0, mapFBO.width, mapFBO.height); // Render on the whole framebuffer, complete from the lower left corner to the upper right
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
      
		float x = playerVehicle.getCenter().x;
		float z = playerVehicle.getCenter().z;
		float carRot = (float) Math.toDegrees(playerVehicle.getRotY());
		
		//Translates
		Matrix4f trans1 = Matrix4f.translate(new Vector3f(-0.5f,-0.5f,0));
		Matrix4f trans2 = Matrix4f.translate(new Vector3f(0.5f,0.5f,0));
		Matrix4f view = Matrix4f.identity();
		
		
		//Map
		Matrix4f mm = Matrix4f.identity(); 
		mm = Matrix4f.scale(Texture.MINIMAP_SIZE*scale);
		view = Matrix4f.translate(new Vector3f(-z*scale, -x*scale, 0));
		view = Matrix4f.rotateZ(carRot).multiply(view);
		view = Matrix4f.translate(new Vector3f(Texture.MINIMAP_SIZE/2, Texture.MINIMAP_SIZE/2, 0)).multiply(view);
		mm = view.multiply(mm);
		
		VAO vao = SpriteBatch.quadVAO;
		int m = activeProgramMap.getUniform("M");


		
		glUniformMatrix4fv(m, true, mm.toFloatBuffer());
	    float[] color = {1.0f, 1.0f, 1.0f, 1.0f};
		glUniform4fv(c, Utilities.createFloatBuffer(color));
		glBindTexture(GL_TEXTURE_2D, Texture.MINIMAP);
		glBindVertexArray(vao.VAO);
		glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
		glBindVertexArray(0);
		glBindTexture(GL_TEXTURE_2D, 0);
		
		//wc
		long tt = tick % 60;

		for(WaitingCustomer wc : waitingCustomers){
			//if(tt > 30) break;
			int text = Texture.CIRCLE;
			Vector3f dis = wc.position.subtract(new Vector3f(x,playerVehicle.getCenter().y, z));
			dis.y = 0;
			float len = dis.length();
			Vector3f wcp = new Vector3f((wc.position.z)*scale, (wc.position.x)*scale, 0.0f);
			float cond = Texture.MINIMAP_SIZE * 0.5f/scale; 
			if(len > cond*0.88f) {
				dis = new Vector3f(dis.z, dis.x, 0);
				wcp = new Vector3f(z, x, 0).add(dis.normalized().multiply(cond*0.93f));
				wcp = wcp.multiply(scale);
				//text = Texture.TRIANGLE;
			}
			Matrix4f trans = Matrix4f.translate(wcp);
			Matrix4f fix = Matrix4f.translate(new Vector3f(-iconSize/2*scale, -iconSize/2*scale,0));
			mm = view.multiply(trans).multiply(fix).multiply(Matrix4f.scale(iconSize*scale));
			glUniformMatrix4fv(m, true, mm.toFloatBuffer());
		    float color2[] = {wc.color.x, wc.color.y, wc.color.z, 1};
			glUniform4fv(c, Utilities.createFloatBuffer(color2));
			glBindTexture(GL_TEXTURE_2D, text);
			glBindVertexArray(vao.VAO);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
			glBindVertexArray(0);
			glBindTexture(GL_TEXTURE_2D, 0);
		}
		
		//ic

		for(InCarCustomer wc : inCarCustomers){
			int text = Texture.RECTANGLE;
			//if(tt > 30) break;
		//	Matrix4f trans = Matrix4f.translate(new Vector3f((wc.targetPosition.z)*scale, (wc.targetPosition.x)*scale, 0.0f));
			//mm = Matrix4f.translate(new Vector3f(-2.5f, -2.5f,0)).multiply(view).multiply(trans).multiply(Matrix4f.scale(5*scale));
			
			Vector3f dis = wc.targetPosition.subtract(new Vector3f(x,playerVehicle.getCenter().y, z));
			dis.y = 0;
			float len = dis.length();
			Vector3f wcp = new Vector3f((wc.targetPosition.z)*scale, (wc.targetPosition.x)*scale, 0.0f);
			float cond = Texture.MINIMAP_SIZE * 0.5f/scale; 
			if(len > cond*0.94f) {
				dis = new Vector3f(dis.z, dis.x, 0);
				wcp = new Vector3f(z, x, 0).add(dis.normalized().multiply(cond*0.93f));
				wcp = wcp.multiply(scale);
			//	text = Texture.TRIANGLE;
			}
			Matrix4f trans = Matrix4f.translate(wcp);
			Matrix4f fix = Matrix4f.translate(new Vector3f(-iconSize/2*scale, -iconSize/2*scale,0));
			mm = view.multiply(trans).multiply(fix).multiply(Matrix4f.scale(iconSize*scale));
			glUniformMatrix4fv(m, true, mm.toFloatBuffer());
			float color2[] = {wc.color.x, wc.color.y, wc.color.z, 1};
			glUniform4fv(c, Utilities.createFloatBuffer(color2));
			glBindTexture(GL_TEXTURE_2D, text);
			glBindVertexArray(vao.VAO);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
			glBindVertexArray(0);
			glBindTexture(GL_TEXTURE_2D, 0);
		}
		activeProgramMap.disable();

		
        //RESTORE ORTHO
		shader.enable();
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
		glViewport(0,0, (int)Window.width, (int)Window.height); // Render on the whole framebuffer, complete from the lower left corner to the upper right
		proj = Matrix4f.orthoProjection(Window.width, 0, 0, Window.height, 0.1f, 100f); 
		pv = shader.getUniform("PV");
		glUniformMatrix4fv(pv, true, proj.toFloatBuffer());
		
		
		//Render fbo
		Matrix4f scal = Matrix4f.scale(mapWidth);
		trans1 = trans2.multiply(Matrix4f.rotateZ(-carRot)).multiply(trans1);
		Matrix4f t = Matrix4f.translate(new Vector3f(Window.width-mapWidth, 0, 0));
		Matrix4f m2 = t.multiply(scal);
		
		spriteBatch.render(Texture.CIRCLE2, m2);
		spriteBatch.render(mapFBO.shadowmap, m2);
		spriteBatch.render(Texture.MINIMAP_BORDER, m2);


		
		//Render car in center
		Matrix4f t2 = Matrix4f.translate(new Vector3f(mapWidth/2 -5, mapWidth/2-5, 0));
		Matrix4f large = t2.multiply(t).multiply(Matrix4f.scaleX(13)).multiply(Matrix4f.scaleY(13));
		Matrix4f small =  t2.multiply(t).multiply(Matrix4f.scaleX(10)).multiply(Matrix4f.scaleY(10));
	//	spriteBatch.render(Texture.RECTANGLE, large, new Vector3f(0,0,0));
		spriteBatch.render(Texture.TRIANGLE, large, new Vector3f(1,1,0));
	}
	
	public void render3D(MeshBatch meshBatch){
		for (WaitingCustomer c : waitingCustomers) {
			javax.vecmath.Matrix4f mat =  new javax.vecmath.Matrix4f();
    		mat.setIdentity();
    		mat.setScale(1f);
    		mat.rotY((float) (-Math.atan2(c.position.z  - playerVehicle.getCenter().z, c.position.x  - playerVehicle.getCenter().x) - Math.PI));
    		mat.setTranslation(new javax.vecmath.Vector3f(c.position.x, c.position.y - 0.6f, c.position.z));
    		meshBatch.render(Mesh.FOX, Texture.FOX, mat, new Vector3f(1,1,1));
    		mat.setIdentity();
    		mat.setScale(1f);
    		mat.setTranslation(new javax.vecmath.Vector3f(c.position.x, c.position.y + 6f, c.position.z));
    		meshBatch.render(Mesh.ARROW, Texture.RECTANGLE, mat, c.color);
		}
		
		for (InCarCustomer c : inCarCustomers) {
			javax.vecmath.Matrix4f mat =  new javax.vecmath.Matrix4f();
    		mat.setIdentity();
    		mat.setScale(1f);
    		mat.setTranslation(new javax.vecmath.Vector3f(c.targetPosition.x, c.targetPosition.y + 2, c.targetPosition.z));
    		meshBatch.render(Mesh.ARROW, Texture.RECTANGLE, mat, c.color);
		}
	}

	public long getScore() {
		return score;
	}


	public int getCustomersInCar() {
		return inCarCustomers.size() + waitingCustomers.size();
	}
	
	public void free(){
		mapFBO.free();
	}
}
