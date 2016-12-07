package entity;



import static org.lwjgl.opengl.GL11.*;

import javax.vecmath.Vector3f;

import GLEngine.Matrix4f;
import GLEngine.Mesh;
import GLEngine.MeshBatch;
import GLEngine.Texture;

import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.CompoundShape;
import com.bulletphysics.collision.shapes.CompoundShapeChild;
import com.bulletphysics.collision.shapes.CylinderShapeX;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.vehicle.DefaultVehicleRaycaster;
import com.bulletphysics.dynamics.vehicle.RaycastVehicle;
import com.bulletphysics.dynamics.vehicle.VehicleRaycaster;
import com.bulletphysics.dynamics.vehicle.VehicleTuning;
import com.bulletphysics.dynamics.vehicle.WheelInfo;
import com.bulletphysics.linearmath.Transform;

import physics.Physics;

public class VehicleEntity extends Entity {
	//private static final float CUBE_HALF_EXTENTS = 0.5f;//1; //int
	protected float wheelAxelDistance = 2; //2
	protected float wheelFrontBackDistance = 2.2f;
	protected final int rightIndex = 0;
	protected final int upIndex = 1;
	protected final int forwardIndex = 2;
	protected final Vector3f wheelDirectionCS0 = new Vector3f(0,-1,0);
	protected final Vector3f wheelAxleCS = new Vector3f(-1,0,0);

	public float gEngineForce = 0.f;
	public float gBreakingForce = 0.f;

	protected static float maxEngineForce = 5000f;//1000.f;//this should be engine/velocity dependent
	protected static float maxBackwardForce = -500f;
	protected static float maxBreakingForce = 100.f;

	protected float gVehicleSteering = 0.f;
	protected float steeringIncrement = 0.1f; //0.04
	protected float steeringClamp = 0.6f; // 0.3f
	public float wheelRadius = 0.4f;
	public float wheelWidth = 0.6f; //0.4f;
	protected float wheelFriction = 3000; //1000
	protected float suspensionStiffness = 20.f; // 20
	protected float suspensionDamping = 2.3f; //2.3
	protected float suspensionCompression = 4.4f; //4.4
	protected float rollInfluence = 0.1f;//0.1f;

	protected float suspensionRestLength = 1f; //0.6

	protected RigidBody carChassis;

	protected VehicleTuning tuning = new VehicleTuning();
	protected VehicleRaycaster vehicleRayCaster;
	public RaycastVehicle vehicle;

	//protected static int TEXTURE_ID = new Texture("textures/cars/LP_Car2_2_MAP2.png").textureID; //lpcarmap6
	protected static int WHEEL_TEXTURE_ID = new Texture("textures/wheel.png").textureID;

	public VehicleEntity(Physics physics, Vector3f position, Vector3f headingTarget, VehicleInfo vehicleInfo, int textureID, Mesh mesh) {
		super(physics);

		maxEngineForce = vehicleInfo.maxEngineForce;
		wheelFriction = vehicleInfo.wheelFriction;
		suspensionStiffness = vehicleInfo.suspensionStiffness;
		suspensionDamping = vehicleInfo.suspensionDamping;
		suspensionCompression = vehicleInfo.suspensionCompression;
		rollInfluence = vehicleInfo.rollInfluence;
		suspensionRestLength = vehicleInfo.suspensionRestLength;

		steeringIncrement = vehicleInfo.steeringIncrement;
		steeringClamp = vehicleInfo.steeringClamp;

		maxBackwardForce = vehicleInfo.maxBackwardForce;
		this.textureID = textureID; 
		Transform tr = new Transform();
		tr.setIdentity();

		tuning.suspensionCompression = suspensionCompression;
		tuning.suspensionDamping = suspensionDamping;
		tuning.suspensionStiffness = suspensionStiffness;
		
		
		CollisionShape chassisShape = new BoxShape(new Vector3f(1.0f, 0.75f, 2.5f)); // 1 0.5 2
		physics.getCollisionShapes().add(chassisShape);

		CompoundShape compound = new CompoundShape();
		physics.getCollisionShapes().add(compound);
		Transform localTrans = new Transform();
		localTrans.setIdentity();
		// localTrans effectively shifts the center of mass with respect to the chassis
		localTrans.origin.set(0, 1f, 0.5f); // 0 1 0

		compound.addChildShape(localTrans, chassisShape);

		//tr.origin.set(0, 0, 0);
		tr.origin.set(position);

		float dx = position.x - headingTarget.x;
		float dz = position.z - headingTarget.z;
		float angle = (float)Math.atan2(-dz, dx);
		//double angle = Math.atan2( position.x*headingTarget.z - position.z*headingTarget.x, position.x*headingTarget.x + position.z*headingTarget.z );
		tr.basis.rotY(angle - 1.5f);

		carChassis = physics.localCreateRigidBody(vehicleInfo.weight, tr, compound); //chassisShape); // 800

		// create vehicle

		vehicleRayCaster = new DefaultVehicleRaycaster(physics.getDynamicsWorld());
		vehicle = new RaycastVehicle(tuning, carChassis, vehicleRayCaster);

		// never deactivate the vehicle
		carChassis.setActivationState(CollisionObject.DISABLE_DEACTIVATION);
		physics.getDynamicsWorld().addVehicle(vehicle);

		float connectionHeight = 1.2f; // 1.2

		boolean isFrontWheel = true;

		// choose coordinate system
		vehicle.setCoordinateSystem(rightIndex, upIndex, forwardIndex);

		//Vector3f connectionPointCS0 = new Vector3f(CUBE_HALF_EXTENTS - (0.3f * wheelWidth), connectionHeight, 2f * CUBE_HALF_EXTENTS - wheelRadius);
		Vector3f connectionPointCS0 = new Vector3f(wheelAxelDistance*0.5f - (0.3f * wheelWidth), connectionHeight, 2f * wheelFrontBackDistance*0.5f - wheelRadius);
		vehicle.addWheel(connectionPointCS0, wheelDirectionCS0, wheelAxleCS, suspensionRestLength, wheelRadius, tuning, isFrontWheel);

		//connectionPointCS0.set(-CUBE_HALF_EXTENTS + (0.3f * wheelWidth), connectionHeight, 2f * CUBE_HALF_EXTENTS - wheelRadius);
		connectionPointCS0.set(-wheelAxelDistance*0.5f + (0.3f * wheelWidth), connectionHeight, 2f * wheelFrontBackDistance*0.5f - wheelRadius);
		vehicle.addWheel(connectionPointCS0, wheelDirectionCS0, wheelAxleCS, suspensionRestLength, wheelRadius, tuning, isFrontWheel);

		//connectionPointCS0.set(-CUBE_HALF_EXTENTS + (0.3f * wheelWidth), connectionHeight, -2f * CUBE_HALF_EXTENTS + wheelRadius);
		connectionPointCS0.set(-wheelAxelDistance*0.5f + (0.3f * wheelWidth), connectionHeight, -2f * wheelFrontBackDistance*0.5f + wheelRadius);
		isFrontWheel = false;
		vehicle.addWheel(connectionPointCS0, wheelDirectionCS0, wheelAxleCS, suspensionRestLength, wheelRadius, tuning, isFrontWheel);

		//connectionPointCS0.set(CUBE_HALF_EXTENTS - (0.3f * wheelWidth), connectionHeight, -2f * CUBE_HALF_EXTENTS + wheelRadius);
		connectionPointCS0.set(wheelAxelDistance*0.5f - (0.3f * wheelWidth), connectionHeight, -2f * wheelFrontBackDistance*0.5f + wheelRadius);
		vehicle.addWheel(connectionPointCS0, wheelDirectionCS0, wheelAxleCS, suspensionRestLength, wheelRadius, tuning, isFrontWheel);

		for (int i = 0; i < vehicle.getNumWheels(); i++) {
			WheelInfo wheel = vehicle.getWheelInfo(i);
			wheel.suspensionStiffness = suspensionStiffness;
			wheel.wheelsDampingRelaxation = suspensionDamping;
			wheel.wheelsDampingCompression = suspensionCompression;
			wheel.frictionSlip = wheelFriction;
			wheel.rollInfluence = rollInfluence;
		}
		compound.setUserPointer(new Entity(null){
			@Override
			public void collidedWith(Entity entity) {
				System.out.println("comp " + entity);
			}
		});
		//vehicle = new Vehicle(physics, position);
		body = carChassis;
		this.mesh = mesh;
		setBodyUserPointer();
	}

	@Override
	public void update() {
		// man kan välja här om vilka hjul man ska ha kraft på, framhjulen verkar kunna var bättre
		int wheelIndex = 0; //2
		vehicle.applyEngineForce(gEngineForce,wheelIndex);
		vehicle.setBrake(gBreakingForce,wheelIndex);
		wheelIndex = 1; //3
		vehicle.applyEngineForce(gEngineForce,wheelIndex);
		vehicle.setBrake(gBreakingForce,wheelIndex);

		wheelIndex = 0;
		vehicle.setSteeringValue(gVehicleSteering,wheelIndex);
		wheelIndex = 1;
		vehicle.setSteeringValue(gVehicleSteering,wheelIndex);

		gEngineForce = 0f;
		gBreakingForce = 0f;
	}

	public void forward() {
		gEngineForce = maxEngineForce * speedMul();
		//gBreakingForce = 0.f;
	}

	public void backward() {
		if (vehicle.getCurrentSpeedKmHour() > 1) {
			gBreakingForce = maxBreakingForce;
			//gEngineForce = 0.f;

		} else {
			gEngineForce = maxBackwardForce  * speedMul();
			//gBreakingForce = 0f;
		}
	}
	private float speedMul(){
		 return (Math.abs(vehicle.getCurrentSpeedKmHour())  < 40 ? 5 : 1);
	}

	public void steerLeft() {
		float frac = 1 - Math.abs(vehicle.getCurrentSpeedKmHour())  / 200;
		frac = frac > 1 ? 1 : frac;
		frac = frac < 0.3 ? 0.3f : frac;
		float clamp = steeringClamp * frac;
		gVehicleSteering += steeringIncrement;
		if (gVehicleSteering > clamp) {
			gVehicleSteering = clamp;
		}
	}

	public void steerRight() {
		float frac = 1 - Math.abs(vehicle.getCurrentSpeedKmHour())  / 200;
		frac = frac > 1 ? 1 : frac;
		frac = frac < 0.3 ? 0.3f : frac;
		float clamp = steeringClamp * frac;
		gVehicleSteering -= steeringIncrement;
		if (gVehicleSteering < -clamp) {
			gVehicleSteering = -clamp;
		}
	}

	public void render(MeshBatch meshBatch) {
		renderChassi(meshBatch);
		renderWheels(meshBatch);
		renderShadow(meshBatch);
	}

	protected void renderChassi(MeshBatch meshBatch){
		Transform trans = new Transform();
		body.getWorldTransform(trans);
		javax.vecmath.Matrix4f mat = new javax.vecmath.Matrix4f();
		trans.getMatrix(mat);
		
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

	protected void renderWheels(MeshBatch meshBatch){
		javax.vecmath.Matrix4f mat = new javax.vecmath.Matrix4f();
		CylinderShapeX wheelShape = new CylinderShapeX(new Vector3f(wheelWidth, wheelRadius, wheelRadius));
		for (int i = 0; i < vehicle.getNumWheels(); i++) {
			// synchronize the wheels with the (interpolated) chassis worldtransform
			vehicle.updateWheelTransform(i, true);

			// draw wheels (cylinders)
			Transform trans = vehicle.getWheelInfo(i).worldTransform;
			//GLShapeDrawer.drawOpenGL(gl, trans, wheelShape, wheelColor, getDebugMode());
			float[] radius = new float[1];
			Vector3f center = new Vector3f();
			wheelShape.getBoundingSphere(center, radius);
			mat = new javax.vecmath.Matrix4f();
			trans.getMatrix(mat);
			mat.setScale(wheelRadius * 2);
			//mat.transform(center);

			meshBatch.render(Mesh.getMesh("objfiles/wheel.obj", 1), WHEEL_TEXTURE_ID, mat);
		}
	}
	
	protected void renderShadow(MeshBatch meshBatch){

	}

	public double getCurrentSpeedKmHour() {
		return vehicle.getCurrentSpeedKmHour();
	}
	
}
class VehicleInfo{
	public float weight = 1500;
	public float maxEngineForce = 5000f;
	public float wheelFriction = 3000; //1000
	public float suspensionStiffness = 20.f; // 20
	public float suspensionDamping = 2.3f; //2.3
	public float suspensionCompression = 4.4f; //4.4
	public float rollInfluence = 0.1f;//0.1f;
	public float suspensionRestLength = 1f; //0.6

	public float steeringIncrement = 0.1f; //0.04
	public float steeringClamp = 0.6f; // 0.3f

	public float maxBackwardForce = -500f;
}
