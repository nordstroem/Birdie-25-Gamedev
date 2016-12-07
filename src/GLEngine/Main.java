package GLEngine;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_UP;
import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.*;

import java.util.ArrayList;


public class Main{
     
    public boolean running = true;
    
    private Window window;

    private VertexArray VATest;
    private GLSLProgram PRTest;
    private Texture TxTest;
    private ArrayList<VertexArray> VAOS = new ArrayList<VertexArray>();
    
    
    public static void main(String args[]){
       new Main();
    }
     
    public Main(){
		float vertices[] = {
				
				//index
				//1 3
				//2 4
				
				//back
				-0.5f, -0.5f, -0.5f,
				-0.5f, 0.5f, -0.5f,
				0.5f, 0.5f, -0.5f,
				0.5f, -0.5f, -0.5f,
				
				//left
				-0.5f, 0.5f, -0.5f,
				-0.5f, -0.5f, -0.5f,
				-0.5f, -0.5f, 0.5f,
				-0.5f, 0.5f, 0.5f,
				
				//front
				-0.5f, -0.5f, 0.5f,
				-0.5f, 0.5f, 0.5f,
				0.5f, 0.5f, 0.5f,
				0.5f, -0.5f, 0.5f,
				
				
				//bottom
				-0.5f, -0.5f, 0.5f,
				0.5f, -0.5f, 0.5f,
				0.5f, -0.5f, -0.5f,
				-0.5f, -0.5f, -0.5f,
				
				
				//right
				0.5f, 0.5f, -0.5f,
				0.5f, -0.5f, -0.5f,
				0.5f, -0.5f, 0.5f,
				0.5f, 0.5f, 0.5f,
				
				
				//top
				-0.5f, 0.5f, 0.5f,
				0.5f, 0.5f, 0.5f,
				0.5f, 0.5f, -0.5f,
				-0.5f, 0.5f, -0.5f
				
				
		};
		

		float tex[] = { 
				0, 1, 
				0, 0, 
				1, 0,
				1, 1, 
				
				0, 0, 
				0, 1, 
				1, 1,
				1, 0,
				
				0, 1, 
				0, 0, 
				1, 0,
				1, 1,
				
				0, 0, 
				0, 1, 
				1, 1,
				1, 0,
				
				0, 0, 
				0, 1, 
				1, 1,
				1, 0,
				
				0, 1, 
				0, 0, 
				1, 0,
				1, 1
				
				};
		
		float color[] = {1, 0, 0, 1,
						1, 0, 0, 1,
						1, 0, 0, 1,
						1, 0, 0, 1,
						
						0, 1, 0, 1,
						0, 1, 0, 1,
						0, 1, 0, 1,
						0, 1, 0, 1,
						
						0, 0, 1, 1,
						0, 0, 1, 1,
						0, 0, 1, 1,
						0, 0, 1, 1,
						
						1, 0, 0, 1,
						0, 1, 0, 1,
						0, 0, 1, 1,
						0, 1, 0, 1,

						1, 0, 0, 1,
						0, 1, 0, 1,
						0, 0, 1, 1,
						0, 1, 0, 1,
						
						1, 0, 0, 1,
						0, 1, 0, 1,
						0, 0, 1, 1,
						0, 1, 0, 1
						
						};
		
		int indices[] = { 
				0, 1, 2, 2, 3, 0,
				4, 5, 6, 6, 7, 4, 
				8, 9, 10, 10, 11, 8,
				12, 13, 14, 14, 15, 12,
				16, 17, 18, 18, 19, 16,
				20, 21, 22, 22, 23, 20,
				};

    	
   
		initWindow(); //Should be called before anything else, because it creates GLContext
		PRTest = new GLSLProgram("shaders/vertex.vert", "shaders/fragment.frag");
		float[] color2 = {};
		//VATest = new VertexArray(vertices, indices, tex, color2, color2);
		TxTest = new Texture("textures/text.png");
	

		
		
		
		
		initMesh();
		loop();
        free();
    }
    
    Mesh mesh = new Mesh();
    MeshBatch meshBatch = new MeshBatch();
    
    private void initMesh(){
		
		OBJLoader oe = new OBJLoader();
		mesh = oe.getIndexedMesh("objfiles/skyscraper.obj", 1f);
		//mesh = oe.getIndexedMesh("objfiles/skyscraper.obj", 0.1f);
		float[] colorCoordinates = {};
    	
    	meshBatch.init();
    	meshBatch.preAllocateVAO(mesh);
    }
    
    private void initWindow(){
    	window = new Window("GLEngine");
    	//window.init();
    }
     

    
    public void update(){
        window.pollEvents();
        
        if(InputHandler.keys[GLFW_KEY_ESCAPE]){
        	window.setShouldClose(true);
        }
        if(InputHandler.keys[GLFW_KEY_LEFT]){
        	camera.x-=0.1;
        }
        if(InputHandler.keys[GLFW_KEY_RIGHT]){
        	camera.x+=0.1;
        }
        if(InputHandler.keys[GLFW_KEY_DOWN]){
        	camera.z+=0.1;
        }
        if(InputHandler.keys[GLFW_KEY_UP]){
        	camera.z-=0.1;
        }
        if(InputHandler.keys[GLFW_KEY_A]){
        	rot+=1;
        }
        if(InputHandler.keys[GLFW_KEY_D]){
        	rot-=1;
        }
        if(InputHandler.keys[GLFW_KEY_SPACE]){
        	camera.y+=0.1;
        }
        if(InputHandler.keys[GLFW_KEY_LEFT_CONTROL]){
        	camera.y-=0.1;
        }
        
    }
    
    Vector3f camera = new Vector3f(0.0f, 0.1f, 30.0f);
    float rot = 0;
    /* RENDER ORDER:
     * Clear buffer bits
     * Bind program
     * Bind texture
     * Bind VA
     * Unbind VA
     * Unbind texture
     * Unbind program
     * Swap the buffers
     * */
    public void render(){
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        
        PRTest.enable();
        
        
        // Make sure the shader uses texture 0 (Se upp om man behöver flera samplers i samma vertexArray, kolla länken jag har sparad)
        glActiveTexture(GL_TEXTURE0);
        int textureUniform = PRTest.getUniform("texSampler");
        glUniform1i(textureUniform, 0);
        
        float[] vec = {1.f,1.f, 1f, 1f};
        int vecUniform = PRTest.getUniform("uniColor");
        glUniform4fv(vecUniform, Utilities.createFloatBuffer(vec));
        
        Matrix4f proj = Matrix4f.perspective(70, window.getAspect(), 1.0f, 1000f);
        Matrix4f view = Matrix4f.lookAt(new Vector3f(1.2f,1.2f,1.2f), new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.0f,1.0f,0.0f));
        Matrix4f model = Matrix4f.rotateY((float)glfwGetTime()*50f);
        model = Matrix4f.identity();
        view = Matrix4f.rotateY(-rot).multiply(Matrix4f.translate(camera.multiply(-1)));
        Matrix4f total = proj.multiply(view);
        int ortho = PRTest.getUniform("PV");
        glUniformMatrix4fv(ortho, true, total.toFloatBuffer()); //Mer optimerat att transponera redan från början? (eller ändra till column-major lagring)

    //	TxTest.bind();
    	
    	//meshBatch.begin(PRTest);
    	meshBatch.render(mesh, TxTest.textureID, Matrix4f.translate(new Vector3f(-10.0f,0,0)));
    	//meshBatch.render(mesh, TxTest.textureID, Matrix4f.translate(new Vector3f(10.0f,0,0)));
    	//meshBatch.render(mesh, TxTest.textureID, Matrix4f.translate(new Vector3f(0f,0,0)));
    	meshBatch.end();
    	PRTest.disable();
    	
    	window.swapBuffers();
    
    }
    
    public void free(){
    	TxTest.free();
    	PRTest.free();
    	meshBatch.free();
    	window.destroy();
    }
     
    public void loop() {
    	long counter = 0;
        while(running){
        	counter++;
            long time0 = System.nanoTime();
            
        	update();
            render();
            if(window.shouldClose()){
                running = false;
            }
            long dt = System.nanoTime() - time0;
            if(counter % 60 == 0 && dt!= 0){
            	System.out.println("FPS: " + 1.0/((float)dt/1E9));
            }
            
        }
        
    }
     
     
 
}