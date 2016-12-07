package editor;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.vecmath.Vector3f;

import physics.Physics;
import GLEngine.Mesh;
import GLEngine.Texture;
import GLEngine.Utilities;
import entity.DynamicEntity;
import entity.Entity;
import game.TrafficInfo;

class FileEntity{ // Info om en typ av entity
	String name = "TODO";
	String obj = "TODO";
	String tex = "TODO";
	double weight = -1;
	String collision = "TODO";
	int entityId = -1;
	
	public String toString(){
		return "Entity: " + name + " " + obj + " " + tex + " " + weight + " " + collision;
	}
}

class FileTileEntity{ // Info om en specifik entity, sparas per tile
	int entityId;
	double px, py, pz, rx, ry, rz;
	double probability = 1;
	FileHandler fileHandler;
	public FileTileEntity(FileHandler fh) {
		fileHandler = fh;
	}
	@Override
	public String toString() {
		return "TileEntity: " + (fileHandler == null ? "?" : fileHandler.getEntityName(entityId));
	}
	public Vector3f getRotatedPos(FileMapTile fmt, FileTile ft) {
		Vector3f pos = new Vector3f();
		float centerx = (float) (ft.sizeX / 2.0);
		float centerz = (float) (ft.sizeZ / 2.0);
		float transx = (float) (px - centerx);
		float transz = (float) (pz - centerz);
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
		pos.x = transx + centerx;
		pos.z = transz + centerz;
		pos.y = (float) py;
		return pos;
	}
}

class FileTrafficNode{
	float x, y = 0, z;
	int prev; // -1 om ingen
	public FileTrafficNode(float x, float z, int prev) {
		this.x = x;
		this.z = z;
		this.prev = prev;
	}
	public Vector3f getRotatedPos(FileMapTile fmt, FileTile ft) {
		Vector3f pos = new Vector3f();
		float centerx = (float) (ft.sizeX / 2);
		float centerz = (float) (ft.sizeZ / 2);
		float transx = (float) (x - centerx);
		float transz = (float) (z - centerz);
		// TODO vet inte varför case 1 och 3 ska byta plats här, men verkar bli rätt om man gör så
		if (fmt.rot == 3) {
			float tmp = transz;
			transz = transx;
			transx = -tmp;
		} else if (fmt.rot == 2) {
			transx = -transx;
			transz = -transz;
		} else if (fmt.rot == 1) {
			float tmp = transz;
			transz = -transx;
			transx = tmp;
		}
		pos.x = transx + centerx;
		pos.z = transz + centerz;
		return pos;
	}
}
class FileTile{ // Info om en typ av tile
	String name = "TODO";
	int sizeX; // I antal tiles
	int sizeZ;
	ArrayList<FileTrafficNode> nodes = new ArrayList<FileTrafficNode>();
	ArrayList<FileTileEntity> entities = new ArrayList<FileTileEntity>();
	int tileId;
	String minimap = "TODO.png";
	ArrayList<FileCustomerNode> customerNodes = new ArrayList<FileCustomerNode>();
	
	// Bredden på denna tile efter rotTiles rotation
	public int getWidth(FileMapTile rotTile){
		return rotTile.rot % 2 == 0 ? sizeX : sizeZ;
	}
	// Höjden på denna tile efter rotTiles rotation
	public int getHeight(FileMapTile rotTile){
		return rotTile.rot % 2 != 0 ? sizeX : sizeZ;
	}
	
	@Override
	public String toString() {
		return "Tile: " + name;
	}
}

class FileCustomerNode{
	double px, py, pz;
	public FileCustomerNode(float x, float y, float z) {
		px = x;
		py = y;
		pz = z;
	}
	public Vector3f getRotatedPos(FileMapTile fmt, FileTile ft) {
		Vector3f pos = new Vector3f();
		float centerx = (float) (ft.sizeX / 2);
		float centerz = (float) (ft.sizeZ / 2);
		float transx = (float) (px - centerx);
		float transz = (float) (pz - centerz);
		// TODO vet inte varför case 1 och 3 ska byta plats här, men verkar bli rätt om man gör så
		if (fmt.rot == 3) {
			float tmp = transz;
			transz = transx;
			transx = -tmp;
		} else if (fmt.rot == 2) {
			transx = -transx;
			transz = -transz;
		} else if (fmt.rot == 1) {
			float tmp = transz;
			transz = -transx;
			transx = tmp;
		}
		pos.x = transx + centerx;
		pos.z = transz + centerz;
		return pos;
	}

}

class FileMapTile{
	int tileId;
	int ix; // ix iz ska vara uppe till vänster alltid oavsett rot 
	int iz; 
	double y;
	int rot;
}

class FileMap{
	double tileSize;
	ArrayList<FileMapTile> fileMapTiles = new ArrayList<FileMapTile>();
	
}
public class FileHandler {
	private ArrayList<FileEntity> fileEntities = new ArrayList<FileEntity>();
	private ArrayList<FileTile> fileTiles = new ArrayList<FileTile>();
	private FileMap map = new FileMap();
	private boolean loaded = false;
	
	public synchronized void load(){
		loaded = true;
		
		InputStream in = Utilities.class.getResourceAsStream("/map/entities");
		Scanner s = new Scanner(in);
		s.useLocale(Locale.US); // Locale us så punkt istället för komma vid decimaltal
		
		int id = 0;
		while(s.hasNext()) {
			FileEntity fe = new FileEntity();
			fe.name = nextLine(s);
			fe.obj = nextLine(s);
			fe.tex = nextLine(s);
			fe.collision = nextLine(s);
			fe.weight = Double.parseDouble(nextLine(s));
			fe.entityId = id;
			fileEntities.add(fe);
			id++;
		}
		s.close();

		in = Utilities.class.getResourceAsStream("/map/tiles");
		s = new Scanner(in);
		s.useLocale(Locale.US);
		
		id = 0;
		while(s.hasNext()) {
			FileTile ft = new FileTile();
			ft.name = nextLine(s);
			ft.sizeX = Integer.parseInt(nextLine(s));
			ft.sizeZ = Integer.parseInt(nextLine(s));
			int numNodes = Integer.parseInt(nextLine(s));
			for(int i = 0; i < numNodes; i++) {
				String line = nextLine(s);
				String[] vals = line.split(" ");
				FileTrafficNode ftn = new FileTrafficNode(Float.parseFloat(vals[0]), Float.parseFloat(vals[1]), Integer.parseInt(vals[2]));
				ft.nodes.add(ftn);
			}
			
			int numEntities = Integer.parseInt(nextLine(s));
			for(int i = 0; i < numEntities; i++) {
				FileTileEntity fte = new FileTileEntity(this);
				Scanner ls = new Scanner(nextLine(s));
				ls.useLocale(Locale.US);
				fte.entityId = ls.nextInt();
				
				fte.px = ls.nextDouble();
				fte.py = ls.nextDouble();
				fte.pz = ls.nextDouble();

				fte.rx = ls.nextDouble();
				fte.ry = ls.nextDouble();
				fte.rz = ls.nextDouble();

				fte.probability = ls.nextDouble();
				
				ls.close();
				ft.entities.add(fte);	
			}
			
			int numCustomerNodes = Integer.parseInt(nextLine(s));
			for(int i = 0; i < numCustomerNodes; i++) {
				String line = nextLine(s);
				String[] vals = line.split(" ");
				FileCustomerNode fcn = new FileCustomerNode(Float.parseFloat(vals[0]), Float.parseFloat(vals[1]), Float.parseFloat(vals[2]));
				ft.customerNodes.add(fcn);
			}
			
			ft.minimap = nextLine(s);
			ft.tileId = id;
			fileTiles.add(ft);
			id++;
		}
		
		s.close();

		in = Utilities.class.getResourceAsStream("/map/map");
		s = new Scanner(in);
		s.useLocale(Locale.US);
		
		map.tileSize = Double.parseDouble(nextLine(s));
		while(s.hasNext()) {
			String line = nextLine(s);
			Scanner ls = new Scanner(line);
			ls.useLocale(Locale.US);
			FileMapTile fmt = new FileMapTile();
			fmt.tileId = ls.nextInt();
			fmt.ix = ls.nextInt();
			fmt.iz = ls.nextInt();
			fmt.y = ls.nextDouble();
			fmt.rot = ls.nextInt();
			map.fileMapTiles.add(fmt);
			ls.close();
		}
	}
	
	public String getEntityName(int entityId) {
		return fileEntities.get(entityId).name;
	}

	public synchronized void save(){
		if (!loaded) {
			throw new RuntimeException("Load must be called before save");
		}
		PrintWriter pw = null;
		try {
			 pw = new PrintWriter(new BufferedWriter(new FileWriter("src/map/entities", false)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		saveEntities(pw);
		pw.close();
		
		try {
			 pw = new PrintWriter(new BufferedWriter(new FileWriter("src/map/tiles", false)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		saveTiles(pw);
		pw.close();
		
		try {
			 pw = new PrintWriter(new BufferedWriter(new FileWriter("src/map/map", false)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		saveMap(pw);
		pw.close();
	}
	
	private void saveEntities(PrintWriter pw) {
		for (FileEntity fe: fileEntities) {
			pw.println(fe.name);
			pw.println(fe.obj);
			pw.println(fe.tex);
			pw.println(fe.collision);
			pw.println(fe.weight);
			pw.println();
		}
	}

	private void saveTiles(PrintWriter pw) {
		for (FileTile ft: fileTiles) {
			pw.println(ft.name);
			pw.println(ft.sizeX);
			pw.println(ft.sizeZ);
			pw.println(ft.nodes.size());
			for (FileTrafficNode ftn: ft.nodes) {
				pw.print(ftn.x + " ");
				pw.print(ftn.z + " ");
				pw.print(ftn.prev);
				pw.println();
			}
			pw.println(ft.entities.size());
			for (FileTileEntity fte: ft.entities) {
				pw.print(fte.entityId + " ");
				pw.print(fte.px + " ");
				pw.print(fte.py + " ");
				pw.print(fte.pz + " ");
				pw.print(fte.rx + " ");
				pw.print(fte.ry + " ");
				pw.print(fte.rz + " ");
				pw.print(fte.probability + " ");
				pw.println();
			}
			
			pw.println(ft.customerNodes.size());
			for (FileCustomerNode fcn : ft.customerNodes) {
				pw.print(fcn.px + " ");
				pw.print(fcn.py + " ");
				pw.print(fcn.pz);
				pw.println();
			}
			pw.println(ft.minimap);
			pw.println();
		}
		
	}

	private void saveMap(PrintWriter pw) {
		pw.println(map.tileSize);
		for (FileMapTile fmt : map.fileMapTiles) {
			pw.print(fmt.tileId + " ");
			pw.print(fmt.ix + " ");
			pw.print(fmt.iz + " ");
			pw.print(fmt.y + " ");
			pw.print(fmt.rot + " ");
			pw.println();
		}
	}

	public synchronized BufferedImage createDynamicEntities(Physics physics, ArrayList<Entity> entities, TrafficInfo trafficInfo, ArrayList<GLEngine.Vector3f> customerNodes){
		if (!loaded) {
			throw new RuntimeException("Load must be called before createDynamicEntities");
		}
		Random random = new Random(9365825);
		double tileSize = map.tileSize;
		for (FileMapTile fmt: map.fileMapTiles) {
			int tileId = fmt.tileId;
			int ix = fmt.ix;
			int iz = fmt.iz;
			double y = fmt.y;
			int r = fmt.rot;
			for (FileTileEntity fte: fileTiles.get(tileId).entities) {
				if (random.nextDouble() > fte.probability) {
					continue;
				}
				FileEntity fe = fileEntities.get(fte.entityId);
				Vector3f pos = fte.getRotatedPos(fmt, fileTiles.get(tileId));
				pos.x += ix * tileSize;
				pos.z += iz * tileSize;
				pos.y += fmt.y;
				Vector3f rot = new Vector3f(0, (float)Math.PI * 0.5f * r, 0); 
				entities.add(new DynamicEntity(physics, pos, rot, Mesh.getMesh("objfiles/" +fe.obj, 1f), fe.collision, new Texture("textures/"+fe.tex).textureID,  fe.weight));
			}
			
			trafficInfo.newNodeBatch();
			for (FileTrafficNode ftn : fileTiles.get(tileId).nodes) {
				Vector3f pos = ftn.getRotatedPos(fmt, fileTiles.get(tileId));
				pos.x += ix * tileSize;
				pos.z += iz * tileSize;
				trafficInfo.addIntermediateNode(pos, ftn.prev);
			}
			
			for (FileCustomerNode fcn : fileTiles.get(tileId).customerNodes) {
				Vector3f pos = fcn.getRotatedPos(fmt, fileTiles.get(tileId));
				pos.x += ix * tileSize;
				pos.z += iz * tileSize;
				customerNodes.add(new GLEngine.Vector3f(pos.x, 1, pos.z));
			}
		}
		
		int size = 16; // i pixlar
		for (FileMapTile fmt: map.fileMapTiles) {
			FileTile ft = fileTiles.get(fmt.tileId);
			int maxSize = Math.max(ft.sizeX, ft.sizeZ);
			size = Math.max(size, fmt.ix + maxSize);
			size = Math.max(size, fmt.iz + maxSize);
		}
		HashMap<String,BufferedImage> images = new HashMap<String, BufferedImage>();
		for (FileTile ft : fileTiles) {
			InputStream in = getClass().getResourceAsStream("/textures/minimap/" + ft.minimap);
			BufferedImage image = null;
			try {
				 image =  ImageIO.read(in);
			} catch (IOException e) {
				e.printStackTrace();
			}
			images.put(ft.minimap, image);
		}
		BufferedImage minimap = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		Graphics g = minimap.getGraphics();
		for (FileMapTile fmt: map.fileMapTiles) {
			FileTile ft = fileTiles.get(fmt.tileId);
			BufferedImage img = images.get(ft.minimap);
			AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
		    tx.translate(-img.getWidth(null), 0);
		    AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		    img = op.filter(img, null);
			AffineTransform identity = new AffineTransform();
			Graphics2D g2d = (Graphics2D)g;
			AffineTransform trans = new AffineTransform();
			trans.setTransform(identity);
			trans.translate(fmt.iz, fmt.ix);
			trans.rotate(Math.toRadians(270 + 90 * fmt.rot), img.getWidth() * 0.5, img.getHeight() * 0.5);
			g2d.drawImage(img, trans, null);
		}
		return minimap;
	}
	
	private String nextLine(Scanner s) {
		while (s.hasNextLine()) {
			String c = s.nextLine();
			if (!c.matches("\\s*") && !c.matches("//.*")) { // Om endast whitespace eller kommentar
				return c;
			}
		}
		throw new RuntimeException("Could not find one more matching line");
	}


	

	public synchronized Vector<FileEntity> getFileEntitiesCopy() {
		Vector<FileEntity> vector = new Vector<FileEntity>();
		for(FileEntity fe : fileEntities){
			vector.add(fe);
		}
		return vector;
	}
	
	public synchronized Vector<FileTile> getFileTilesCopy() {
		Vector<FileTile> vector = new Vector<FileTile>();
		for(FileTile ft : fileTiles){
			vector.add(ft);
		}
		return vector;
	}

	public synchronized Vector<FileTileEntity> getFileTileEntitiesCopy(int tileId) {
		Vector<FileTileEntity> vector = new Vector<FileTileEntity>();
		for(FileTileEntity fte : fileTiles.get(tileId).entities){
			vector.add(fte);
		}
		return vector;
	}

	public synchronized void addNewFileTileEntity(int selectedTile, int selectedEntity) {
		FileTileEntity fte = new FileTileEntity(this);
		fte.entityId = selectedEntity;
		fileTiles.get(selectedTile).entities.add(fte);
	}

	public synchronized void addNewEmptyTile() {
		FileTile fileTile = new FileTile();
		fileTile.tileId = fileTiles.size();
		fileTiles.add(fileTile);
	}
	
	public synchronized void addNewEmptyEntity() {
		FileEntity fileEntity = new FileEntity();
		// TODO ska man göra något med fileEntity.id?, tror dess id är onödigt
		fileEntities.add(fileEntity);
	}

	public synchronized FileEntity getEntity(int entityId) {
		return fileEntities.get(entityId);
	}

	public synchronized FileMap getMap() {
		return map;
	}

	public synchronized FileTile getFileTile(int tileId) {
		return fileTiles.get(tileId);
	}

	public void removeMapTiles(Collection<FileMapTile> removedTiles) {
		synchronized (map.fileMapTiles) {
			map.fileMapTiles.removeAll(removedTiles);
		}
	}

	public void addFileMapTile(FileMapTile newTile) {
		synchronized (map.fileMapTiles) {
			map.fileMapTiles.add(newTile);
		}
	}

	public void removeMapTile(FileMapTile removedTile) {
		synchronized (map.fileMapTiles) {
			map.fileMapTiles.remove(removedTile);
		}
	}

	public synchronized void clearNodes(FileTile fileTile) {
		if (fileTile != null) {
			fileTile.nodes.clear();
		}
		
	}

}
