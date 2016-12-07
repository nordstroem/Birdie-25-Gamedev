package editor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

public class MapPanel extends JPanel{
	private Editor editor;
	private FileHandler fileHandler;
	private JPanel mapPanel;
	private JList<FileTile> tileList;
	
	public MapPanel(Editor editor, FileHandler fileHandler) {
		this.editor = editor;
		this.fileHandler = fileHandler;
		add(createInfoPanel());
		mapPanel = createMapPanel();
		JButton focus = new JButton("Edit");
		focus.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mapPanel.requestFocusInWindow();
			}
		});
		add(focus);
		add(mapPanel);
	}

	private JPanel createInfoPanel() {
		JPanel panel = new JPanel();
		JLabel objLabel = new JLabel("Tile size:");
		panel.add(objLabel);
		JTextField obj = new JTextField("", 20);
		panel.add(obj);
		
		tileList = new JList<FileTile>();
		tileList.setListData(fileHandler.getFileTilesCopy());
		//panel.add(tileList);
		JScrollPane tileListScroller = new JScrollPane(tileList);
		tileListScroller.setPreferredSize(new Dimension(400, 300));
		add(tileListScroller);
		return panel;
	}
	
	private JPanel createMapPanel() {
		JPanel map = new JPanel(){
			@Override
			public void paint(Graphics g) {
				super.paint(g);
				paintMap(g);
			}
		};
		map.setPreferredSize(new Dimension(600, 600));
		map.setBackground(Color.BLACK);
		map.setFocusable(true);
		map.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_UP) {
					camIndexZ--;
				}
				if (e.getKeyCode() == KeyEvent.VK_DOWN) {
					camIndexZ++;
				}
				if (e.getKeyCode() == KeyEvent.VK_LEFT) {
					camIndexX--;
				}
				if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
					camIndexX++;
				}
				if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
					if (tileDrawSize <= 20) {
						tileDrawSize++;
						editor.zoomIn();
					}
				}
				if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
					if (tileDrawSize >= 3) {
						tileDrawSize--;
						editor.zoomOut();
					}
				}
				
				if (e.getKeyCode() == KeyEvent.VK_SPACE) {
					addSelectedTile();
				}
				if (e.getKeyCode() == KeyEvent.VK_S) {
					int sel = tileList.getSelectedIndex();
					tileList.setSelectedIndex((sel + 1) % tileList.getModel().getSize());
					mapPanel.requestFocusInWindow();
				}
				if (e.getKeyCode() == KeyEvent.VK_W) {
					int sel = tileList.getSelectedIndex();
					int newIndex = sel - 1 == -1 ? tileList.getModel().getSize() - 1: sel - 1;
					tileList.setSelectedIndex(newIndex);
					mapPanel.requestFocusInWindow();
				}
				if (e.getKeyCode() == KeyEvent.VK_R) {
					rot++;
					rot %= 4;
				}
				
				if (e.getKeyCode() == KeyEvent.VK_D) {
					deleteTile();
				}
				
				synchWithEditor();
			}
		});
		new Thread(){
			@Override
			public void run() {
				populateMap();
				while(true){
					try {
						Thread.sleep(20);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					map.repaint();
				}
			}
		}.start();
		return map;
	}
	
	private void deleteTile(){
		FileMapTile removedTile = map[camIndexX][camIndexZ];
		if (removedTile != null) {
			int removeRadius = 100; // TODO problem om tiles större än detta
			for (int x = camIndexX - removeRadius; x < camIndexX + 2 * removeRadius; x++) {
				for (int z = camIndexZ - removeRadius; z < camIndexZ + 2 * removeRadius; z++) {
					if (x >= 0 && x < map.length && z >= 0 && z < map[0].length) {
						if (map[x][z] != null && removedTile == map[x][z]) {
							map[x][z] = null;
						}
					}
				}
			}
			fileHandler.removeMapTile(removedTile);
		}
	}
	private void addSelectedTile(){
		FileTile selectedTile = tileList.getSelectedValue();
		FileMapTile newTile = new FileMapTile();
		newTile.tileId = selectedTile.tileId;
		newTile.rot = rot;
		
		int hx1 = (int) Math.floor(selectedTile.sizeX / 2.0); 
		int hx2 = (int) Math.ceil(selectedTile.sizeX / 2.0);
		int hz1 = (int) Math.floor(selectedTile.sizeZ / 2.0);
		int hz2 = (int) Math.ceil(selectedTile.sizeZ / 2.0);
		int x1 = -hx1;
		int z1 = -hz1;
		int x2 = hx2;
		int z2 = hz2;
		
		int rot = newTile.rot;
		if (rot == 1) {
			int tmp = z1;
			z1 = x1;
			x1 = -tmp;
		} else if (rot == 2) {
			x1 = -x1;
			z1 = -z1;
		} else if (rot == 3) {
			int tmp = z1;
			z1 = -x1;
			x1 = tmp;
		}
		
		if (rot == 1) {
			int tmp = z2;
			z2 = x2;
			x2 = -tmp;
		} else if (rot == 2) {
			x2 = -x2;
			z2 = -z2;
		} else if (rot == 3) {
			int tmp = z2;
			z2 = -x2;
			x2 = tmp;
		}
		
		x1 += camIndexX;
		x2 += camIndexX;
		z1 += camIndexZ;
		z2 += camIndexZ;

		int xStart = Math.min(x1, x2);
		int w = Math.max(x1, x2) - xStart;
		int zStart = Math.min(z1, z2);
		int h = Math.max(z1, z2) - zStart;
		
		newTile.ix = xStart;
		newTile.iz = zStart;
		
		HashSet<FileMapTile> removedTiles = new HashSet<FileMapTile>();
		for (int x = xStart; x < xStart + w; x++) { 
			for (int z = zStart; z < zStart + h; z++) {
				removedTiles.add(map[x][z]);
				map[x][z] = newTile;
			}
		}
		
		int removeRadius = 100; // TODO problem om tiles större än detta
			
		for (int x = camIndexX - removeRadius; x < camIndexX + 2 * removeRadius; x++) {
			for (int z = camIndexZ - removeRadius; z < camIndexZ + 2 * removeRadius; z++) {
				if (x >= 0 && x < map.length && z >= 0 && z < map[0].length) {
					if (map[x][z] != null && removedTiles.contains(map[x][z])) {
						map[x][z] = null;
					}
				}
			}
		}
		
		fileHandler.removeMapTiles(removedTiles);
		fileHandler.addFileMapTile(newTile);
	}
	
	private void synchWithEditor() {
		editor.setCameraPositionXZ(camIndexX, camIndexZ);
	}

	private void populateMap(){
		for(FileMapTile fmt : fileHandler.getMap().fileMapTiles){
			FileTile ft = fileHandler.getFileTile(fmt.tileId);
			int w = ft.getWidth(fmt);
			int h = ft.getHeight(fmt);
			for(int x = fmt.ix; x < fmt.ix + w; x++){
				for(int z = fmt.iz; z < fmt.iz + h; z++){
					map[x][z] = fmt;
				}
			}
		}
	}
	private int mapSize = 1000;
	private int tileDrawSize = 7;
	private int camIndexX = 0;
	private int camIndexZ = 0;
	private int rot = 0; // 0,1,2 eller 3
	private FileMapTile[][] map = new FileMapTile[mapSize][mapSize];
	private void paintMap(Graphics g){
		g.setColor(Color.CYAN);
		g.fillRect(0, 0, 1000, 1000);
		int camTransX = -camIndexX * tileDrawSize + 300, camTransY = -camIndexZ * tileDrawSize + 300;
		g.translate(camTransX, camTransY);
		int halfNumTiles = (300 / tileDrawSize) + 5;
		int startx = camIndexX - halfNumTiles, endx = camIndexX + halfNumTiles, startz = camIndexZ - halfNumTiles , endz = camIndexZ + halfNumTiles;
		for(int x = startx; x < endx; x++){
			for(int z = startz; z < endz; z++){
				if (x < 0 || z < 0 || x >= map.length || z >= map[0].length) {
					continue;
				}
				int rx = x * tileDrawSize;
				int rz = z * tileDrawSize;
				if (map[x][z] != null) {
					g.setColor(new Color(new Random(map[x][z].tileId).nextInt(255*255*255)));
					g.fillRect(rx, rz, tileDrawSize, tileDrawSize ); 
				}else{
					g.setColor(Color.WHITE);
					g.fillRect(rx, rz, tileDrawSize, tileDrawSize);
				}
				
				g.setColor(new Color(0,0,0, 30));
				g.drawRect(rx, rz, tileDrawSize, tileDrawSize);
				// Draw "crosshair"
				if (x == camIndexX && z == camIndexZ) {
					g.setColor(Color.RED);
					g.drawLine(rx, rz, rx + tileDrawSize, rz + tileDrawSize);
					g.drawLine(rx + tileDrawSize, rz, rx, rz + tileDrawSize);
				}
			}
		}
		
		// Draw black lines
		for(int x = startx; x < endx; x++){
			for(int z = startz; z < endz; z++){
				if (x < 0 || z < 0 || x >= map.length || z >= map[0].length) {
					continue;
				}
				int rx = x * tileDrawSize;
				int rz = z * tileDrawSize;
				if (x + 1 < map.length && z < map[0].length && map[x + 1][z] != null && map[x + 1][z] != map[x][z]) {
					g.setColor(Color.BLACK);
					g.drawLine(rx + tileDrawSize, rz, rx + tileDrawSize, rz + tileDrawSize);
				}
				if (x < map.length && z + 1 < map[0].length && map[x][z + 1] != null && map[x][z + 1] != map[x][z]) {
					g.setColor(Color.BLACK);
					g.drawLine(rx , rz + tileDrawSize, rx + tileDrawSize, rz + tileDrawSize);
				}
			}
		}
		
		// Draw selected tile
		FileTile selectedTile = tileList.getSelectedValue();
		if (selectedTile != null) {
			g.setColor(new Color(255,0,0,128));

			int hx1 = (int) Math.floor(selectedTile.sizeX / 2.0); 
			int hx2 = (int) Math.ceil(selectedTile.sizeX / 2.0);
			int hz1 = (int) Math.floor(selectedTile.sizeZ / 2.0);
			int hz2 = (int) Math.ceil(selectedTile.sizeZ / 2.0);
			int x1 = -hx1;
			int z1 = -hz1;
			int x2 = hx2;
			int z2 = hz2;
			
			if (rot == 1) {
				int tmp = z1;
				z1 = x1;
				x1 = -tmp;
			} else if (rot == 2) {
				x1 = -x1;
				z1 = -z1;
			} else if (rot == 3) {
				int tmp = z1;
				z1 = -x1;
				x1 = tmp;
			}
			
			if (rot == 1) {
				int tmp = z2;
				z2 = x2;
				x2 = -tmp;
			} else if (rot == 2) {
				x2 = -x2;
				z2 = -z2;
			} else if (rot == 3) {
				int tmp = z2;
				z2 = -x2;
				x2 = tmp;
			}
			
			x1 += camIndexX;
			x2 += camIndexX;
			z1 += camIndexZ;
			z2 += camIndexZ;
			
			x1 *= tileDrawSize;
			z1 *= tileDrawSize;
			x2 *= tileDrawSize;
			z2 *= tileDrawSize;
			int x = Math.min(x1, x2);
			int w = Math.max(x1, x2) - x;
			int z = Math.min(z1, z2);
			int h = Math.max(z1, z2) - z;
			g.fillRect(x, z, w, h);
		}
		
		
		g.translate(-camTransX, -camTransY);
		if (!mapPanel.hasFocus()) {
			g.setColor(new Color(0, 0, 0, 128));
			g.fillRect(0, 0, 600, 600);
		}
	}
	
}
