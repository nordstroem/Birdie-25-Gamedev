package GLEngine;

import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glTexCoord2f;
import static org.lwjgl.opengl.GL11.glVertex2f;
import static org.lwjgl.opengl.GL11.glVertex3f;

import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JOptionPane;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.font.effects.ColorEffect;



public class OJ2D {
	private ArrayList<Save2D> save2D = new ArrayList<Save2D>();
	
	private HashMap<String, UnicodeFont> fonts = new HashMap<String,UnicodeFont>();
	
	public void addFont(String name, Font font){
		GL11.glDisable(GL11.GL_TEXTURE_2D); // behövs annars kan slick fucka up saker
		UnicodeFont unicodeFont = new UnicodeFont(font);
		unicodeFont.getEffects().add(new ColorEffect());
		unicodeFont.addAsciiGlyphs();

		try {
			unicodeFont.loadGlyphs();
		} catch (SlickException e) {
			JOptionPane.showMessageDialog(null, "OJ2D FONT : "+e.getMessage());
		}
		
		fonts.put(name, unicodeFont);
		GL11.glEnable(GL11.GL_TEXTURE_2D);// behövs annars kan slick fucka up saker

	}

	

	public void init() {

	}

	public void beginRender() {
		save2D.clear();
	}

	public void endRender() {

	}

	public void render() {
		for (int i = 0; i < save2D.size(); i++) {
			Save2D s2D = save2D.get(i);
			switch (s2D.type) {
			case Save2D.FILLRECT:
				fillRectRender(s2D.x, s2D.y, s2D.w, s2D.h, s2D.color);
				break;
			case Save2D.DRAWSTRING:
				drawStringRender(s2D.x, s2D.y, s2D.s, s2D.uf,s2D.color);
				break;
			case Save2D.DRAWRECT:
				drawRectRender(s2D.x, s2D.y, s2D.w, s2D.h, s2D.color);
				break;
			case Save2D.DRAWLINE:
				drawLineRender(s2D.x, s2D.y, s2D.w, s2D.h, s2D.color);
				break;
			case Save2D.DRAWIMAGE:
				drawImageRender(s2D.x, s2D.y, s2D.w, s2D.h, s2D.texID);
				break;
			default:
				System.err.println("ogiltigt 2d render typ: "+s2D.type);
			}
		}
		
	}
	public void drawString(int x,int y,String s, String font,Vec3 color){
		UnicodeFont uf = fonts.get(font);
		if(uf != null){
			save2D.add(new Save2D(Save2D.DRAWSTRING,x,y,s,uf, color));
		}else{
			System.err.println("fel font name");
		}
		
	}

	private void drawStringRender(int x, int y, String s, UnicodeFont uf,Vec3 color){
		uf.drawString(x, y, s, new org.newdawn.slick.Color(color.x,color.y,color.z));
	
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
}
	public void fillRect(int x,int y, int w, int h, Vec3 color){
		save2D.add(new Save2D(Save2D.FILLRECT,x,y,w,h,color));
	}

	private void fillRectRender(int x,int y, int w, int h, Vec3 color){
		
		glColor3f(color.x,color.y,color.z);
		glBegin(GL_QUADS);
		glVertex2f(x, y);
		glVertex2f(x + w, y);
		glVertex2f(x + w, y + h);
		glVertex2f(x, y + h);
		glEnd();	
		
	}
	public void drawRect(int x,int y, int w, int h, Vec3 color){
		save2D.add(new Save2D(Save2D.DRAWRECT,x,y,w,h,color));
	}

	private void drawRectRender(int x,int y, int w, int h, Vec3 color){
		
		glColor3f(color.x,color.y,color.z);
		glBegin(GL_LINES);
		glVertex2f(x, y); 	glVertex2f(x+w, y);
		glVertex2f(x+w, y); glVertex2f(x+w, y+h);
		glVertex2f(x+w, y+h); glVertex2f(x, y+h);
		glVertex2f(x, y+h); glVertex2f(x, y);
		glEnd();
		
	}
	public void drawLine(int x,int y, int w, int h, Vec3 color){
		save2D.add(new Save2D(Save2D.DRAWLINE,x,y,w,h,color));
	}

	private void drawLineRender(int x,int y, int w, int h, Vec3 color){
		
		glColor3f(color.x,color.y,color.z);
		 glBegin(GL_LINES);
		 glVertex2f(x, y); 
		 glVertex2f(w, h);
		 glEnd();
		
	}
	public void drawImage(int x,int y, int w, int h, int ID){
		save2D.add(new Save2D(Save2D.DRAWIMAGE,x,y,w,h,ID));
	}

	private void drawImageRender(int x, int y, int w, int h,int textureID){
		glPushMatrix();
		
		org.newdawn.slick.Color.white.bind();
		glBindTexture(GL_TEXTURE_2D, textureID);
		glBegin(GL_QUADS);

		glTexCoord2f(1, 0);
		glVertex3f(x+w, y, 0);
		glTexCoord2f(0, 0);
		glVertex3f(x, y, 0);
		glTexCoord2f(0, 1);
		glVertex3f(x, y+h, 0);
		glTexCoord2f(1, 1);
		glVertex3f(x+w, y+h, 0);

		glEnd();

		glBindTexture(GL_TEXTURE_2D, 0);
		glPopMatrix();
		
		
	}
	private class Save2D{ //klass för att komma ihåg 2D
		public static final int FILLRECT = 1;
		public static final int DRAWSTRING = 2;
		public static final int DRAWRECT = 3;
		public static final int DRAWLINE = 4;
		public static final int DRAWIMAGE = 5;
		public int type;

		public int x,y,w,h,texID;
		public Vec3 color;
		public String s;
		public UnicodeFont uf;
		public Save2D(int type,int x,int y,int w,int h,Vec3 color){//fillRect och drawRect och line mellan (x,y) och (w,h)
			this.type = type;
			this.x=x; this.y=y; this.w=w; this.h=h; this.color=color;
		}
		public Save2D(int type,int x,int y,String s,UnicodeFont uf,Vec3 color){//drawString
			this.type = type;
			this.x=x; this.y=y; this.s=s;  this.uf=uf; this.color=color;
		}
		public Save2D(int type,int x,int y,int w, int h,int ID){//drawImage
			this.type = type;
			this.x=x; this.y=y; this.w=w; this.h=h; this.texID = ID;
		}

	}
}
class Vec3 {

	public float x, y, z;
	public static final Vec3 GRAY = new Vec3(0.5f, 0.5f, 0.5f);
	public static final Vec3 RED = new Vec3(1, 0, 0);
	public static final Vec3 GREEN = new Vec3(0, 1, 0);
	public static final Vec3 BLUE = new Vec3(0, 0, 1);
	public static final Vec3 CYAN = new Vec3(0, 1, 1);
	public static final Vec3 WHITE = new Vec3(1, 1, 1);
	public static final Vec3 BLACK = new Vec3(0, 0, 0);
	public static final Vec3 YELLOW = new Vec3(1, 1, 0);
	public static final Vec3 BROWN = new Vec3(160,82,45).multiply(1/255f);

	public Vec3(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	public void setToZero(){
		this.x = 0;
		this.y = 0;
		this.z = 0;
	}
	public Vec3(float v) {
		this.x = v;
		this.y = v;
		this.z = v;
	}

	public Vec3 add(Vec3 v) {
		return new Vec3(this.x + v.x, this.y + v.y, this.z + v.z);
	}
	public Vec3 add(float x, float y, float z) {
		return new Vec3(this.x + x, this.y + y, this.z + z);
	}
	public Vec3 subtract(Vec3 v) {
		return new Vec3(this.x - v.x, this.y - v.y, this.z - v.z);
	}

	public Vec3 multiply(float f) {
		return new Vec3(this.x * f, this.y * f, this.z * f);
	}
	public Vec3() {

	}
	
	public Vec3 cross(Vec3 b){
		return new Vec3(this.y*b.z - this.z*b.y, this.z*b.x - this.x*b.z, this.x*b.y - this.y*b.x);
	}

	public Vec3 clone() {
		return new Vec3(x, y, z);
	}
	public float norm(){
		return (float)Math.sqrt(this.x*this.x + this.y*this.y + this.z*this.z);
	}
	public String toString() {
		return "(" + x + ", " + y + ", " + z + ")";
	}

}
