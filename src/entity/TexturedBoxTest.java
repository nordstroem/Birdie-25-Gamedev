package entity;

import GLEngine.Mesh;
import GLEngine.Texture;
import physics.Physics;

import javax.vecmath.Vector3f;

public class TexturedBoxTest extends Entity {
	private static int TEXTURE_ID = new Texture("textures/texture_test.png").textureID;
	public TexturedBoxTest(Physics physics, Vector3f position) {
		super(physics);
		mesh = Mesh.TEXTURED_BOX_TEST;
		body = physics.createBox(mesh.getBounds(), position, 1f);
		textureID = TEXTURE_ID;
		setBodyUserPointer();
	}
}
