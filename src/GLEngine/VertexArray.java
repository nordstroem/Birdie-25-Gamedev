package GLEngine;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;

import static org.lwjgl.opengl.GL20.*;

import static org.lwjgl.opengl.GL30.*;


public class VertexArray {
	/*
	private int VAO; //Vertex array
	public int VBO; //Position buffer
	private int IBO; //Index buffer (ebo)
	private int UVBO; //Texture buffer
	private int CBO; //Color buffer
	private int NBO; //Normal buffer
	
	private int count; 	// Number of elements to draw (i.e count/3 number of triangles)
	
	
	public VertexArray(float[] vertices, int[] indices, float[] textureCoordinates, float[] colorCoordinates, float[] normalCoordinates){
		load(vertices, indices, textureCoordinates, colorCoordinates, normalCoordinates);
	}
	
	private void load(float[] vertices, int[] indices, float[] textureCoordinates, float[] colorCoordinates, float[] normalCoordinates){
	    count = indices.length;
	    
	    VAO = glGenVertexArrays();
	    glBindVertexArray(VAO);
	    
	    //Position
	    VBO = glGenBuffers();
	    glBindBuffer(GL_ARRAY_BUFFER, VBO);
	    glBufferData(GL_ARRAY_BUFFER, Utilities.createFloatBuffer(vertices),  GL_STATIC_DRAW);
	    glVertexAttribPointer(GLSLProgram.VERTEX_ATTRIB, 3, GL_FLOAT, false, 0, 0);
	    glEnableVertexAttribArray(GLSLProgram.VERTEX_ATTRIB);
	     
	    //Color
	    CBO = glGenBuffers();
	    glBindBuffer(GL_ARRAY_BUFFER, CBO);
	    glBufferData(GL_ARRAY_BUFFER, Utilities.createFloatBuffer(colorCoordinates), GL_STATIC_DRAW);
	    glVertexAttribPointer(GLSLProgram.COLOR_COORDS_ATTRIB, 4, GL_FLOAT, false, 0, 0);
	    glEnableVertexAttribArray(GLSLProgram.COLOR_COORDS_ATTRIB);
	    
	    //Texture
	    UVBO = glGenBuffers();
	    glBindBuffer(GL_ARRAY_BUFFER, UVBO);
	    glBufferData(GL_ARRAY_BUFFER, Utilities.createFloatBuffer(textureCoordinates), GL_STATIC_DRAW);
	    glVertexAttribPointer(GLSLProgram.UV_COORDS_ATTRIB, 2, GL_FLOAT, false, 0, 0);
	    glEnableVertexAttribArray(GLSLProgram.UV_COORDS_ATTRIB);

	    //Normal
	    NBO = glGenBuffers();
	    glBindBuffer(GL_ARRAY_BUFFER, NBO);
	    glBufferData(GL_ARRAY_BUFFER, Utilities.createFloatBuffer(normalCoordinates), GL_STATIC_DRAW);
	    glVertexAttribPointer(GLSLProgram.NORMAL_COORDS_ATTRIB, 3, GL_FLOAT, false, 0, 0);
	    glEnableVertexAttribArray(GLSLProgram.NORMAL_COORDS_ATTRIB);
	    
	    // Index buffer
	    IBO = glGenBuffers();
	    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, IBO);
	    glBufferData(GL_ELEMENT_ARRAY_BUFFER, Utilities.createIntBuffer(indices), GL_STATIC_DRAW);
	     
	     
	    glBindVertexArray(0);
	    glBindBuffer(GL_ARRAY_BUFFER, 0);
	    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
	}
	
	public void updateVertexBuffer(){
		
	}
	
	public void bind(){
	    glBindVertexArray(VAO);
	}
	 
	public void unbind(){
	    glBindVertexArray(0);
	}

	public void render(){
	    bind();
	    glDrawElements(GL_TRIANGLES, count, GL_UNSIGNED_INT, 0);
	    unbind();
	}
	
	
	public void free(){
		glDeleteBuffers(VBO);
		glDeleteBuffers(UVBO);
		glDeleteBuffers(CBO);
		glDeleteBuffers(NBO);
		glDeleteBuffers(IBO);
		glDeleteVertexArrays(VAO);
	}
	*/
}
