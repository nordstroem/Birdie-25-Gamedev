package entity;

import javax.vecmath.Vector3f;

import physics.Physics;
import GLEngine.Mesh;
import GLEngine.OBJLoader;
import GLEngine.Texture;

public class TrashCan extends Entity {
	
	private static int TEXTURE_ID = new Texture("textures/trashCan.png").textureID;
	public TrashCan(Physics physics, Vector3f position) {
		super(physics);
		mesh = Mesh.TRASHCAN;
		body = physics.createBox(mesh.getBounds(), position, 1f);
		textureID = TEXTURE_ID;
		setBodyUserPointer();
	}
	
	@Override
	public void update() {
		//body.setAngularVelocity(new Vector3f(2, 1, 0.5f));
	}
	
}
