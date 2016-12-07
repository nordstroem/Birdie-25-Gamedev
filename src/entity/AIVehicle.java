package entity;

import game.Game;
import game.TrafficInfo;
import game.TrafficInfo.Node;

import javax.vecmath.Vector3f;

import com.bulletphysics.linearmath.Transform;

import physics.Physics;
import GLEngine.Mesh;
import GLEngine.MeshBatch;
import GLEngine.Texture;

public class AIVehicle extends VehicleEntity {

	private static int[] TEXTURE_IDS = new int[]{new Texture("textures/cars/LP_Car2_2_MAP2.png").textureID, new Texture("textures/cars/LP_Car2_2_MAP.png").textureID,
		new Texture("textures/cars/LP_Car2_2_MAP3.png").textureID,new Texture("textures/cars/LP_Car2_2_MAP4.png").textureID,new Texture("textures/cars/LP_Car2_2_MAP5.png").textureID};
	private Node next;
	private TrafficInfo trafficInfo;
	
	private static VehicleInfo VEHICLE_INFO = new VehicleInfo();
	static{
		VEHICLE_INFO.weight = 200; //800
	}
	
	public AIVehicle(Physics physics, Vector3f position, TrafficInfo trafficInfo, Node next) {
		super(physics, position, new Vector3f(next.pos.x, 0, next.pos.z), VEHICLE_INFO, TEXTURE_IDS[(int)(Math.random() * TEXTURE_IDS.length)], Mesh.CAR_AI);
		setBodyUserPointer();
		this.next = next; 
		this.trafficInfo = trafficInfo;
		
		maxEngineForce = 4000f;//1000.f;//this should be engine/velocity dependent
		maxBreakingForce = 100.f;

		gVehicleSteering = 0.f;
		steeringIncrement = 0.05f;
		steeringClamp = 0.8f;
		
		trafficInfo.addVehicle(this);
	}

	@Override
	public void update() {
		
		
		if (next != null) {
			float dx = next.pos.x - getCenter().x;
			//float dy = next.pos.y - getCenter().y;
			float dy = 0;
			float dz = next.pos.z - getCenter().z;
			
			Vector3f target = new Vector3f(dx, 0, dz);
			target.normalize();
			
			Vector3f forward = new Vector3f();
			vehicle.getForwardVector(forward);
			forward.normalize();
			double angle = Math.atan2( forward.x*target.z - forward.z*target.x,forward.x*target.x + forward.z*target.z );
			gVehicleSteering = (float)-angle;
			if (gVehicleSteering > steeringClamp) {
				gVehicleSteering = steeringClamp;
			}else if (gVehicleSteering < -steeringClamp) {
				gVehicleSteering = -steeringClamp;
			}
			
			if (vehicle.getCurrentSpeedKmHour() > 0 && shouldBreak()) {
				backward();
			} else if ((vehicle.getCurrentSpeedKmHour() < 30 && Math.abs(angle) < 0.1) || vehicle.getCurrentSpeedKmHour() < 10) {
				forward();
			}
			
			double thres = 3; // 6
			if (Math.sqrt(dx*dx + dy*dy + dz*dz) < thres) {
				next = next.getNext();
			}
		}
		
		super.update();
	}
	
	private boolean shouldBreak() {
		for (VehicleEntity v : trafficInfo.getCarsWithin(this, 7)) {
			float dx = v.getCenter().x - getCenter().x;
			float dz = v.getCenter().z - getCenter().z;
			
			Vector3f target = new Vector3f(dx, 0, dz);
			target.normalize();
			
			Vector3f forward = new Vector3f();
			vehicle.getForwardVector(forward);
			forward.normalize();
			double angle = Math.atan2( forward.x*target.z - forward.z*target.x,forward.x*target.x + forward.z*target.z );
			if (Math.abs(angle) < Math.toRadians(20)) {
				return true;
			}
		}
		return false;
	}

	public void render(MeshBatch meshBatch) {
		renderChassi(meshBatch);
		renderWheels(meshBatch);
		renderShadow(meshBatch);
		if (Game.DEBUG_SHOW_VEHICLE_NEXT_NODE && next != null) {
			javax.vecmath.Matrix4f mat = new javax.vecmath.Matrix4f();
			mat.setIdentity();
			mat.setTranslation(new Vector3f(next.pos.x, next.pos.y, next.pos.z));
			meshBatch.render(Mesh.TEXTURED_BOX_TEST, Texture.RED, mat);
		}
		
		if (Game.DEBUG_SHOW_AI_BREAK && shouldBreak()) {
			javax.vecmath.Matrix4f mat = new javax.vecmath.Matrix4f();
			mat.setIdentity();
			Vector3f center = getCenter();
			center.y += 2;
			mat.setTranslation(center);
			meshBatch.render(Mesh.TEXTURED_BOX_TEST, Texture.RED, mat);
		}
	}
}
