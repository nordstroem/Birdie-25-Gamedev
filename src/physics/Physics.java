package physics;

import java.util.ArrayList;

import javax.vecmath.Vector3f;

import GLEngine.Mesh;

import com.bulletphysics.BulletStats;
import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.broadphase.Dispatcher;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.CollisionWorld.ClosestRayResultCallback;
import com.bulletphysics.collision.dispatch.CollisionWorld.LocalRayResult;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.narrowphase.ManifoldPoint;
import com.bulletphysics.collision.narrowphase.PersistentManifold;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.BvhTriangleMeshShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.InternalTickCallback;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.dynamics.constraintsolver.ConstraintSolver;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.dynamics.constraintsolver.TypedConstraint;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.util.ObjectArrayList;

import entity.Entity;


public class Physics {
	// keep the collision shapes, for deletion/cleanup
		private ObjectArrayList<CollisionShape> collisionShapes = new ObjectArrayList<CollisionShape>();
		private BroadphaseInterface broadphase;
		private CollisionDispatcher dispatcher;
		private ConstraintSolver solver;
		private DefaultCollisionConfiguration collisionConfiguration;
		
		private static final float STEPSIZE = 5;
		
		public static int numObjects = 0;
		public static final int maxNumObjects = 16384;
		public static Transform[] startTransforms = new Transform[maxNumObjects];
		public static CollisionShape[] gShapePtr = new CollisionShape[maxNumObjects]; //1 rigidbody has 1 shape (no re-use of shapes)
		
		public static RigidBody pickedBody = null; // for deactivation state
		
		static {
			for (int i=0; i<startTransforms.length; i++) {
				startTransforms[i] = new Transform();
			}
		}
		
		// this is the most important class
		protected DynamicsWorld dynamicsWorld = null;

		// constraint for mouse picking
		protected TypedConstraint pickConstraint = null;

		
		public Physics() {
			init();
		}
		
		private void init(){
			// collision configuration contains default setup for memory, collision setup
			collisionConfiguration = new DefaultCollisionConfiguration();
			// use the default collision dispatcher. For parallel processing you can use a diffent dispatcher (see Extras/BulletMultiThreaded)
			dispatcher = new CollisionDispatcher(collisionConfiguration);
			broadphase = new DbvtBroadphase();
			// the default constraint solver. For parallel processing you can use a different solver (see Extras/BulletMultiThreaded)
			SequentialImpulseConstraintSolver sol = new SequentialImpulseConstraintSolver();
			solver = sol;
			dynamicsWorld = new DiscreteDynamicsWorld(dispatcher, broadphase, solver, collisionConfiguration);
			dynamicsWorld.setGravity(new Vector3f(0f, -10f, 0f));
			
			// http://gamedev.stackexchange.com/questions/73401/jbullet-detecting-when-2-objects-collide
			dynamicsWorld.setInternalTickCallback(new InternalTickCallback() {
			    @Override
			    public void internalTick(DynamicsWorld dynamicsWorld, float timeStep) {
			    	Dispatcher dispatcher = dynamicsWorld.getDispatcher();
			    	int manifoldCount = dispatcher.getNumManifolds();
			    	for (int i = 0; i < manifoldCount; i++) {
			    	    PersistentManifold manifold = dispatcher.getManifoldByIndexInternal(i);
			    	    // The following two lines are optional.
			    	    RigidBody object1 = (RigidBody)manifold.getBody0();
			    	    RigidBody object2 = (RigidBody)manifold.getBody1();
			    	    Entity entity1 = (Entity)object1.getUserPointer();
			    	    Entity entity2 = (Entity)object2.getUserPointer();
			    	    boolean hit = false;
			    	    Vector3f normal = null;
			    	    for (int j = 0; j < manifold.getNumContacts(); j++) {
			    	        ManifoldPoint contactPoint = manifold.getContactPoint(j);
			    	        if (contactPoint.getDistance() < 0.0f) {
			    	            hit = true;
			    	            normal = contactPoint.normalWorldOnB;
			    	            break;
			    	        }
			    	    }
			    	    if (hit) {
			    	    	if (entity1 != null && entity2 != null) {
			    	    		entity1.collidedWith(entity2);
			    	    		entity2.collidedWith(entity1);
			    	    	}
			    	        // Collision happened between physicsObject1 and physicsObject2. Collision normal is in variable 'normal'.
			    	    }
			    	}
			    }
			}, null);
			
			clientResetScene();
		}
		
		public synchronized void clientResetScene() {
			BulletStats.gNumDeepPenetrationChecks = 0;
			BulletStats.gNumGjkChecks = 0;

			int numObjects = 0;
			if (dynamicsWorld != null) {
				dynamicsWorld.stepSimulation(1f / 60f, 0);
				numObjects = dynamicsWorld.getNumCollisionObjects();
			}

			for (int i = 0; i < numObjects; i++) {
				CollisionObject colObj = dynamicsWorld.getCollisionObjectArray().getQuick(i);
				RigidBody body = RigidBody.upcast(colObj);
				if (body != null) {
					if (body.getMotionState() != null) {
						DefaultMotionState myMotionState = (DefaultMotionState) body.getMotionState();
						myMotionState.graphicsWorldTrans.set(myMotionState.startWorldTrans);
						colObj.setWorldTransform(myMotionState.graphicsWorldTrans);
						colObj.setInterpolationWorldTransform(myMotionState.startWorldTrans);
						colObj.activate();
					}
					// removed cached contact points
					dynamicsWorld.getBroadphase().getOverlappingPairCache().cleanProxyFromPairs(colObj.getBroadphaseHandle(), dynamicsWorld.getDispatcher());

					body = RigidBody.upcast(colObj);
					if (body != null && !body.isStaticObject()) {
						RigidBody.upcast(colObj).setLinearVelocity(new Vector3f(0f, 0f, 0f));
						RigidBody.upcast(colObj).setAngularVelocity(new Vector3f(0f, 0f, 0f));
					}
				}
			}
		}
		
		public synchronized void update(){
			// simple dynamics world doesn't handle fixed-time-stepping

			// step the simulation
			if (dynamicsWorld != null) {
				dynamicsWorld.stepSimulation(1f / 60f); // TODO ska det alltid vara 1f / 60f ?
			}
			
			/*for (int i = 0; i < 10; i++) {
				if (dynamicsWorld != null) {
					dynamicsWorld.stepSimulation(1f / 600f); // TODO ska det alltid vara 1f / 60f ?
				}
			}*/
		}
		
		private class CustomClosestRayResultCallback extends ClosestRayResultCallback{

			public CustomClosestRayResultCallback(Vector3f rayFromWorld, Vector3f rayToWorld) {
				super(rayFromWorld, rayToWorld);
			}
			Vector3f pos = null;
			public float addSingleResult(LocalRayResult lrr, boolean arg1) {
				Transform trans = new Transform();
				lrr.collisionObject.getWorldTransform(trans);
				double hf = lrr.hitFraction * 0.9; // Så att kameran hamnar lite framför
				double rhf = 1 - hf;
				pos = new Vector3f();
				pos.x = (float) (rayFromWorld.x * rhf + rayToWorld.x * hf);
				pos.y = (float) (rayFromWorld.y * rhf + rayToWorld.y * hf);
				pos.z = (float) (rayFromWorld.z * rhf + rayToWorld.z * hf);
				return 0;
			}
			
			
		}
		public synchronized Vector3f rayTest(Vector3f from, Vector3f towards){
			CustomClosestRayResultCallback c = new CustomClosestRayResultCallback(from, towards);
			dynamicsWorld.rayTest(from, towards, c);
			return c.pos;
		}
		
		public synchronized RigidBody createStaticBox(Vector3f size, Vector3f position) {
			return createBox(size, position, 0f);
		}
		
		public synchronized RigidBody createBox(Vector3f size, Vector3f position, float mass) {
			return createBox(size, position, mass, new Vector3f());
		}
		public synchronized RigidBody createBox(Vector3f size, Vector3f position, float mass, Vector3f rot) {
			CollisionShape shape = new BoxShape(size);
			collisionShapes.add(shape);
			
			Transform transform = new Transform();
			transform.setIdentity();
			transform.origin.set(position.x, position.y, position.z);
			/*transform.basis.rotX(rot.x);
			transform.basis.rotY(rot.y);
			transform.basis.rotZ(rot.z);*/
			transform.basis.rotY(rot.y);
			
			return localCreateRigidBody(mass, transform, shape);
		}
		
		public synchronized RigidBody createBodyFromMesh(Mesh mesh, Vector3f position, float mass){
			return createBodyFromMesh(mesh, position, mass, new Vector3f());
		}
		public synchronized RigidBody createBodyFromMesh(Mesh mesh, Vector3f position, float mass, Vector3f rot){
			TriangleMesh triangleMesh = new TriangleMesh(mesh);
			CollisionShape shape = new BvhTriangleMeshShape(triangleMesh, true);
			Transform transform = new Transform();
			transform.setIdentity();
			transform.origin.set(position.x, position.y, position.z);
			//transform.basis.rotX(rot.x);
			transform.basis.rotY(rot.y);
			//transform.basis.rotZ(rot.z);
			return localCreateRigidBody(mass, transform, shape);
		}
		
		public synchronized void remove(RigidBody body) {
			// TODO Är det så här man tar bort en body, kanske något mer som måste göras
			dynamicsWorld.removeRigidBody(body);
		}
		
		public synchronized void add(RigidBody body) {
			dynamicsWorld.addRigidBody(body);
		}
		
		public synchronized RigidBody localCreateRigidBody(float mass, Transform startTransform, CollisionShape shape) {
			// rigidbody is dynamic if and only if mass is non zero, otherwise static
			boolean isDynamic = (mass != 0f);

			Vector3f localInertia = new Vector3f(0f, 0f, 0f);
			if (isDynamic) {
				shape.calculateLocalInertia(mass, localInertia);
			}

			// using motionstate is recommended, it provides interpolation capabilities, and only synchronizes 'active' objects
			DefaultMotionState myMotionState = new DefaultMotionState(startTransform);
			
			RigidBodyConstructionInfo cInfo = new RigidBodyConstructionInfo(mass, myMotionState, shape, localInertia);
			
			RigidBody body = new RigidBody(cInfo);
			dynamicsWorld.addRigidBody(body);

			return body;
		}

		public synchronized ObjectArrayList<CollisionShape> getCollisionShapes() {
			return collisionShapes;
		}

		public synchronized DynamicsWorld getDynamicsWorld() {
			return dynamicsWorld;
		}
}
