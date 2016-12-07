package physics;

import javax.vecmath.Tuple3f;

import GLEngine.Mesh;

import com.bulletphysics.collision.shapes.VertexData;

public class TriangleVertexData extends VertexData {
	
	private Mesh mesh;
	public TriangleVertexData(Mesh mesh) {
		this.mesh = mesh;
	}

	@Override
	public int getIndex(int i) {
		return mesh.indices[i];
	}

	@Override
	public int getIndexCount() {
		return mesh.indices.length;
	}

	@Override
	public <T extends Tuple3f> T getVertex(int v, T tuple) {
		tuple.x = mesh.vertices[v * 3 + 0];
		tuple.y = mesh.vertices[v * 3 + 1];
		tuple.z = mesh.vertices[v * 3 + 2];
		return tuple;
	}

	@Override
	public int getVertexCount() {
		return mesh.vertices.length;
	}

	@Override
	public void setVertex(int i, float x, float y, float z) {
		mesh.vertices[i * 3 + 0] = x;
		mesh.vertices[i * 3 + 1] = y;
		mesh.vertices[i * 3 + 2] = z;
	}

}
