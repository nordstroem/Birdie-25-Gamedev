package GLEngine;

import org.lwjgl.glfw.GLFWKeyCallback;
import static org.lwjgl.glfw.GLFW.*;

public class InputHandler extends GLFWKeyCallback{
 
	private static int index = 0;
	private static int prevIndex = 1;
    public static boolean[] keys = new boolean[65535];
    public static boolean[] previousKeys = new boolean[65535];
     
    @Override
    public void invoke(long window, int key, int scancode, int action, int mods) {
    	if (key >= 0 && key < keys.length) {
    		keys[key] = action != GLFW_RELEASE;
    	}
    }
    public static void update(){
		previousKeys = keys.clone();
    }
    public static boolean up(int key){
    	return !keys[key];
    }
    public static boolean down(int key){
    	return keys[key];
    }
    public static boolean pressed(int key){
    	return !previousKeys[key] && keys[key];
    }
    public static boolean released(int key){
    	return previousKeys[key] && !keys[key];
    }
    
     
}