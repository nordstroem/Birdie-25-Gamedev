package GLEngine;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import game.Camera;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


import GLEngine.OBJLoader.Material;

public class MeshBatch{
	
	private Map<Mesh,VAO> vaoMap = new HashMap<Mesh,VAO>();
	private ArrayList<Model> renderList = new ArrayList<Model>();
	private GLSLProgram activeShader;
	private GLSLProgram shadowProgram;
	public FBO fbo;
	private Camera camera;
	private Window window;
	
	private class Model implements Comparable<Model>{
		public Mesh mesh;
		public int textureID;
		public Matrix4f modelMatrix;
		public Material material;
		public Vector3f color;
		
		public int compareTo(Model r) {
			if(this.textureID < r.textureID)
				return -1;
			if(this.textureID > r.textureID)
				return 1;
			return 0;
		}
	}
	
	public class VAO{
		public int VAO; //Vertex array
		public int VBO; //Position buffer
		public int IBO; //Index buffer (ebo)
		public int UVBO; //Texture buffer
		public int NBO; //Normal buffer
	}
	
	
	
	public void preAllocateVAO(Mesh mesh){
		VAO vao = new VAO();
		
		vao.VAO = glGenVertexArrays();
		glBindVertexArray(vao.VAO);
		if(mesh.vertices.length == 0 || mesh.textureCoords.length == 0 || mesh.normalCoords.length == 0 || mesh.indices.length == 0){
			System.out.println("ERROR!");
			
		}
		// Position
		vao.VBO = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vao.VBO);
		glBufferData(GL_ARRAY_BUFFER, Utilities.createFloatBuffer(mesh.vertices),
				GL_STATIC_DRAW);
		glVertexAttribPointer(GLSLProgram.VERTEX_ATTRIB, 3, GL_FLOAT, false, 0,
				0);
		glEnableVertexAttribArray(GLSLProgram.VERTEX_ATTRIB);

		// Color
		/*vao.CBO = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vao.CBO);
		glBufferData(GL_ARRAY_BUFFER,
				Utilities.createFloatBuffer(mesh.colorCoords), GL_STATIC_DRAW);
		glVertexAttribPointer(GLSLProgram.COLOR_COORDS_ATTRIB, 4, GL_FLOAT,
				false, 0, 0);
		glEnableVertexAttribArray(GLSLProgram.COLOR_COORDS_ATTRIB);
		*/

		// Texture
		vao.UVBO = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vao.UVBO);
		glBufferData(GL_ARRAY_BUFFER,
				Utilities.createFloatBuffer(mesh.textureCoords), GL_STATIC_DRAW);
		glVertexAttribPointer(GLSLProgram.UV_COORDS_ATTRIB, 2, GL_FLOAT, false,
				0, 0);
		glEnableVertexAttribArray(GLSLProgram.UV_COORDS_ATTRIB);

		// Normal
		vao.NBO = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vao.NBO);
		glBufferData(GL_ARRAY_BUFFER,
				Utilities.createFloatBuffer(mesh.normalCoords), GL_STATIC_DRAW);
		glVertexAttribPointer(GLSLProgram.NORMAL_COORDS_ATTRIB, 3, GL_FLOAT,
				false, 0, 0);
		glEnableVertexAttribArray(GLSLProgram.NORMAL_COORDS_ATTRIB);

		// Index buffer
		vao.IBO = glGenBuffers();
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vao.IBO);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER,
				Utilities.createIntBuffer(mesh.indices), GL_STATIC_DRAW);

		glBindVertexArray(0);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
		
		//Insert in map
		vaoMap.put(mesh, vao);
	}
	
	public void freeMeshVAO(Mesh mesh){
		if(!vaoMap.containsKey(mesh)){
			return;
		}
		VAO vao = vaoMap.get(mesh);
		glDeleteBuffers(vao.VBO);
		glDeleteBuffers(vao.UVBO);
		glDeleteBuffers(vao.NBO);
		glDeleteBuffers(vao.IBO);
		glDeleteVertexArrays(vao.VAO);
		shadowProgram.free();
	}
	
	public void init() {
		shadowProgram = new GLSLProgram("/shaders/shadowVert.vert", "/shaders/shadowFrag.frag");
		fbo = new FBO(4000,4000);
	}

	public void begin(GLSLProgram shader, Camera camera, Window window) {
		renderList.clear();
		activeShader = shader;
		this.camera = camera;
		this.window = window;
	}

	public void render(Mesh mesh, int textureID, javax.vecmath.Matrix4f m) {
		Matrix4f modelMatrix = new Matrix4f();
		modelMatrix.elements = new float[]{	m.m00, m.m01, m.m02, m.m03,
											m.m10, m.m11, m.m12, m.m13,
											m.m20, m.m21, m.m22, m.m23,
											m.m30, m.m31, m.m32, m.m33};
		render(mesh, textureID, modelMatrix);
	}
	
	public void render(Mesh mesh, int textureID, javax.vecmath.Matrix4f m, Vector3f color) {
		Matrix4f modelMatrix = new Matrix4f();
		modelMatrix.elements = new float[]{	m.m00, m.m01, m.m02, m.m03,
											m.m10, m.m11, m.m12, m.m13,
											m.m20, m.m21, m.m22, m.m23,
											m.m30, m.m31, m.m32, m.m33};
		render(mesh, textureID, modelMatrix, color);
	}
	
	public void render(Mesh mesh, int textureID, Matrix4f modelMatrix, Vector3f color) {
		if(!vaoMap.containsKey(mesh)){ //Maybe remove this and return error somewhere else if its not there..
			preAllocateVAO(mesh);
		}
		Model m = new Model();
		m.mesh = mesh;
		m.textureID = textureID;
		m.modelMatrix = modelMatrix;
		m.material = mesh.material;
		m.color = color;
		renderList.add(m);
	}
	
	public void render(Mesh mesh, int textureID, Matrix4f modelMatrix) {
		if(!vaoMap.containsKey(mesh)){ //Maybe remove this and return error somewhere else if its not there..
			preAllocateVAO(mesh);
		}
		Model m = new Model();
		m.mesh = mesh;
		m.textureID = textureID;
		m.modelMatrix = modelMatrix;
		m.material = mesh.material;
		m.color = new Vector3f(1.0f,1.0f,1.0f);
		renderList.add(m);
	}

	//private FloatBuffer[] floatBuffers = new FloatBuffer[2000];
	private FloatBuffer[] floatBuffersA = new FloatBuffer[2000];
	private FloatBuffer[] floatBuffersB = new FloatBuffer[2000];
	private FloatBuffer[] floatBuffersC = new FloatBuffer[2000];
	private FloatBuffer[] floatBuffersD = new FloatBuffer[2000];
	private FloatBuffer[] floatBuffersE = new FloatBuffer[2000];
	public MeshBatch() {
		allocateFloatBuffers();
	}
	
	private void allocateFloatBuffers() {
		for (int i = 0; i < floatBuffersA.length; i++) {
			floatBuffersA[i] = ByteBuffer.allocateDirect(16 * 4)
					.order(ByteOrder.nativeOrder()).asFloatBuffer();
		}
		for (int i = 0; i < floatBuffersB.length; i++) {
			floatBuffersB[i] = ByteBuffer.allocateDirect(16 * 4)
					.order(ByteOrder.nativeOrder()).asFloatBuffer();
		}
		for (int i = 0; i < floatBuffersC.length; i++) {
			floatBuffersC[i] = ByteBuffer.allocateDirect(3 * 4)
					.order(ByteOrder.nativeOrder()).asFloatBuffer();
		}
		for (int i = 0; i < floatBuffersD.length; i++) {
			floatBuffersD[i] = ByteBuffer.allocateDirect(16 * 4)
					.order(ByteOrder.nativeOrder()).asFloatBuffer();
		}
		for (int i = 0; i < floatBuffersE.length; i++) {
			floatBuffersE[i] = ByteBuffer.allocateDirect(4 * 4)
					.order(ByteOrder.nativeOrder()).asFloatBuffer();
		}
	}


	private void renderMeshBatch(){
		while (renderList.size() >= floatBuffersA.length){ // Dubbla storleken tills allt får plats
			floatBuffersA = new FloatBuffer[floatBuffersA.length * 2];
			floatBuffersB = new FloatBuffer[floatBuffersB.length * 2];
			floatBuffersC = new FloatBuffer[floatBuffersC.length * 2];
			floatBuffersD = new FloatBuffer[floatBuffersD.length * 2];
			floatBuffersE = new FloatBuffer[floatBuffersD.length * 2];
			allocateFloatBuffers();
		}
		int i = 0;
		
		//Render shadowpass
		

		glBindFramebuffer(GL_FRAMEBUFFER, fbo.fbo);
		glViewport(0,0,fbo.width,fbo.height); // Render on the whole framebuffer, complete from the lower left corner to the upper right
	

		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		shadowProgram.enable();
		Vector3f lightInvDir = new Vector3f(-0.2f,1f,0.2f);

		//lightInvDir = lightInvDir.normalized();
		Vector3f tp = camera.target.subtract(camera.pos);
		float dot = tp.x / (float)Math.sqrt(tp.x*tp.x + tp.y*tp.y);
		dot = 1 - Math.abs(dot);
		Matrix4f depthProjectionMatrix = Matrix4f.orthoProjection(100, -100, 100, -100, -100, 100);
		Vector3f c = camera.target;
		Matrix4f depthViewMatrix = Matrix4f.lookAt(c.add(lightInvDir), c,  new Vector3f(1,0,0));

		i = 0;
		for(Model model : renderList){
			i++;
			Matrix4f depthModelMatrix = model.modelMatrix;
			Matrix4f depthMVP = depthProjectionMatrix.multiply(depthViewMatrix).multiply(depthModelMatrix);
			int m = shadowProgram.getUniform("depthMVP");
			floatBuffersA[i].put(depthMVP.elements).flip();
			glUniformMatrix4fv(m, true, floatBuffersA[i]);
			VAO vao = vaoMap.get(model.mesh);
			glBindVertexArray(vao.VAO);
			glDrawElements(GL_TRIANGLES, model.mesh.indices.length, GL_UNSIGNED_INT, 0);
			

		}
		glBindVertexArray(0);
		shadowProgram.disable();
		
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
		glViewport(0,0, (int)window.getWidth(), (int)window.getHeight()); // Render on the whole framebuffer, complete from the lower left corner to the upper right
		//glEnable(GL_CULL_FACE);
		//glCullFace(GL_BACK); // Cull back-facing triangles -> draw only front-facing triangles

		// Clear the screen
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        activeShader.enable();
        
        int pv = activeShader.getUniform("PV");
        glUniformMatrix4fv(pv, true, getProjectionViewMatrix().toFloatBuffer()); //Mer optimerat att transponera redan från början? (eller ändra till column-major lagring)
       
        int cam = activeShader.getUniform("camPos");
        glUniform3f(cam, camera.pos.x, camera.pos.y, camera.pos.z);

        int tid = activeShader.getUniform("texSampler");
        int sid = activeShader.getUniform("shadowMap");
        int spid = activeShader.getUniform("specularColor");
        int m = activeShader.getUniform("M");
        int did = activeShader.getUniform("DepthBiasMVP"); 
        int cid = activeShader.getUniform("color");
		//fbo.bindForReading(GL_TEXTURE1);

        i = 0;
        int currentTexture = -1;
		glActiveTexture(GL_TEXTURE1);
		glBindTexture(GL_TEXTURE_2D, fbo.shadowmap);
		glUniform1i(sid, 1);
        glActiveTexture(GL_TEXTURE0);
        for(Model model : renderList){
			
			i++;
			
			if(currentTexture != model.textureID){
				glBindTexture(GL_TEXTURE_2D, model.textureID);
				glUniform1i(tid, 0);
				currentTexture = model.textureID;
			}

			
			floatBuffersB[i].put(model.modelMatrix.elements).flip();
			glUniformMatrix4fv(m, true, floatBuffersB[i]);
	        float[] fb = {model.material.specular.x, model.material.specular.y, model.material.specular.z};
	        floatBuffersC[i].put(fb).flip();
	        glUniform3fv(spid, floatBuffersC[i]);
	        
	        Matrix4f depthModelMatrix = model.modelMatrix;
	        Matrix4f depthMVP = Matrix4f.bias().multiply(depthProjectionMatrix).multiply(depthViewMatrix).multiply(depthModelMatrix);
	        floatBuffersD[i].put(depthMVP.elements).flip();
	        glUniformMatrix4fv(did, true,  floatBuffersD[i]);
	        
	        float[] col = {model.color.x, model.color.y, model.color.z, 1.0f};
	        floatBuffersE[i].put(col).flip();
	        glUniform4fv(cid, floatBuffersE[i]);
	        
			VAO vao = vaoMap.get(model.mesh);
			glBindVertexArray(vao.VAO);
			glDrawElements(GL_TRIANGLES, model.mesh.indices.length, GL_UNSIGNED_INT, 0);

		}
		glBindVertexArray(0);
		glBindTexture(GL_TEXTURE_2D,0);
		activeShader.disable();
		
			
	}
	

	public void end() {
		Collections.sort(renderList);
		renderMeshBatch();
	}
	
	public Matrix4f getProjectionViewMatrix(){
        Matrix4f proj = Matrix4f.perspective(90, window.getAspect(), 1.0f, 10000f);
        //Matrix4f view = Matrix4f.rotateX(-camera.rot.x).multiply(Matrix4f.rotateY(-camera.rot.y)).multiply(Matrix4f.translate(camera.pos.multiply(-1)));
        return proj.multiply(camera.getViewMatrix());
	}
	
    private void initPerspective(){
    	
        activeShader.enable();
        
        // Make sure the shader uses texture 0 (Se upp om man behöver flera samplers i samma vertexArray, kolla länken jag har sparad)
      //  int textureUniform = activeShader.getUniform("texSampler");
       // glUniform1i(textureUniform, 0);
        //textureUniform = activeShader.getUniform("shadowMap");
        //glUniform1i(textureUniform, 1);
        // Create the PV matrix
        int pv = activeShader.getUniform("PV");
        glUniformMatrix4fv(pv, true, getProjectionViewMatrix().toFloatBuffer()); //Mer optimerat att transponera redan från början? (eller ändra till column-major lagring)
       
        int cam = activeShader.getUniform("camPos");
        glUniform3f(cam, camera.pos.x, camera.pos.y, camera.pos.z);
        

   }
    
	public void free() {
		Iterator it = vaoMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();	
			Mesh mesh = (Mesh)pair.getKey();
			freeMeshVAO(mesh);
			it.remove(); // avoids a ConcurrentModificationException
		}
		
		fbo.free();
	}

}
