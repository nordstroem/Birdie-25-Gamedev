package GLEngine;

import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;

public class SpriteBatch {
	
	private ArrayList<Sprite> renderList = new ArrayList<Sprite>();
	private GLSLProgram activeShader;
	public static VAO quadVAO;
	
	private class Sprite{
		public int textureID;
		public Matrix4f modelMatrix;
		public Vector3f color = new Vector3f(1,1,1);
	}
	
	public class VAO{
		public int VAO; //Vertex array
		public int VBO; //Position buffer
		public int IBO; //Index buffer (ebo)
		public int UVBO; //Texture buffer
	}
	
	public void init(){
		allocateVAO();
	}
	
	public void allocateVAO(){
		VAO vao = new VAO();
		
		vao.VAO = glGenVertexArrays();
		glBindVertexArray(vao.VAO);
		
		/*float vertices[] = {
			    -0.5f,  0.5f, // Top-left
			     0.5f,  0.5f, // Top-right
			     0.5f, -0.5f, // Bottom-right
			    -0.5f, -0.5f,  // Bottom-left
			};	
		*/
		float vertices[] = {
			     0,  1, // Top-left
			     1,  1, // Top-right
			     1, 0, // Bottom-right
			     0, 0,  // Bottom-left
			};	
		int elements[] = {
			    0, 1, 2,
			    2, 3, 0
		};
		float uv[] = {
				0.0f, 0.0f,
				1.0f, 0.0f,
				1.0f, 1.0f,
				0.0f, 1.0f
		};
		
		// Position
		vao.VBO = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vao.VBO);
		glBufferData(GL_ARRAY_BUFFER, Utilities.createFloatBuffer(vertices),
				GL_STATIC_DRAW);
		glVertexAttribPointer(GLSLProgram.VERTEX_ATTRIB, 2, GL_FLOAT, false, 0,
				0);
		glEnableVertexAttribArray(GLSLProgram.VERTEX_ATTRIB);

		// Color
/*		
		vao.CBO = glGenBuffers();
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
				Utilities.createFloatBuffer(uv), GL_STATIC_DRAW);
		glVertexAttribPointer(GLSLProgram.UV_COORDS_ATTRIB_2D, 2, GL_FLOAT, false,
				0, 0);
		glEnableVertexAttribArray(GLSLProgram.UV_COORDS_ATTRIB_2D);
/*
		// Normal
		vao.NBO = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vao.NBO);
		glBufferData(GL_ARRAY_BUFFER,
				Utilities.createFloatBuffer(mesh.normalCoords), GL_STATIC_DRAW);
		glVertexAttribPointer(GLSLProgram.NORMAL_COORDS_ATTRIB, 3, GL_FLOAT,
				false, 0, 0);
		glEnableVertexAttribArray(GLSLProgram.NORMAL_COORDS_ATTRIB);
*/
		// Index buffer
		vao.IBO = glGenBuffers();
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vao.IBO);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER,
				Utilities.createIntBuffer(elements), GL_STATIC_DRAW);

		glBindVertexArray(0);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
		
		//Insert in map
		quadVAO = vao;
		
	}
	

	
	public void begin(GLSLProgram shader) {
		renderList.clear();
		activeShader = shader;
	}
	
	/*public void renderNumber(int num, Vector3f pos){
		String n = Integer.toString(num);
		Matrix4f modelMatrix = Matrix4f.translate(pos).multiply(Matrix4f.scaleX(42)).multiply(Matrix4f.scaleY(52));
		for(int i = 0; i < n.length(); i++){
			Sprite m = new Sprite();
			
			m.textureID = Texture.getNumberID(Integer.parseInt(n.substring(i, i+1)));
			m.modelMatrix = Matrix4f.translate(new Vector3f(43*i,0,0)).multiply(modelMatrix);
			renderList.add(m);
		}
	}*/
	
	public void renderString(String text, Vector3f pos){
		text = text.toUpperCase();
		int x = 0;
		for(int i = 0; i < text.length(); i++){
			Sprite m = new Sprite();
			int width = Texture.getCharWidth(text.charAt(i));
			int height = Texture.getCharHeight(text.charAt(i));
			Matrix4f modelMatrix = Matrix4f.translate(pos).multiply(Matrix4f.scaleX(width)).multiply(Matrix4f.scaleY(height));
			m.textureID = Texture.getCharID(text.charAt(i));
			m.modelMatrix = Matrix4f.translate(new Vector3f(x, 0, 0)).multiply(modelMatrix);
			x += width;
			renderList.add(m);
		}
	}
	
	public void renderString(String text, Vector3f pos, float scale, Vector3f color){
		text = text.toUpperCase();
		int x = 0;
		for(int i = 0; i < text.length(); i++){
			Sprite m = new Sprite();
			float width = Texture.getCharWidth(text.charAt(i))*scale;
			float height = Texture.getCharHeight(text.charAt(i))*scale;
			Matrix4f modelMatrix = Matrix4f.translate(pos).multiply(Matrix4f.scaleX(width)).multiply(Matrix4f.scaleY(height));
			m.textureID = Texture.getCharID(text.charAt(i));
			m.modelMatrix = Matrix4f.translate(new Vector3f(x, 0, 0)).multiply(modelMatrix);
			m.color = color;
			x += width;
			renderList.add(m);
		}
	}
	
	public void render(int textureID, Matrix4f modelMatrix) {
	
		Sprite m = new Sprite();
		m.textureID = textureID;
		m.modelMatrix = modelMatrix;
		m.color = new Vector3f(1.0f, 1.0f, 1.0f);
		renderList.add(m);
		
	}

	public void render(int textureID, Matrix4f modelMatrix, Vector3f color) {
		
		Sprite m = new Sprite();
		m.textureID = textureID;
		m.modelMatrix = modelMatrix;
		m.color = color;
		renderList.add(m);
		
	}
	
	public void end() {
		// 2D rendering
		glDisable(GL_DEPTH_TEST);
		glEnable(GL_BLEND);
		VAO vao = quadVAO;
		int m = activeShader.getUniform("M");
		int c = activeShader.getUniform("color");
		for(Sprite s : renderList){

			glUniformMatrix4fv(m, true, s.modelMatrix.toFloatBuffer());
		    float color2[] = {s.color.x, s.color.y, s.color.z, 1};
			glUniform4fv(c, Utilities.createFloatBuffer(color2));
		    glBindTexture(GL_TEXTURE_2D, s.textureID);
		    glUniform1i(s.textureID, 0);
			glBindVertexArray(vao.VAO);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
			glBindVertexArray(0);
			glBindTexture(GL_TEXTURE_2D, 0);
		}
		
		glDisable(GL_BLEND);
		glEnable(GL_DEPTH_TEST);
	
	}

	public GLSLProgram getActiveShader(){
		return activeShader;
	}
	
	public void free() {
		VAO vao = quadVAO;
		glDeleteBuffers(vao.VBO);
		glDeleteBuffers(vao.UVBO);
		glDeleteBuffers(vao.IBO);
		glDeleteVertexArrays(vao.VAO);
		
	}

	public void renderString(String string, Vector3f pos, float s1) {
		renderString(string, pos, s1, new Vector3f(1,1,1));
		
	}


}
