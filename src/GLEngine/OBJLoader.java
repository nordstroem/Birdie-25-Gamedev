package GLEngine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;


/**
 * OBJLoader
 * Parses and loads an OBJ file with getOBJMesh or 
 * getGLMesh for reindexed float arrays for opengl buffers
 * No support for groups or materials yet
 * @author Johan
 * */
public class OBJLoader {
	
	
	public static OBJLoader LOADER = new OBJLoader();
	
	public class OBJMesh {
		public ArrayList<Vector3f> vertexList = new ArrayList<Vector3f>();
		public ArrayList<Vector3f> normalList = new ArrayList<Vector3f>();
		public ArrayList<Vector3f> texcoordList = new ArrayList<Vector3f>();
		public ArrayList<OBJFace> faceList = new ArrayList<OBJFace>();
		public Material material = new Material();
	}
	
	public class Material{
		Vector3f specular = new Vector3f(0,0,0);
	}
	
	//Indices for one face corresponding to the OBJMesh, with vertices A,B and C
	private class OBJFace {
		public int vA = -1, vB = -1, vC = -1; // Vertex indices
		public int nA = -1, nB = -1, nC = -1; // Normal indices
		public int tA = -1, tB = -1, tC = -1; // Texture indices
	}
	
	
	/*
	 * Helper class for reindex to GLMesh
	 * */
	private class FaceVertex{
		int vertexID, normalID, texID;
		
		public FaceVertex(int v, int n, int t){
			vertexID = v;
			normalID = n;
			texID = t;
		}
		
		public int existsIn(ArrayList<FaceVertex> fv){
			for(int i = 0; i < fv.size(); i++){
				FaceVertex ftemp = fv.get(i);
				if(this.vertexID == ftemp.vertexID && this.normalID == ftemp.normalID && this.texID == ftemp.texID){
					return i;
				}
			}
			return -1;
		}
	
	}
	
	public Mesh getIndexedMesh(String path, float scale){
		return getIndexedMesh(path, scale, false);
	}
	
	//Could possibly optimize this method alot.
	public Mesh getIndexedMesh(String path, float scale, boolean forceFlatShading){
		OBJMesh mesh = getOBJMesh(path, scale);
	
		ArrayList<OBJFace> faces = mesh.faceList;
		ArrayList<FaceVertex> fVertices = new ArrayList<FaceVertex>();
		ArrayList<Integer> findices = new ArrayList<Integer>();
		
		ArrayList<Vector3f> flatShadingNormals = new ArrayList<Vector3f>();
		
		int maxindex = 0;
		for(OBJFace f: faces){
			FaceVertex fa = new FaceVertex(f.vA, f.nA, f.tA);
			FaceVertex fb = new FaceVertex(f.vB, f.nB, f.tB);
			FaceVertex fc = new FaceVertex(f.vC, f.nC, f.tC);
			
			if (forceFlatShading) {
				Vector3f v1 = mesh.vertexList.get(f.vA - 1);
				Vector3f v2 = mesh.vertexList.get(f.vB - 1);
				Vector3f v3 = mesh.vertexList.get(f.vC - 1);
				fa.normalID = flatShadingNormals.size();
				fb.normalID = flatShadingNormals.size();
				fc.normalID = flatShadingNormals.size();
				flatShadingNormals.add(v1.subtract(v2).cross(v1.subtract(v3)).normalized());
			}
			
			int index = fa.existsIn(fVertices);
			if(index == -1){
				findices.add(maxindex);
				maxindex++;
				fVertices.add(fa);
			}else{
				findices.add(index);
			}
			
			index = fb.existsIn(fVertices);
			if(index == -1){
				findices.add(maxindex);
				maxindex++;
				fVertices.add(fb);
			}else{
				findices.add(index);
			}
			
			index = fc.existsIn(fVertices);
			if(index == -1){
				findices.add(maxindex);
				maxindex++;
				fVertices.add(fc);
			}else{
				findices.add(index);
			}
		}
		
		
		
		float[] vertices = new float[maxindex*3];
		float[] textureCoordinates = new float[maxindex*2];
		float[] normalCoordinates = new float[maxindex*3];
		int[] indices = new int[findices.size()];
		
		for(int i = 0; i < indices.length; i++){
			indices[i] = findices.get(i).intValue();
		}
		

		for(int i = 0; i < maxindex; i++){
			int vID = fVertices.get(i).vertexID - 1;
			int nID = fVertices.get(i).normalID - 1;
			int tID = fVertices.get(i).texID - 1;
			Vector3f vertex = mesh.vertexList.get(vID);
			Vector3f normal;
			if (forceFlatShading) {
				normal = flatShadingNormals.get(fVertices.get(i).normalID);
			} else {
				normal = mesh.normalList.get(nID);
			}
			Vector3f tex = null;
			if (mesh.texcoordList.size() == 0 || tID < 0) { // TODO varför behövs tID < 0 på Well?
				tex = new Vector3f(0,0,0);
			} else {
				tex = mesh.texcoordList.get(tID);
			}
			vertices[i*3 + 0] = vertex.x; 
			vertices[i*3 + 1] = vertex.y; 
			vertices[i*3 + 2] = vertex.z; 
			normalCoordinates[i*3 + 0] = normal.x; 
			normalCoordinates[i*3 + 1] = normal.y; 
			normalCoordinates[i*3 + 2] = normal.z;
			
			textureCoordinates[i*2 + 0] = tex.x; 
			textureCoordinates[i*2 + 1] = tex.y; 

		}
		//"Free" mesh
	
		
		Mesh glMesh = new Mesh();
		glMesh.vertices = vertices;
		glMesh.normalCoords = normalCoordinates;
		glMesh.textureCoords = textureCoordinates;
		glMesh.indices = indices;
		glMesh.material = mesh.material;
		mesh = null;
		return glMesh;
	}
	
	
	
	static String convertStreamToString(java.io.InputStream is) {
	    java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
	    return s.hasNext() ? s.next() : "";
	}


	public OBJFace getFace(String line) {
		OBJFace face = new OBJFace();
		
		//Remove f and spaces
		while(line.charAt(0) == 'f' || line.charAt(0) == ' '){
			line = line.substring(1,line.length());
		}
		line = line.replaceAll("//", "/");
		line = line.replaceAll("  ", " ");
		String[] vectors = line.split(" ");
		
		
		String a = vectors[0];
		String b = vectors[1];
		String c = vectors[2];
		
		
		//A index
		String[] values = a.split("/");
		if(values.length == 1){
			face.vA = Integer.parseInt(values[0]);
		}
		if(values.length == 2){
			face.vA = Integer.parseInt(values[0]);
			face.nA = Integer.parseInt(values[1]);
		}
		if(values.length == 3){
			face.vA = Integer.parseInt(values[0]);
			face.tA = Integer.parseInt(values[1]);
			face.nA = Integer.parseInt(values[2]);
		}
		
		//B index
		values = b.split("/");
		if(values.length == 1){
			face.vB = Integer.parseInt(values[0]);
		}
		if(values.length == 2){
			face.vB = Integer.parseInt(values[0]);
			face.nB = Integer.parseInt(values[1]);
		}
		if(values.length == 3){
			face.vB = Integer.parseInt(values[0]);
			face.tB = Integer.parseInt(values[1]);
			face.nB = Integer.parseInt(values[2]);
		}
		
		//C index
		values = c.split("/");
		if(values.length == 1){
			face.vC = Integer.parseInt(values[0]);
		}
		if(values.length == 2){
			face.vC = Integer.parseInt(values[0]);
			face.nC = Integer.parseInt(values[1]);
		}
		if(values.length == 3){
			face.vC = Integer.parseInt(values[0]);
			face.tC = Integer.parseInt(values[1]);
			face.nC = Integer.parseInt(values[2]);
		}
		
		return face;
	}
	

	public OBJMesh getOBJMesh(String filePath, float scale){
		InputStream in = Utilities.class.getResourceAsStream("/" + filePath); 
		String objString = convertStreamToString(in);

		
		OBJMesh obj;
		obj = new OBJMesh();
		Scanner scan = new Scanner(objString);

		while (scan.hasNext()) {

			String line = scan.nextLine();

			if (line.length() >= 2 && line.charAt(0) == 'v' && line.charAt(1) == ' ') {

				String[] s = line.replace("  ", " ").split(" ");
				double x = Double.parseDouble(s[1]) * scale;
				double y = Double.parseDouble(s[2]) * scale;
				double z = Double.parseDouble(s[3]) * scale;
				obj.vertexList.add(new Vector3f((float) x, (float) y, (float) z)); 

			}
			if (line.length() >= 2 && line.charAt(0) == 'v' && line.charAt(1) == 'n') {

				String[] s = line.replace("  ", " ").split(" ");
				double x = Double.parseDouble(s[1]);
				double y = Double.parseDouble(s[2]);
				double z = Double.parseDouble(s[3]);
				obj.normalList.add(new Vector3f((float) x, (float) y, (float) z)); 

			}
			
			if (line.length() >= 2 && line.charAt(0) == 'v' && line.charAt(1) == 't') {

				String[] s = line.replace("  ", " ").split(" ");
				double x = 0, y = 0, z = 0;
				if(s.length > 1){
					x = Double.parseDouble(s[1]);
				}
				
				if(s.length > 2){
					y = Double.parseDouble(s[2]);
				}
				
				if(s.length > 3){
					z = Double.parseDouble(s[3]);
				}
				obj.texcoordList.add(new Vector3f((float) x, (float) y, (float) z)); 

			}
			
			if (line.length() >= 2 && line.charAt(0) == 'f' && line.charAt(1) == ' ') {

				OBJFace face = getFace(line);
				obj.faceList.add(face);

			}

		}
		filePath = filePath.replaceAll(".obj", ".mtl");
		
		in = Utilities.class.getResourceAsStream("/" + filePath); 
		objString = convertStreamToString(in);
		scan.close();
		
		scan = new Scanner(objString);
		while (scan.hasNext()) {
			String line = scan.nextLine();
			String[] s = line.split(" ");
			
			if (line.length() >= 2 && line.charAt(0) == 'K' && line.charAt(1) == 's') {
				obj.material.specular = new Vector3f((float)Double.parseDouble(s[1]), (float)Double.parseDouble(s[2]), (float)Double.parseDouble(s[3]));
			}
		}
		scan.close();
		
		return obj;
	}
	
	

}
