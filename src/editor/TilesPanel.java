package editor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class TilesPanel extends JPanel{
	JList<FileTile> tileList = new JList<FileTile>();
	JTextField posx;
	JTextField posy;
	JTextField posz;
	JTextField probability;
	public TilesPanel(Editor editor, FileHandler fileHandler) {
		JList<FileTileEntity> tileEntitiesList = new JList<FileTileEntity>();
		JList<FileEntity> entityList = new JList<FileEntity>();
		
		//JLabel entityLabel = new JLabel();		
		//add(entityLabel);
		JLabel posLabel = new JLabel("Pos x y z:");
		add(posLabel);
		posx = new JTextField("", 5);
		add(posx);
		posy = new JTextField("", 5);
		add(posy);
		posz = new JTextField("", 5);
		add(posz);
		
		JLabel probLabel = new JLabel("%:");
		add(probLabel);
		probability = new JTextField("", 5);
		add(probability);
		
		JLabel nameLabel = new JLabel("Tile name: ");
		add(nameLabel);
		JTextField name = new JTextField("", 20);
		add(name);
		
		JLabel sizexLabel = new JLabel("Size x z: ");
		add(sizexLabel);
		JTextField sizex = new JTextField("", 5);
		add(sizex);
		JTextField sizez = new JTextField("", 5);
		add(sizez);
		
		JLabel minimapLabel = new JLabel("Minimap: ");
		add(minimapLabel);
		JTextField minimap = new JTextField("", 20){
			@Override
			public void repaint() {
				if (getDocument() != null) {
					setBackground(new File("src/textures/minimap/" + getText()).exists() ? Color.WHITE : Color.RED);
				}
				super.repaint();
			}
		};
		add(minimap);
		
		JButton newTile = new JButton("Create new tile");
		newTile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				fileHandler.addNewEmptyTile();
				Vector<FileTile> newTiles = fileHandler.getFileTilesCopy();
				tileList.setListData(newTiles);
				tileList.setSelectedIndex(newTiles.size() - 1);
				
			}
		});
		add(newTile);
		
		JButton add = new JButton("Add selected entity");
		add.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int selectedTile = tileList.getSelectedIndex();
				int selectedEntity = entityList.getSelectedIndex();
				if (selectedTile != -1 && selectedEntity != -1) {
					fileHandler.addNewFileTileEntity(selectedTile, selectedEntity);
					Vector<FileTileEntity> fileTileEntities = fileHandler.getFileTileEntitiesCopy(selectedTile);
					tileEntitiesList.setListData(fileTileEntities);
					tileEntitiesList.setSelectedIndex(fileTileEntities.size()  - 1);
				}
			}
		});
		add(add);
		
		tileList.setListData(fileHandler.getFileTilesCopy());
		tileList.addListSelectionListener(new ListSelectionListener() {
			int prev = -1;
			@Override
			public void valueChanged(ListSelectionEvent e) {
				// Om man byter tile, spara den selectade entityn
				saveTileEntity(tileEntitiesList.getSelectedValue());
				
				int sel = tileList.getSelectedIndex();
				if (prev != -1) {
					FileTile prevTile = tileList.getModel().getElementAt(prev);
					prevTile.name = name.getText();
					try{
						prevTile.sizeX = Integer.parseInt(sizex.getText());
					}catch(NumberFormatException exception){
						System.err.println("Tile size x could not be parsed");
					}
					try{
						prevTile.sizeZ = Integer.parseInt(sizez.getText());
					}catch(NumberFormatException exception){
						System.err.println("Tile size z could not be parsed");
					}
					prevTile.minimap = minimap.getText();
					
					if (prevTile != null) {
						ArrayList<FileTrafficNode> newNodes = new ArrayList<FileTrafficNode>();
						for (Node n : nodes) {
							newNodes.add(new FileTrafficNode(n.x, n.y, n.prev));
						}
						prevTile.nodes = newNodes;
						
						prevTile.customerNodes = customerNodes;
						customerNodes = new ArrayList<FileCustomerNode>();
					}
				}
				if (sel != -1) {
					tileEntitiesList.setListData(fileHandler.getFileTileEntitiesCopy(tileList.getSelectedIndex()));
					name.setText(tileList.getSelectedValue().name);
					sizex.setText("" + tileList.getSelectedValue().sizeX);
					sizez.setText("" + tileList.getSelectedValue().sizeZ);
					minimap.setText("" + tileList.getSelectedValue().minimap);
					editor.setSelectedTile(tileList.getSelectedValue());
					
					nodes.clear();
					for (FileTrafficNode n : tileList.getSelectedValue().nodes) {
						nodes.add(new Node(n.x, n.z, n.prev));
					}
					
					customerNodes = tileList.getSelectedValue().customerNodes; 
					prevNode = -1;
				}
				prev = sel;
			}
		});
		JScrollPane tileListScroller = new JScrollPane(tileList);
		tileListScroller.setPreferredSize(new Dimension(250, 80));
		add(tileListScroller);
		
		
		entityList.setListData(fileHandler.getFileEntitiesCopy());
		JScrollPane entityListScroller = new JScrollPane(entityList);
		entityListScroller.setPreferredSize(new Dimension(500, 200));
		add(entityListScroller);
		
		tileEntitiesList.addListSelectionListener(new ListSelectionListener() {
			int prev = -1;
			@Override
			public void valueChanged(ListSelectionEvent e) {
				int sel = tileEntitiesList.getSelectedIndex();
				if (prev != -1 && sel != -1) { // Om man byter tile blir sel -1, då ska vi inte skriva över något hos den nya tilen (kanske?)
					/*FileTileEntity prevTileEntity = tileEntitiesList.getModel().getElementAt(prev);
					try{
						prevTileEntity.px = Double.parseDouble(posx.getText());
					}catch(NumberFormatException exception){
						System.err.println("TileEntity pos x could not be parsed");
					}
					try{
						prevTileEntity.py = Double.parseDouble(posy.getText());
					}catch(NumberFormatException exception){
						System.err.println("TileEntity pos y could not be parsed");
					}
					try{
						prevTileEntity.pz = Double.parseDouble(posz.getText());
					}catch(NumberFormatException exception){
						System.err.println("TileEntity pos z could not be parsed");
					}*/
					saveTileEntity(tileEntitiesList.getModel().getElementAt(prev));
				}
				FileTileEntity selected = tileEntitiesList.getSelectedValue();
				if (sel != -1 && selected != null) {
					//entityLabel.setText(selected.toString());
					posx.setText("" + selected.px);
					posy.setText("" + selected.py);
					posz.setText("" + selected.pz);
					probability.setText("" + selected.probability);
				}
				
				prev = sel;
				
			}
		});
		
		JScrollPane tileEntitiesListScroller = new JScrollPane(tileEntitiesList);
		tileEntitiesListScroller.setPreferredSize(new Dimension(300, 200));
		add(tileEntitiesListScroller);
		
		JPanel trafficPanel = new JPanel(){
			@Override
			public void paint(Graphics g) {
				super.paint(g);
				trafficPaint(g);
			}
		};
		trafficPanel.setPreferredSize(new Dimension(prefferedSize, prefferedSize));
		trafficPanel.setBackground(Color.WHITE);
		/*JButton saveTI = new JButton("Save TI");
		saveTI.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				FileTile tile = tileList.getSelectedValue();
				if (tile != null) {
					ArrayList<FileTrafficNode> newNodes = new ArrayList<FileTrafficNode>();
					for (Node n : nodes) {
						newNodes.add(new FileTrafficNode(n.x, n.y, n.prev));
					}
					tile.nodes = newNodes;
				}
			}
		});*/
		JButton clearTI = new JButton("Clear TI");
		clearTI.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				prevNode = -1;
				nodes.clear();
				customerNodes.clear();
				fileHandler.clearNodes(tileList.getSelectedValue());
			}
		});
		//add(saveTI);
		add(clearTI);
		add(trafficPanel);
		
		trafficPanel.addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) {
				
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					nodes.add(new Node((float)(e.getX() / scale),(float)( e.getY() / scale), prevNode));
					prevNode++;
				} else {
					customerNodes.add(new FileCustomerNode((float)(e.getX() / scale),0,(float)( e.getY() / scale)));
				}
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				
			}
		});
		new Thread(){
			@Override
			public void run() {
				while(true){
					try {
						Thread.sleep(20);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					trafficPanel.repaint();
				}
			}
		}.start();
		trafficPanel.addMouseWheelListener(new MouseWheelListener() {
			
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				prevNode += Math.signum(e.getWheelRotation());
				if (prevNode < -1) {
					prevNode = -1;
				}
				if (prevNode >= nodes.size()) {
					prevNode = nodes.size() - 1;
				}
			}
		});
		
	}
	protected void saveTileEntity(FileTileEntity selectedEntity) {
		if (selectedEntity != null) {
			try{
				selectedEntity.px = Double.parseDouble(posx.getText());
			}catch(NumberFormatException exception){
				System.err.println("TileEntity pos x could not be parsed");
			}
			try{
				selectedEntity.py = Double.parseDouble(posy.getText());
			}catch(NumberFormatException exception){
				System.err.println("TileEntity pos y could not be parsed");
			}
			try{
				selectedEntity.pz = Double.parseDouble(posz.getText());
			}catch(NumberFormatException exception){
				System.err.println("TileEntity pos z could not be parsed");
			}
			try{
				selectedEntity.probability = Double.parseDouble(probability.getText());
			}catch(NumberFormatException exception){
				System.err.println("TileEntity probability could not be parsed");
			}
		}
		
	}
	class Node{
		float x;
		float y;
		int prev;
		public Node(float x, float y, int prev) {
			this.x = x;
			this.y = y;
			this.prev = prev;
		}
	}
	ArrayList<FileCustomerNode> customerNodes = new ArrayList<FileCustomerNode>();
	ArrayList<Node> nodes = new ArrayList<Node>();
	int prevNode = -1; // -1 = no node
	int prefferedSize = 500;
	double scale = 15;
	private void trafficPaint(Graphics g){
		FileTile tile = tileList.getSelectedValue();
		if (tile == null) {
			return;
		}
		g.setColor(Color.LIGHT_GRAY);
		g.fillRect(0, 0, (int)(tile.sizeX * scale), (int)(tile.sizeZ * scale));
		
		for(Node n : nodes){
			Node p = n;
			do{
				g.setColor(new Color(new Random(p.prev * 100).nextInt(255*255*255)));
				if(p.prev != -1){
					p = nodes.get(p.prev);
				}
			}while(p.prev != -1);
			
			if (n.prev != -1) {
				g.fillRect((int)(n.x * scale), (int)(n.y * scale), 4, 4);
				g.drawLine((int)(n.x * scale), (int)(n.y * scale),(int)(nodes.get(n.prev).x * scale), (int)(nodes.get(n.prev).y * scale));
			}else{
				g.fillOval((int)(n.x * scale), (int)(n.y * scale), 7, 7);
			}
		}
		
		if (prevNode != -1 && System.currentTimeMillis() % 500 > 250) {
			g.fillRect((int)(nodes.get(prevNode).x * scale), (int)(nodes.get(prevNode).y * scale), 8, 8);
		}
		
		g.setColor(Color.RED);
		for (FileCustomerNode fcn : customerNodes) {
			g.fillOval((int)(fcn.px * scale), (int)(fcn.pz * scale), 15, 15);
		}
	}
}
