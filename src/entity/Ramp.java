package entity;

import javax.vecmath.Vector3f;

import GLEngine.Mesh;
import GLEngine.Texture;
import physics.Physics;

public class Ramp extends Entity {
	private static int TEXTURE_ID = new Texture("textures/red.png").textureID;
	public Ramp(Physics physics, Vector3f position) {
		super(physics);
		mesh = Mesh.RAMP;
		body = physics.createBodyFromMesh(mesh, position, 0f);
		textureID = TEXTURE_ID;
		setBodyUserPointer();
	}

}
