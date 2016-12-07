package game;

import java.util.ArrayList;

import physics.Physics;
import GLEngine.Matrix4f;
import GLEngine.Vector3f;
import entity.PlayerVehicle;

public class Camera {

	public Vector3f pos = new Vector3f();
	public Vector3f rot = new Vector3f();
	public Vector3f target = new Vector3f();
	
	private PlayerVehicle pv;
	private boolean isFollowing;
	private Physics physics;
	
	public Camera(Physics physics) {
		this.physics = physics;
	}
	
	public void follow(PlayerVehicle pv) {
		this.pv = pv;
		isFollowing = true;
	}
	
	public void stopFollowing(){
		isFollowing = false;
	}

	public Vector3f getPosition(){
		return pos;
	}
	
	private ArrayList<javax.vecmath.Vector3f> forwards = new ArrayList<javax.vecmath.Vector3f>();
	private ArrayList<Float> rotys = new ArrayList<Float>();
	private int delayTicks = 15;
	private double minDistance = 10;
	private double maxDistance = 20;
	private double flexibleLength = maxDistance - minDistance;
	private double maxDistanceSpeedKmHour = 120;
	

	public Matrix4f getViewMatrix() {
		if(isFollowing){
			Vector3f c = new Vector3f(pv.getCenter().x, pv.getCenter().y, pv.getCenter().z);
			float length = (float) Math.abs((minDistance + Math.min(flexibleLength, flexibleLength * Math.abs(pv.vehicle.getCurrentSpeedKmHour())  / maxDistanceSpeedKmHour)));
			
			float height = length / 1.9f;
			javax.vecmath.Vector3f carUp = pv.getUpVec();
			carUp.normalize();
			javax.vecmath.Vector3f forward = pv.getForwardVec();
			forwards.add(forward);
			float roty = pv.getRotY();
			rotys.add(roty);
			if (forwards.size() == delayTicks) {
				forward = forwards.remove(0);
				roty = rotys.remove(0);
			}
			forward.normalize();
			forward.x = -forward.x;
			forward.y = -forward.y;
			forward.z = -forward.z;
			pos.x = (float) (c.x + Math.cos(roty - Math.PI) * length);
			//pos.y = c.y + length * forward.y + height * carUp.y;
			pos.y = c.y + height;
			pos.z = (float) (c.z + Math.sin(roty - Math.PI) * length);
			javax.vecmath.Vector3f from = pv.getCenter();
			//from.y += c.y + 3; // Så man inte träffar bilen
			from.y += c.y < 0 ? 2 : 4; // Så man inte träffar bilen 2
			
			javax.vecmath.Vector3f v = physics.rayTest(from, new javax.vecmath.Vector3f(pos.x, pos.y, pos.z));
			if (v != null) {
				pos.x = v.x;
				pos.y = v.y;
				pos.z = v.z;
			}
			target = c;
			return Matrix4f.lookAt(pos, c, new Vector3f(0, 1, 0));
		}else{
			return Matrix4f.rotateX(-rot.x).multiply(Matrix4f.rotateY(-rot.y)).multiply(Matrix4f.translate(pos.multiply(-1)));
		}
	}
}
