package entity;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import com.bulletphysics.linearmath.Transform;

import physics.Physics;
import GLEngine.Mesh;
import GLEngine.MeshBatch;

public class DynamicEntity extends Entity {

	// Entities som skapas av data inläst från fil
	
	public DynamicEntity(Physics physics, Vector3f pos, Vector3f rot, Mesh mesh, String collision, int textureID, double weight) {
		super(physics);
		this.mesh = mesh;
		if (collision.equals("mesh")) {
			body = physics.createBodyFromMesh(mesh, pos, (float)weight, rot);
		} else if (collision.equals("box")) {
			Vector3f size = new Vector3f();
			Vector3f center = new Vector3f();
			mesh.getBounds(size, center);
			//pos.add(center);
			pos.x += center.x;
			pos.y += center.y;
			pos.z += center.z;
			boxTranslation = center;
			size.x *= 0.5f;
			size.y *= 0.5f;
			size.z *= 0.5f;
			body = physics.createBox(size, pos, (float)weight, rot);
			//body = physics.createBox(mesh.getBounds(), pos, (float)weight, rot);
		} else { // En separat mesh för collision
			body = physics.createBodyFromMesh(Mesh.getMesh(collision, 1), pos, (float)weight, rot);
		}
		this.textureID = textureID;
		setBodyUserPointer();
	}
	private Vector3f boxTranslation = new Vector3f(); // Om en modell har box collision och ej är symmetrisk måste positionen till center adderas vid skapande av
														// rigidbody. Detta leder till att vi måste translate så mycket åt motsatt håll vid utritning.
	public void render(MeshBatch meshBatch) {
		Transform trans = new Transform();
		body.getWorldTransform(trans);
		javax.vecmath.Matrix4f mat = new javax.vecmath.Matrix4f();
		trans.getMatrix(mat);
		//if (boxTranslation.y < 2) mat.setScale(mat.getScale() * 3);
		
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
		
		GLEngine.Matrix4f fix = GLEngine.Matrix4f.translate(new GLEngine.Vector3f(-boxTranslation.x, -boxTranslation.y, -boxTranslation.z));
		real = real.multiply(fix);
		//clone.setTranslation(new Vector3f(trans.origin.x - boxTranslation.x * 1, trans.origin.y - boxTranslation.y * 1, trans.origin.z - boxTranslation.z * 1));
		meshBatch.render(mesh, textureID, real);
	}
	
}
