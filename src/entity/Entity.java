package entity;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import physics.Physics;
import GLEngine.Mesh;
import GLEngine.MeshBatch;
import GLEngine.Texture;

import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.linearmath.Transform;

import entity.Entity.State;

public abstract class Entity {
	protected RigidBody body;
	protected Physics physics;
	protected Mesh mesh;
	protected int textureID;
	protected State state = State.VISUAL_PHYSICS;
	public static enum State{
		HIDDEN, VISUAL_ONLY, VISUAL_PHYSICS;
	}
	public Entity(Physics physics) {
		this.physics = physics;
		//CollisionObject.setUserPointer();
	}
	
	// Call after body has been initialized
	protected void setBodyUserPointer() {
		body.setUserPointer(this);
	}
	
	public void update() {
		
	}

	public void render(MeshBatch meshBatch) {
		Transform trans = new Transform();
		body.getWorldTransform(trans);
		javax.vecmath.Matrix4f mat = new javax.vecmath.Matrix4f();
		trans.getMatrix(mat);
		meshBatch.render(mesh, textureID, mat);
	}
	
	public Vector3f getCenter() {
		Transform trans = new Transform();
		body.getWorldTransform(trans);
		return trans.origin;
	}
	
	public RigidBody getBody(){
		return body;
	}

	public void collidedWith(Entity entity) {
		// TODO Auto-generated method stub
	}
	
	
	public State getState(){
		return state;
	}

	public void setState(State newState) {
		if (newState == State.HIDDEN || newState == State.VISUAL_ONLY) {
			if (state == State.VISUAL_PHYSICS) {
				physics.remove(body);
			}
		} else if (newState == State.VISUAL_PHYSICS) {
			if (state == State.HIDDEN || state == State.VISUAL_ONLY) {
				physics.add(body);
			}
		}
		state = newState;
	}

	public boolean isStatic() {
		return body.getInvMass() == 0;
	}

	public void cleanUp() {

	}
}

