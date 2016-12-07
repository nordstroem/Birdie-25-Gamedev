package sound;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

import GLEngine.Vector3f;

public class Sound {

	public static final SoundFile GLASS_BREAK = new SoundFile("crt_break.wav"); 
	public static final SoundFile FULL = new SoundFile("engine_full.wav", 0.05); 
	public static final SoundFile IDLE = new SoundFile("engine_idle.wav", 0.03); 
	public static final SoundFile START = new SoundFile("engine_start.wav", 0.05); 
	public static final SoundFile CRASH = new SoundFile("car_crash.wav", 0.75); 
	public static final SoundFile SCORE = new SoundFile("score.wav", 1); 
	public static final SoundFile ERROR = new SoundFile("error.wav", 1); 
	public static final SoundFile PICK_UP = new SoundFile("pick_up.wav", 1); 
	public static final SoundFile OBJECT_CRASH = new SoundFile("object_crash.wav", 1); 
	public static final SoundFile PHONE = new SoundFile("phone.wav", 1); 
	
	private SoundFile currentMusic;
	private double soundVolume;
	private double musicVolume;
	private Clip musicClip;

	public Sound(double soundVolume, double musicVolume) { // 0 - 1
		this.soundVolume = soundVolume;
		this.musicVolume = musicVolume;

	}
	public Clip play(SoundFile sound) {
		return play(sound, soundVolume);
	}
	
	public void play3D(SoundFile sound, Vector3f ear, Vector3f soundPos){
		double maxDistance = 100;
		double dx = ear.x - soundPos.x;
		double dy = ear.y - soundPos.y;
		double dz = ear.z - soundPos.z;
		double dis = Math.sqrt(dx*dx + dy*dy + dz*dz);
		if (dis < maxDistance) {
			play(sound, soundVolume * (1 - (dis/maxDistance)));
		}
	}
	
	public Clip play(SoundFile sound, double volume) {
		try {
			Clip clip = (Clip) AudioSystem.getLine(sound.info);
			clip.open(sound.af, sound.audio, 0, sound.size);
			FloatControl volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
			volumeControl.setValue((float) (20 * Math.log10(volume * sound.volume * soundVolume)));
			clip.start();
			return clip;
		} catch (Exception e) {
			//JOptionPane.showMessageDialog(null, "play sound " + e.getMessage());
			e.printStackTrace();
		}
		return null;

	}

	public void setMusic(SoundFile music) {
		
		if (currentMusic == music) {
			return;
		}
		if (musicClip != null) {
			musicClip.stop();
		}

		currentMusic = music;
		try {

			musicClip = (Clip) AudioSystem.getLine(music.info);
			musicClip.open(music.af, music.audio, 0, music.size);
			musicClip.loop(Clip.LOOP_CONTINUOUSLY);
			FloatControl volume = (FloatControl) musicClip.getControl(FloatControl.Type.MASTER_GAIN);
			volume.setValue((float) (20 * Math.log10(musicVolume * music.volume)));
			musicClip.start();
		} catch (Exception e) {
			//JOptionPane.showMessageDialog(null, "set music " + e.getMessage());
			e.printStackTrace();
		}
		
	}

	public void stopMusic() {
		currentMusic = null;
		musicClip.stop();
	}

}