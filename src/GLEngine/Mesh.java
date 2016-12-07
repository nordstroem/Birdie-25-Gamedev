package GLEngine;

import java.util.HashMap;

import javax.vecmath.Vector3f;

import GLEngine.OBJLoader.Material;

public class Mesh {

	private static HashMap<MeshKey, Mesh> meshMap = new HashMap<MeshKey, Mesh>();
	public static Mesh TRASHCAN = getMesh("objfiles/trashCan.obj", 1);
	public static Mesh FOX = getMesh("objfiles/LP_Fox_1.obj", 0.07f);
	public static Mesh SKYSCRAPER = getMesh("objfiles/skyscraper.obj", 10);
	public static Mesh CAR_PLAYER = getMesh("objfiles/LP_Car_12.obj", 0.1f, false); //LP_Car_6 0.1F
	public static Mesh CAR_AI = getMesh("objfiles/LP_Car2_2.obj", 0.1f, false);
	public static Mesh WHEEL = getMesh("objfiles/wheel.obj", 1);
	public static Mesh TEXTURED_BOX_TEST = getMesh("objfiles/cubeTextured.obj", 1);
	public static Mesh RAMP = getMesh("objfiles/ramp.obj", 5);
	public static Mesh WELL = getMesh("objfiles/well.obj", 1);
	public static Mesh INTERSECTION = getMesh("objfiles/intersection.obj", 1);
	public static Mesh SKYBOX = getMesh("objfiles/skybox_1x1x1.obj", 1, false);
	public static Mesh ARROW = getMesh("objfiles/arrow.obj", 1, false);
	public static Mesh QUAD = getMesh("objfiles/quad.obj", 1, false);
	
	public int textureID;
	public float[] vertices;
	public float[] textureCoords;
	public float[] normalCoords;
	public float[] colorCoords = new float[]{};
	public int[] indices;
	public Material material;
	
	// Fungerar sämre på Mesh som ej är symmetrisk per axel
	public Vector3f getBounds() {
		float x = Math.abs(vertices[0 + 0]);
		float y = Math.abs(vertices[0 + 1]);
		float z = Math.abs(vertices[0 + 2]);
		for (int i = 3; i < vertices.length; i+=3) {
			x = Math.max(x, Math.abs(vertices[i + 0]));
			y = Math.max(y, Math.abs(vertices[i + 1]));
			z = Math.max(z, Math.abs(vertices[i + 2]));
		}
		return new Vector3f(x, y, z);
	}
	
	public void getBounds(Vector3f size, Vector3f center) {
		float xmin = vertices[0 + 0];
		float ymin = vertices[0 + 1];
		float zmin = vertices[0 + 2];
		
		float xmax = vertices[0 + 0];
		float ymax = vertices[0 + 1];
		float zmax = vertices[0 + 2];
		
		for (int i = 3; i < vertices.length; i += 3) {
			xmin = Math.min(xmin, vertices[i + 0]);
			ymin = Math.min(ymin, vertices[i + 1]);
			zmin = Math.min(zmin, vertices[i + 2]);
			
			xmax = Math.max(xmax, vertices[i + 0]);
			ymax = Math.max(ymax, vertices[i + 1]);
			zmax = Math.max(zmax, vertices[i + 2]);
		}
		size.x = xmax - xmin;
		size.y = ymax - ymin;
		size.z = zmax - zmin;
		
		center.x = (xmax + xmin) * 0.5f;
		center.y = (ymax + ymin) * 0.5f;
		center.z = (zmax + zmin) * 0.5f;
	}

	public static class MeshKey{
		String mesh;
		float scale = 1;
		public MeshKey(String m, float s) {
			mesh = m;
			scale = s;
		}
		
		
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((mesh == null) ? 0 : mesh.hashCode());
			result = prime * result + Float.floatToIntBits(scale);
			return result;
		}



		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			MeshKey other = (MeshKey) obj;
			if (mesh == null) {
				if (other.mesh != null)
					return false;
			} else if (!mesh.equals(other.mesh))
				return false;
			if (Float.floatToIntBits(scale) != Float.floatToIntBits(other.scale))
				return false;
			return true;
		}
		
	}
	
	public static Mesh getMesh(String path) {
		return getMesh(path, 1, true);
	}
	public static Mesh getMesh(String path, float scale) {
		return getMesh(path, scale, true);
	}
	public static Mesh getMesh(String path, float scale, boolean forceFlatShading) {
		MeshKey key = new MeshKey(path, scale);
		if (meshMap.containsKey(key)) {
			return meshMap.get(key);
		} else {
			Mesh value = OBJLoader.LOADER.getIndexedMesh(path, scale, forceFlatShading);
			meshMap.put(key, value);
			return value;
		}
	}
}
