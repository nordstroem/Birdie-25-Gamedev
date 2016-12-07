package GLEngine;

import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_LINEAR_MIPMAP_LINEAR;
import static org.lwjgl.opengl.GL11.GL_REPEAT;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import javax.imageio.ImageIO;

public class Texture {
	
	
	
	private static HashMap<String, Integer> textureMap = new HashMap<String, Integer>();
	public static int RED = new Texture("textures/red.png").textureID;
	public static int TRASH_CAN = new Texture("textures/trashCan.png").textureID;
	public static int WELL = new Texture("textures/well.png").textureID;
	public static int SKYBOX = new Texture("textures/skybox.png").textureID;
	public static int HOHOHAHA = new Texture("textures/text/hohohaha.png").textureID;
	public static int YELLOW = new Texture("textures/yellow.png").textureID;
	public static int TRANSPARENT = new Texture("textures/transparent.png").textureID;
	public static int LOADINGSCREEN = new Texture("textures/loadingscreen.png").textureID;
	public static int NEW_CUSTOMER = new Texture("textures/new_customer.png").textureID;
	public static int MINIMAP_BORDER = new Texture("textures/minimapborder.png").textureID;
	public static int CIRCLE = new Texture("textures/circle.png").textureID;
	public static int CIRCLE2 = new Texture("textures/circle2.png").textureID;
	public static int RECTANGLE = new Texture("textures/rectangle.png").textureID;
	public static int TRIANGLE = new Texture("textures/triangle.png").textureID;
	public static int FOX = new Texture("textures/LP_Fox_1_Map2.png").textureID;
	
	public static int RADIO_HARDBASE = new Texture("textures/radio/hardbase.png").textureID;
	public static int RADIO_HARDCORE = new Texture("textures/radio/hardcore.png").textureID;
	public static int RADIO_DEATH = new Texture("textures/radio/death.png").textureID;
	public static int RADIO_HIPHOP = new Texture("textures/radio/81hiphop.png").textureID;
	public static int RADIO_PAGAN = new Texture("textures/radio/pagan.png").textureID;
	public static int RADIO_JAZZ = new Texture("textures/radio/jazz.png").textureID;
	
	public static int MINIMAP = -1;
	public static int MINIMAP_SIZE = -1;
	
	public static int MENU_MAIN_START = new Texture("textures/menu/main_start.png").textureID;
	public static int MENU_MAIN_HIGHSCORE = new Texture("textures/menu/main_highscore.png").textureID;
	public static int MENU_MAIN_HELP = new Texture("textures/menu/main_help.png").textureID;
	public static int MENU_MAIN_EXIT = new Texture("textures/menu/main_exit.png").textureID;
	
	public static int MENU_HELP = new Texture("textures/menu/help.png").textureID;
	
	public static int NUM_0 = new Texture("textures/text/0.png").textureID;
	public static int NUM_1 = new Texture("textures/text/1.png").textureID;
	public static int NUM_2 = new Texture("textures/text/2.png").textureID;
	public static int NUM_3 = new Texture("textures/text/3.png").textureID;
	public static int NUM_4 = new Texture("textures/text/4.png").textureID;
	public static int NUM_5 = new Texture("textures/text/5.png").textureID;
	public static int NUM_6 = new Texture("textures/text/6.png").textureID;
	public static int NUM_7 = new Texture("textures/text/7.png").textureID;
	public static int NUM_8 = new Texture("textures/text/8.png").textureID;
	public static int NUM_9 = new Texture("textures/text/9.png").textureID;
	
	
	public static int getNumberID(int num){
		if(num == 0) return NUM_0;
		if(num == 1) return NUM_1;
		if(num == 2) return NUM_2;
		if(num == 3) return NUM_3;
		if(num == 4) return NUM_4;
		if(num == 5) return NUM_5;
		if(num == 6) return NUM_6;
		if(num == 7) return NUM_7;
		if(num == 8) return NUM_8;
		return NUM_9;	
	}
		
	private int width, height;
	public int textureID;
	
	public Texture(String path) {
		textureID = load(path);
	}
	
	public static int getTexture(String path){
		if(textureMap.containsKey(path)){
			return textureMap.get(path);
		}else{
			return new Texture(path).textureID;
		}
	}

	private int load(String path) {
		if (textureMap.containsKey(path)) {
			return textureMap.get(path);
		}
		int[] pixels = null;
		try {

			InputStream in = Utilities.class.getResourceAsStream("/" + path);
			BufferedImage image =  ImageIO.read(in);

			width = image.getWidth();
			height = image.getHeight();
			pixels = new int[width * height];
			image.getRGB(0, 0, width, height, pixels, 0, width);
		} catch (Exception e) {
			System.out.println("fel med textur: " + path);
			e.printStackTrace();
		}

		int[] data = new int[width * height];
		for (int i = 0; i < width * height; i++) {
			int a = (pixels[i] & 0xff000000) >> 24;
			int r = (pixels[i] & 0xff0000) >> 16;
			int g = (pixels[i] & 0xff00) >> 8;
			int b = (pixels[i] & 0xff);

			data[i] = a << 24 | b << 16 | g << 8 | r;
		}

		int result = glGenTextures();
		
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, result);
		//glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		//glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR); 
		//glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
		//glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR); 
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, Utilities.createIntBuffer(data));
		glGenerateMipmap(GL_TEXTURE_2D);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
		
		

		
		glBindTexture(GL_TEXTURE_2D, 0);
		textureMap.put(path, result);
		return result;
	}
	
	public void bind() {
		glBindTexture(GL_TEXTURE_2D, textureID);
	}

	public void unbind() {
		glBindTexture(GL_TEXTURE_2D, 0);
	}
	
	public void free(){
		glDeleteTextures(textureID);
		
	}

	public static int createTexture(BufferedImage image) {
		int[] pixels = null;
		int width = 0;
		int height = 0;
		
		

		//BufferedImage image =  minimap;

		width = image.getWidth();
		height = image.getHeight();
		pixels = new int[width * height];
		image.getRGB(0, 0, width, height, pixels, 0, width);

		int[] data = new int[width * height];
		for (int i = 0; i < width * height; i++) {
			int a = (pixels[i] & 0xff000000) >> 24;
			int r = (pixels[i] & 0xff0000) >> 16;
			int g = (pixels[i] & 0xff00) >> 8;
			int b = (pixels[i] & 0xff);

			data[i] = a << 24 | b << 16 | g << 8 | r;
		}

		int result = glGenTextures();
		
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, result);
		//glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		//glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR); 
		//glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
		//glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR); 
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, Utilities.createIntBuffer(data));
		glGenerateMipmap(GL_TEXTURE_2D);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
		
		

		
		glBindTexture(GL_TEXTURE_2D, 0);
		return result;
	}

	public static void createMinimap(BufferedImage minimap) {
		MINIMAP = createTexture(minimap);
		if (minimap.getWidth() != minimap.getHeight()) {
			throw new RuntimeException("minimap image width != height");
		}
		MINIMAP_SIZE = minimap.getHeight();
	}

	private static Font loadFont(String path,float size) {
		InputStream is = Texture.class.getResourceAsStream(path);
		Font font = null;
		try {
			font = Font.createFont(Font.TRUETYPE_FONT, is);
		} catch (FontFormatException | IOException e) {
			e.printStackTrace();
		}
	    return font.deriveFont(size);
	}
	
	private static int[] CHAR_ID;
	private static int[] CHAR_WIDTH;
	private static int[] CHAR_HEIGHT;
	static{
		int num = 256;
		CHAR_ID = new int[num];
		CHAR_WIDTH = new int[num];
		CHAR_HEIGHT = new int[num];
		int height = 72;
		//Font font = new Font("Times New Roman", Font.PLAIN, height);
		Font font = loadFont("/fonts/GROBOLD.ttf", height);
		FontMetrics metrics = new BufferedImage(30, 30, BufferedImage.TYPE_INT_ARGB).getGraphics().getFontMetrics(font);
		for (char c = 0; c < num; c++) {
			if (Character.isISOControl(c)) {
				continue;
			}
			int hgt = metrics.getHeight();
			int adv = metrics.stringWidth(""+c);
			Dimension size = new Dimension(adv+10, hgt+8);
			BufferedImage charImg = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
			Graphics go = charImg.getGraphics();
			Graphics2D g = (Graphics2D)go;
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
			RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
			RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g.setFont(font);
			g.setColor(new Color(0,0,0,0));
			g.fillRect(0, 0, size.width, size.height);
			g.setColor(new Color(32/255f,32/255f,32/255f,1));
			g.drawString(""+c, 5, size.height-2);
			g.setColor(Color.WHITE);
			g.drawString(""+c, 5, size.height-4);
			CHAR_ID[c] =  createTexture(charImg);
			CHAR_WIDTH[c] = size.width;
			CHAR_HEIGHT[c] = size.height;
			
		}
	}
	public static int getCharID(char c) {
		return CHAR_ID[c];
	}

	public static int getCharWidth(char c) {
		return CHAR_WIDTH[c];
	}

	public static int getCharHeight(char c) {
		return CHAR_HEIGHT[c];
	}
}
