package editor;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class EntitiesPanel extends JPanel {
	public EntitiesPanel(Editor editor, FileHandler fileHandler) {
		JList<FileEntity> entityList = new JList<FileEntity>();
		
		JLabel nameLabel = new JLabel("Name: ");
		add(nameLabel);
		JTextField name = new JTextField("", 20);
		add(name);
		
		JLabel objLabel = new JLabel("OBJ File:");
		add(objLabel);
		JTextField obj = new JTextField("", 20){
			@Override
			public void repaint() {
				if (getDocument() != null) {
					setBackground(new File("src/objfiles/" + getText()).exists() ? Color.WHITE : Color.RED);
				}
				super.repaint();
			}
		};
		add(obj);
		
		JLabel texLabel = new JLabel("Texture File:");
		add(texLabel);
		JTextField tex = new JTextField("", 20){
			@Override
			public void repaint() {
				if (getDocument() != null) {
					setBackground(new File("src/textures/" + getText()).exists() ? Color.WHITE : Color.RED);
				}
				super.repaint();
			}
		};
		add(tex);
		
		JLabel colLabel = new JLabel("Collision (mesh, box, <meshFile>):");
		add(colLabel);
		JTextField col = new JTextField("", 20);
		add(col);
		
		JLabel weightLabel = new JLabel("Weight (0 for static):");
		add(weightLabel);
		JTextField weight = new JTextField("", 5);
		add(weight);
		
		JButton newButton = new JButton("New Entity");
		newButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent ae) {
				fileHandler.addNewEmptyEntity();
				Vector<FileEntity> vec = fileHandler.getFileEntitiesCopy();
				entityList.setListData(vec);
				entityList.setSelectedIndex(vec.size() - 1);
			}
			
		});
		add(newButton);
		
		
		entityList.setListData(fileHandler.getFileEntitiesCopy());
		entityList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		entityList.addListSelectionListener(new ListSelectionListener(){
			int prev = -1;
			@Override
			public void valueChanged(ListSelectionEvent lse) {
				int sel = entityList.getSelectedIndex();
				if (prev != -1) {
					FileEntity prevEntity = entityList.getModel().getElementAt(prev);
					prevEntity.name = name.getText();
					prevEntity.obj = obj.getText();
					prevEntity.tex = tex.getText();
					try{
						prevEntity.weight = Double.parseDouble(weight.getText());
					}catch(NumberFormatException e){
						e.printStackTrace();
					}
					prevEntity.collision = col.getText();
				}
				name.setText(entityList.getSelectedValue().name);
				obj.setText(entityList.getSelectedValue().obj);
				tex.setText(entityList.getSelectedValue().tex);
				col.setText(entityList.getSelectedValue().collision);
				weight.setText(""+entityList.getSelectedValue().weight);
				//selectedEntity = entityList.getSelectedValue();
				editor.setSelectedEntity(entityList.getSelectedValue());
				prev = sel;
			}
			
		});
		add(entityList);
	}
}
