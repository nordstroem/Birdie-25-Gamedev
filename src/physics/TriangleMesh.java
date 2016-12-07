package physics;

import GLEngine.Mesh;

import com.bulletphysics.collision.shapes.StridingMeshInterface;
import com.bulletphysics.collision.shapes.VertexData;

public class TriangleMesh extends StridingMeshInterface {

	private TriangleVertexData vertexData;
	public TriangleMesh(Mesh mesh) {
		this.vertexData = new TriangleVertexData(mesh);
	}
	
	@Override
	public VertexData getLockedReadOnlyVertexIndexBase(int arg0) {
		return vertexData;
	}

	@Override
	public VertexData getLockedVertexIndexBase(int arg0) {
		return vertexData;
	}

	@Override
	public int getNumSubParts() {
		return 1;
	}

	@Override
	public void preallocateIndices(int arg0) {

	}

	@Override
	public void preallocateVertices(int arg0) {
	}

	@Override
	public void unLockReadOnlyVertexBase(int arg0) {
	}

	@Override
	public void unLockVertexBase(int arg0) {
	}

}
