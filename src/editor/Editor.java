package editor;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_UP;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import game.Camera;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import GLEngine.GLSLProgram;
import GLEngine.InputHandler;
import GLEngine.Matrix4f;
import GLEngine.Mesh;
import GLEngine.MeshBatch;
import GLEngine.Texture;
import GLEngine.Vector3f;
import GLEngine.Window;

public class Editor {
	private GLSLProgram activeProgram;
	private MeshBatch meshBatch;
	private Window window;
	private Camera camera = new Camera(null);
	private float fov = 70;
	private State state = State.ENTITIES;
	private enum State{
		ENTITIES, TILES, MAP;
	}
	private FileEntity selectedEntity = new FileEntity();
	private FileTile selectedTile = new FileTile();
	private FileHandler fileHandler;
	public Editor() {
		init();
		loop();
		free();
	}
	
	public void init(){
		window = new Window("Editor", 800, 800);
		window.init(900,30, 1, false);
		
		activeProgram = new GLSLProgram("/shaders/vertex.vert", "/shaders/fragment.frag");
		initMeshBatch();
		camera.pos = new Vector3f(0.0f, 50f, 0.0f);
		camera.rot.x = -90;
		fileHandler = new FileHandler();
		fileHandler.load();
		createFrame();
	}
	
	private void createFrame() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		JFrame frame = new JFrame();
		frame.setSize(1000, 1000);
		
		JTabbedPane tab = new JTabbedPane();
		tab.addTab("Entities", new EntitiesPanel(this, fileHandler));
		tab.addTab("Tiles", new TilesPanel(this, fileHandler));
		tab.addTab("Map", new MapPanel(this, fileHandler));
		tab.addChangeListener(new ChangeListener() {
		        public void stateChanged(ChangeEvent e) {
		        	if (tab.getSelectedIndex() == 0) {
		        		state = State.ENTITIES;
		        	} else if (tab.getSelectedIndex() == 1) {
		        		state = State.TILES;
		        	} else if (tab.getSelectedIndex() == 2) {
		        		state = State.MAP;
		        	}
		        }
		});
		frame.add(tab);
		frame.setJMenuBar(createJFrameMenu());
		frame.setVisible(true);
	}

	private JMenuBar createJFrameMenu() {
		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu("File");
		JMenuItem save = new JMenuItem("Save");
		save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		save.getAccessibleContext().setAccessibleDescription("Save");
		save.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				System.out.print("Saving...");
				fileHandler.save();
				System.out.println("done");
			}
		});
		/*JMenuItem load = new JMenuItem("Load"); // TODO behöver läsa in från file handler också om load ska funka
		load.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
		load.getAccessibleContext().setAccessibleDescription("Load");
		load.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				System.out.print("Loading...");
				fileHandler.load();
				System.out.println("done");
			}
		});
		menu.add(load);*/
		menu.add(save);
		menuBar.add(menu);
		return menuBar;
	}

	public void initMeshBatch(){
		meshBatch = new MeshBatch();
		meshBatch.init();
	}
	
	public void loop(){
		long tick = 0;
        while(!window.shouldClose()){
            long time0 = System.nanoTime();
            tick++;
            
            update(tick);
            render(tick);
            
            long dt = System.nanoTime() - time0;
            if(tick % 60 == 0 && dt!= 0){
            	//System.out.println("FPS: " + 1.0/((float)dt/1E9));
            }
            
        }
	}
	
	private void update(long tick) {
		if (state == State.ENTITIES) {
			
		} else if (state == State.TILES) {
			
		} else if (state == State.MAP) {
			
		}
		
		updateInput();
		
	}

	private void updateInput() {
    	InputHandler.update();
    	window.pollEvents();
		
    	if(InputHandler.down(GLFW_KEY_ESCAPE)){
        	window.setShouldClose(true);
        }
    	
    	float speed = 0.2f;
    	if (InputHandler.down(GLFW_KEY_DOWN)) {
			camera.pos.x += speed * Math.sin(Math.toRadians(camera.rot.y));
			camera.pos.z += speed * Math.sin(Math.toRadians(camera.rot.y + 90));
		}
		if (InputHandler.down(GLFW_KEY_UP)) {
			camera.pos.x -= speed * Math.sin(Math.toRadians(camera.rot.y));
			camera.pos.z -= speed * Math.sin(Math.toRadians(camera.rot.y + 90));
		}
		if (InputHandler.down(GLFW_KEY_RIGHT)) {
			camera.pos.z -= speed * Math.sin(Math.toRadians(camera.rot.y));
			camera.pos.x += speed * Math.sin(Math.toRadians(camera.rot.y + 90));
		}
		if (InputHandler.down(GLFW_KEY_LEFT)) {
			camera.pos.z += speed * Math.sin(Math.toRadians(camera.rot.y));
			camera.pos.x -= speed * Math.sin(Math.toRadians(camera.rot.y + 90));
		}
		
        if(InputHandler.down(GLFW_KEY_W)){
        	camera.rot.x += 1;
        }
        if(InputHandler.down(GLFW_KEY_S)){
        	camera.rot.x -= 1;
        }
        if(InputHandler.down(GLFW_KEY_A)){
        	camera.rot.y += 1;
        }
        if(InputHandler.down(GLFW_KEY_D)){
        	camera.rot.y -= 1;
        }
        if(InputHandler.down(GLFW_KEY_SPACE)){
        	camera.pos.y+=speed;
        }
        if(InputHandler.down(GLFW_KEY_LEFT_CONTROL)){
        	camera.pos.y-=speed;
        }
	}

	public void render(long tick){
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        activeProgram.enable();
        
        // Make sure the shader uses texture 0 (Se upp om man behöver flera samplers i samma vertexArray, kolla länken jag har sparad)
        glActiveTexture(GL_TEXTURE0);
        int textureUniform = activeProgram.getUniform("texSampler");
        glUniform1i(textureUniform, 0);
        // Create the PV matrix
        int pv = activeProgram.getUniform("PV");
        glUniformMatrix4fv(pv, true, getProjectionViewMatrix().toFloatBuffer()); //Mer optimerat att transponera redan från början? (eller ändra till column-major lagring)
        
        //TODO FIX THIS SHIT
        
        meshBatch.begin(activeProgram, camera, window);
        //meshBatch.render(testMesh, 0, Matrix4f.identity());
        if (state == State.ENTITIES) {
        	renderEntities();
		} else if (state == State.TILES) {
			renderTiles();
		} else if (state == State.MAP) {
			renderMap();
		}
        meshBatch.end();
        
        activeProgram.disable();
        window.swapBuffers();
    }
	
	private void renderMap() {
		FileMap map = fileHandler.getMap();
		synchronized (map.fileMapTiles) {
			for(FileMapTile fmt : map.fileMapTiles){
				for (FileTileEntity fte : fileHandler.getFileTile(fmt.tileId).entities) {
					FileEntity fe = fileHandler.getEntity(fte.entityId);
					String objFile = "objfiles/" + fe.obj;
					String texFile = "textures/" + fe.tex;
					if (new File("src/" + objFile).exists() && new File("src/" + texFile).exists()) {
						Vector3f pos = new Vector3f();
						float centerx = (float) (fileHandler.getFileTile(fmt.tileId).sizeX / 2.0);
						float centerz = (float) (fileHandler.getFileTile(fmt.tileId).sizeZ / 2.0);
						float transx = (float) (fte.px - centerx);
						float transz = (float) (fte.pz - centerz);
						float a = fmt.rot * 90;
						if (fmt.rot == 1) {
							float tmp = transz;
							transz = transx;
							transx = -tmp;
						} else if (fmt.rot == 2) {
							transx = -transx;
							transz = -transz;
						} else if (fmt.rot == 3) {
							float tmp = transz;
							transz = -transx;
							transx = tmp;
						}
						pos.x = (float)(transx + centerx + map.tileSize * fmt.ix);
						pos.z = (float)(transz + centerz + map.tileSize * fmt.iz);
						pos.y = (float)(fte.py + fmt.y); 
						meshBatch.render(Mesh.getMesh(objFile), new Texture(texFile).textureID, Matrix4f.translate(pos).multiply(Matrix4f.rotateY(a)));
					} else {
						System.err.println("Could not find files");
					}
				}
			}
		}
	}

	private void renderTiles() {
		try{
			for (FileTileEntity fte : selectedTile.entities) {
				FileEntity fe = fileHandler.getEntity(fte.entityId);
				String objFile = "objfiles/" + fe.obj;
				String texFile = "textures/" + fe.tex;
				if (new File("src/" + objFile).exists() && new File("src/" + texFile).exists()) {
					meshBatch.render(Mesh.getMesh(objFile), new Texture(texFile).textureID, 
							Matrix4f.translate(new Vector3f((float)fte.px, (float)fte.py, (float)fte.pz)));
				}
			}
    	}catch(Exception e){
    		e.printStackTrace();
    		try {
				Thread.sleep(100); // Om något är fel, chilla lite innan man försöker igen
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
    	}
		
	}

	private void renderEntities() {
		try{
			String objFile = "objfiles/" + selectedEntity.obj;
			String texFile = "textures/" + selectedEntity.tex;
			if (new File("src/" + objFile).exists() && new File("src/" + texFile).exists()) {
				meshBatch.render(Mesh.getMesh(objFile), new Texture(texFile).textureID, Matrix4f.identity());
			}
    	}catch(Exception e){
    		try {
				Thread.sleep(100); // Om något är fel, chilla lite innan man försöker igen
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
    	}
	}

	public void free(){
		activeProgram.free();
    	meshBatch.free();
    	window.destroy();
	}
	
	public Matrix4f getProjectionViewMatrix(){
        Matrix4f proj = Matrix4f.perspective(fov, window.getAspect(), 1.0f, 1000f);
        return proj.multiply(camera.getViewMatrix());
	}
	
	public static void main(String[] args) {
		new Editor();
	}

	public void setSelectedEntity(FileEntity entity) {
		this.selectedEntity = entity;
	}
	
	public void setSelectedTile(FileTile tile){
		this.selectedTile = tile;
	}

	public void setCameraPositionXZ(int x, int z) {
		camera.pos.x = x;
		camera.pos.z = z;
		//camera.pos.y = 40;
		camera.rot.x = -90;
		camera.rot.y = 0;
		camera.rot.z = 0;
		
	}

	public void zoomIn() {
		camera.pos.y -= 20;
	}

	public void zoomOut() {
		camera.pos.y += 20;
	}
}
