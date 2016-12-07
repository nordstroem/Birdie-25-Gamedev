package entity;

import javax.vecmath.Vector3f;

import physics.Physics;
import GLEngine.Mesh;
import GLEngine.Texture;

public class Intersection extends Entity {

	private static int TEXTURE_ID = new Texture("textures/intersection.png").textureID;
	public Intersection(Physics physics, Vector3f position) {
		super(physics);
		mesh = Mesh.INTERSECTION;
		body = physics.createBodyFromMesh(mesh, position, 0f);
		textureID = TEXTURE_ID;
		setBodyUserPointer();
	}

}
