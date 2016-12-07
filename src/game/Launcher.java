package game;

import java.awt.Checkbox;
import java.awt.Color;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Launcher {
	public Launcher() {
		
		//if 32 or 64bit
		System.setProperty("org.lwjgl.util.Debug", "true");
		
		/*try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}*/
		
		JFrame frame = new JFrame();
		frame.setTitle("T.A.X.I.");
		frame.setSize(345, 200); //310 200
		frame.setResizable(true);
		frame.setLocationRelativeTo(null);
		//frame.setLayout(new GridBagLayout());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel panel = new JPanel();
		frame.add(panel);
		
		Checkbox full = new Checkbox("Fullscreen", null, Game.DEBUG_WINDOW_SIZE ? false : true); 
		
		JList<Integer> aa = new JList<Integer>();
		Vector<Integer> aaList = new Vector<Integer>();
		aaList.add(1);
		aaList.add(2);
		aaList.add(4);
		aaList.add(8);
		aa.setListData(aaList);
		aa.setSelectedIndex(1);
		
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		JTextField width = new JTextField();
		width.setText(""+(Game.DEBUG_WINDOW_SIZE ? 800 : gd.getDisplayMode().getWidth()));
		
		JTextField height = new JTextField();
		height.setText(""+(Game.DEBUG_WINDOW_SIZE ? 800 : gd.getDisplayMode().getHeight()));
		
		
		JSlider volume = new JSlider(JSlider.HORIZONTAL, 0, 10, 5);
		volume.setMajorTickSpacing(2);
		volume.setMinorTickSpacing(1);
		volume.setPaintTicks(false);
		volume.setPaintLabels(false);
		
		JSlider radioVolume = new JSlider(JSlider.HORIZONTAL, 0, 10, 5);
		radioVolume.setMajorTickSpacing(2);
		radioVolume.setMinorTickSpacing(1);
		radioVolume.setPaintTicks(false);
		radioVolume.setPaintLabels(false);
		
		JTextField name = new JTextField();
		name.setColumns(10);
		name.setText(System.getProperty("user.name"));
		
		JButton launch = new JButton("Launch");
		launch.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try{
					Integer.parseInt(width.getText());
				}catch(Exception e){
					width.setBackground(Color.RED);
					return;
				}
				try{
					Integer.parseInt(height.getText());
				}catch(Exception e){
					height.setBackground(Color.RED);
					return;
				}
				frame.dispose();
				
				Settings s = new Settings();
				s.full = full.getState();
				s.aa = aa.getSelectedValue();
				s.width = Integer.parseInt(width.getText());
				s.height = Integer.parseInt(height.getText());
				s.volume = volume.getValue() / 10.0;
				s.radioVolume = radioVolume.getValue() / 100.0;
				s.name = name.getText().replace("ö", "o").replace("ä", "a").replace("å","a");
				new Game(s);
			}
		});
		
		panel.add(full);
		panel.add(new JLabel("Resolution:"));
		panel.add(width);
		panel.add(new JLabel("x"));
		panel.add(height);
		panel.add(new JLabel("Anti Aliasing:"));
		panel.add(aa);
		panel.add(new JLabel("Sound Volume:"));
		panel.add(volume);
		panel.add(new JLabel("Radio Volume:"));
		panel.add(radioVolume);
		panel.add(new JLabel("Name:"));
		panel.add(name);
		panel.add(launch);
		
		
		frame.setVisible(true);
	}
	public static void main(String[] args) {
		new Launcher();
	}
}
