package entity;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_UP;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;
import game.Camera;
import game.Game;
import game.MissionHandler;

import javax.vecmath.Vector3f;

import org.lwjgl.glfw.GLFW;

import physics.Physics;
import sound.Sound;
import GLEngine.InputHandler;
import GLEngine.Matrix4f;
import GLEngine.Mesh;
import GLEngine.MeshBatch;
import GLEngine.SpriteBatch;
import GLEngine.Texture;

import com.bulletphysics.linearmath.Transform;

public class PlayerVehicle extends VehicleEntity {
	
	
	private Sound sound;
	private Camera camera;
	
	private static VehicleInfo VEHICLE_INFO = new VehicleInfo();
	protected static int TEXTURE_ID = new Texture("textures/cars/LP_Car_11_MAP_1.png").textureID;
	static{
		VEHICLE_INFO.weight = 1000; //1000
		VEHICLE_INFO.maxEngineForce = 2000;
		VEHICLE_INFO.maxBackwardForce = -2000f; // -1400

		VEHICLE_INFO.wheelFriction = 100; //3000
		VEHICLE_INFO.suspensionStiffness = 30f; //20
		VEHICLE_INFO.suspensionDamping = 15f; //2.3
		VEHICLE_INFO.suspensionCompression = 20f; //4.4
		VEHICLE_INFO.rollInfluence = 1f; //1
		VEHICLE_INFO.suspensionRestLength = 1f; //1
		VEHICLE_INFO.steeringIncrement = 0.04f;
		VEHICLE_INFO.steeringClamp = 0.4f; // 0.3f
	}
	
	public PlayerVehicle(Physics physics, Vector3f position, Sound sound, Camera camera) {
		super(physics, position, new Vector3f(30, 0, 30), VEHICLE_INFO, TEXTURE_ID, Mesh.CAR_PLAYER);
		this.sound = sound;
		this.camera = camera;
		//mesh = Mesh.CAR_PLAYER;


		
		/*Vector3f v = new Vector3f();
		vehicle.getForwardVector(v);
		v.normalize();
		v.scale(-110000);
		vehicle.getRigidBody().applyTorque(v);*/
		
	}

	@Override
	public void update() {
		super.update();
		scaleTick--;
		
		if (isAutoTurnAvailable()) {
			ticksTurned += 0.1f;
			Vector3f v = new Vector3f();
			vehicle.getForwardVector(v);
			v.normalize();
			v.scale(-4000*ticksTurned);
			vehicle.getRigidBody().applyTorque(v);
			
			Vector3f force = new Vector3f(0, 1, 0);
			force.scale(500000*ticksTurned);
			//vehicle.getRigidBody().applyCentralForce(force);
		}else{
			ticksTurned = 1;
		}
	}
	
	private boolean isAutoTurnAvailable(){
		return Math.abs(vehicle.getCurrentSpeedKmHour()) < 5 && getUpVec().y < 0;
	}
	
	public void render2D(SpriteBatch spriteBatch) {
		/*if (isAutoTurnAvailable()) {
			Matrix4f scale = Matrix4f.scaleX(700).multiply(Matrix4f.scaleY(300));
			Matrix4f m = Matrix4f.translate(new GLEngine.Vector3f(300, 0, 0)).multiply(scale);
			spriteBatch.render(Texture.HOHOHAHA, m);
		}*/
	}
	
	
	private float ticksTurned = 1;
	public void updateInput() {
		if (InputHandler.pressed(GLFW_KEY_SPACE) && Game.DEBUG_KEYS) {
			Vector3f force = new Vector3f(0, 1, 0);
			force.scale(1000000);
			vehicle.getRigidBody().applyCentralForce(force);
		}
		
		if (InputHandler.down(GLFW_KEY_LEFT_SHIFT) && Game.DEBUG_KEYS) {
			Vector3f vec = new Vector3f();
			vehicle.getForwardVector(vec);
			vec.scale(20000);
			vehicle.getRigidBody().applyCentralForce(vec);
		}
		
		if (InputHandler.pressed(GLFW_KEY_UP)){
			sound.play(Sound.START);
		}
		if (InputHandler.down(GLFW_KEY_UP)) {
			sound.setMusic(Sound.FULL);
		}else{
			sound.setMusic(Sound.IDLE);
		}
		if (InputHandler.down(GLFW_KEY_UP)) {
			forward();
		}
		if (InputHandler.down(GLFW_KEY_DOWN) /*|| InputHandler.down(GLFW_KEY_SPACE)*/) {
			backward();
		}
		if (InputHandler.down(GLFW_KEY_LEFT)) {
			steerLeft();
		}
		if (InputHandler.down(GLFW_KEY_RIGHT)) {
			steerRight();
		}
		
		float torque = 2000;
		if (InputHandler.down(GLFW_KEY_W) && Game.DEBUG_KEYS) {
			Transform t1 = new Transform();
			Transform t2 = new Transform();
			vehicle.getWheelTransformWS(0, t1);
			vehicle.getWheelTransformWS(1, t2);
			Vector3f v = new Vector3f(t1.origin.x - t2.origin.x, t1.origin.y - t2.origin.y, t1.origin.z - t2.origin.z);
			v.normalize();
			v.scale(torque);
			
			vehicle.getRigidBody().applyTorque(v);
		}
		if (InputHandler.down(GLFW_KEY_S) && Game.DEBUG_KEYS) {
			Transform t1 = new Transform();
			Transform t2 = new Transform();
			vehicle.getWheelTransformWS(0, t1);
			vehicle.getWheelTransformWS(1, t2);
			Vector3f v = new Vector3f(t1.origin.x - t2.origin.x, t1.origin.y - t2.origin.y, t1.origin.z - t2.origin.z);
			v.normalize();
			v.scale(-torque);
			
			vehicle.getRigidBody().applyTorque(v);
		}
		if (InputHandler.down(GLFW_KEY_A) && Game.DEBUG_KEYS) {
			Vector3f v = new Vector3f();
			vehicle.getForwardVector(v);
			v.normalize();
			v.scale(-torque);
			vehicle.getRigidBody().applyTorque(v);
		}
		if (InputHandler.down(GLFW_KEY_D) && Game.DEBUG_KEYS) {
			Vector3f v = new Vector3f();
			vehicle.getForwardVector(v);
			v.normalize();
			v.scale(torque);
			vehicle.getRigidBody().applyTorque(v);
		}
		
		if (InputHandler.up(GLFW_KEY_LEFT) && InputHandler.up(GLFW_KEY_RIGHT)) {
			/*if (gVehicleSteering >= 0) {
				gVehicleSteering -= steeringIncrement;
			} else if (gVehicleSteering <= 0) {
				gVehicleSteering += steeringIncrement;
			}*/
			gVehicleSteering = 0;
		}
	}
	
	public void boost(){
		/*Vector3f vec = new Vector3f();
		vehicle.getForwardVector(vec);
		vec.scale(10000);
		vehicle.getRigidBody().applyCentralImpulse(vec);*/
		Vector3f force = new Vector3f(0, 1, 0);
		force.scale(1000000);
		vehicle.getRigidBody().applyCentralForce(force);
	}

	public Vector3f getUpVec(){
		Vector3f forward = new Vector3f();
		vehicle.getForwardVector(forward);
		forward.normalize();
		
		Transform t1 = new Transform();
		Transform t2 = new Transform();
		vehicle.getWheelTransformWS(0, t1);
		vehicle.getWheelTransformWS(1, t2);
		Vector3f side = new Vector3f(t1.origin.x - t2.origin.x, t1.origin.y - t2.origin.y, t1.origin.z - t2.origin.z);
		side.normalize();
		
		Vector3f up = new Vector3f();
		up.cross(forward, side);
		return up;
	}

	public Vector3f getForwardVec() {
		Vector3f vec = new Vector3f();
		vehicle.getForwardVector(vec);
		return vec;
	}
	public float getRotY() {
		Transform t1 = new Transform();
		Transform t2 = new Transform();
		vehicle.getWheelTransformWS(0, t1);
		vehicle.getWheelTransformWS(1, t2);
		Vector3f forward = getForwardVec();
		forward.normalize();
		if (forward.y > -1) {
			return (float) (Math.atan2(t1.origin.z - t2.origin.z, t1.origin.x - t2.origin.x) + Math.PI * 0.5);
		}
		return (float) (Math.PI + Math.atan2(t1.origin.z - t2.origin.z, t1.origin.x - t2.origin.x) + Math.PI * 0.5);
		/*Vector3f xz = new Vector3f(1, 0, 1);
		xz.normalize();
		
		Vector3f forward = getForwardVec();
		xz.scale(forward.dot(xz));
		
		
		return 0;*/
	}
	
	private long lastTimeCrashPlayed;
	public void collidedWith(Entity entity) {
		/*if (entity instanceof Skyscraper && vehicle.getCurrentSpeedKmHour() > 25) {
			sound.play3D(Sound.GLASS_BREAK, camera.pos, new GLEngine.Vector3f(getCenter().x, getCenter().y, getCenter().z));
		}*/
		if (entity instanceof VehicleEntity && System.currentTimeMillis() - lastTimeCrashPlayed > 2000) {
			lastTimeCrashPlayed = System.currentTimeMillis();
			VehicleEntity ve = (VehicleEntity)entity;
			double vol = Math.min(1, 0.01 * (Math.abs(ve.getCurrentSpeedKmHour()) + Math.abs(getCurrentSpeedKmHour())));
			sound.play(Sound.CRASH, vol);
		}
		if (entity instanceof DynamicEntity && System.currentTimeMillis() - lastTimeCrashPlayed > 2000) {
			DynamicEntity de = (DynamicEntity)entity;
			if (de.textureID == Texture.getTexture("textures/trashCan.png") || de.textureID == Texture.getTexture("textures/postbox.png")) {
				lastTimeCrashPlayed = System.currentTimeMillis();
				//VehicleEntity ve = (VehicleEntity)entity;
				double vol = Math.min(1, 0.01 * Math.abs(getCurrentSpeedKmHour()));
				sound.play(Sound.OBJECT_CRASH, vol);
			}
		}
	}

	public void updateStats(long tick, MissionHandler missionHandler) {
		int num = missionHandler.getCustomersInCar() + 0;
		maxEngineForce = 1700f  + num * 800;
		maxBackwardForce = -1700 - num  * 800; //= -800 - num  * 500;
		maxBreakingForce = 200 + num * 50;
		body.setGravity(new Vector3f(0,-10 + num * -4,0));
	}
	
	public void pickUpCustomer(){
		scaleTick = scaleTime;
	}
	
	int scaleTime = (int) (Game.FPS * 0.7);
	int scaleTick = -1;
	private float getScale(){
		if (scaleTick < 0) {
			return 1;
		}
		return (float) (1 + 0.4 * Math.sin(Math.PI * (float)scaleTick / (float)scaleTime));
	}
	
	protected void renderChassi(MeshBatch meshBatch){
		Transform trans = new Transform();
		body.getWorldTransform(trans);
		javax.vecmath.Matrix4f mat = new javax.vecmath.Matrix4f();
		trans.getMatrix(mat);
		mat.setScale(mat.getScale() * getScale());
		
		
		GLEngine.Matrix4f real = GLEngine.Matrix4f.identity();
		real.elements = new float[]{mat.m00
		,mat.m01
		,mat.m02
		,mat.m03
		,mat.m10
		,mat.m11
		,mat.m12
		,mat.m13
		,mat.m20
		,mat.m21
		,mat.m22
		,mat.m23
		,mat.m30
		,mat.m31
		,mat.m32
		,mat.m33};
		
		GLEngine.Matrix4f fix = GLEngine.Matrix4f.translate(new GLEngine.Vector3f(0, 0.4f, 0));
		real = real.multiply(fix);
		
		meshBatch.render(mesh, textureID, real);
	}
}
