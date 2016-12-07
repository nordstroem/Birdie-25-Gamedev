package entity;

import javax.vecmath.Vector3f;

import com.bulletphysics.linearmath.Transform;

import physics.Physics;
import GLEngine.Matrix4f;
import GLEngine.Mesh;
import GLEngine.MeshBatch;
import GLEngine.Texture;

public class Well extends Entity {
	private static int TEXTURE_ID = new Texture("textures/well.png").textureID;
	public Well(Physics physics, Vector3f position) {
		super(physics);
		mesh = Mesh.WELL;
		body = physics.createStaticBox(mesh.getBounds(), position);
		textureID = TEXTURE_ID;
		setBodyUserPointer();
	}

	/*public void render(MeshBatch meshBatch) {
		meshBatch.render(mesh, textureID, Matrix4f.identity());
	}*/
}
