package entity;

import javax.vecmath.Vector3f;

import physics.Physics;
import GLEngine.Mesh;
import GLEngine.OBJLoader;
import GLEngine.Texture;

public class Skyscraper extends Entity {

	private static int TEXTURE_ID = new Texture("textures/skyscraper.png").textureID;
	public Skyscraper(Physics physics, Vector3f position) {
		super(physics);
		mesh = Mesh.SKYSCRAPER;
		body = physics.createStaticBox(mesh.getBounds(), position);
		textureID = TEXTURE_ID;
		setBodyUserPointer();
	}

}
