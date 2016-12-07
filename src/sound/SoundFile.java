package sound;

import java.net.URL;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.swing.JOptionPane;

class SoundFile {
	AudioFormat af;
	DataLine.Info info;
	byte[] audio;
	int size;
	double volume = 1;
	static boolean soundErrorShown = false;

	public SoundFile(String path) {
		this(path, 1);
	}
	public SoundFile(String path, double volume) {
		this.volume = volume;
		try {
			URL u = Sound.class.getResource(path);
			AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(u);
			
			af = audioInputStream.getFormat();
			size = (int) (af.getFrameSize() * audioInputStream.getFrameLength());
			audio = new byte[size];
			info = new DataLine.Info(Clip.class, af, size);
			//audioInputStream.read(audio, 0, size);
			int read = 0;
			while(true){
				int newRead = audioInputStream.read(audio, read, size);	
				if(newRead == -1){
					break;
				}
				read+= newRead;
				
			}
		} catch (Exception e) {
			if (!soundErrorShown) {
				soundErrorShown = true;
				//JOptionPane.showMessageDialog(null, "Error loading sound");
			}
			e.printStackTrace();
		}
	}
}