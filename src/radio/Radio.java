package radio;

import game.Settings;

import java.util.ArrayList;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.JavaSoundAudioDevice;
import javazoom.jl.player.Player;
import GLEngine.Matrix4f;
import GLEngine.SpriteBatch;
import GLEngine.Texture;
import GLEngine.Vector3f;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

public class Radio {
	public enum RadioButton{
		ON_OFF, VOLUME_DOWN, VOLUME_UP, STATION_PREVIOUS, STATION_NEXT;
	}
	private class Station{
		String url;
		String name;
		int iconID;
		public Station(String n, String u, int texID) {
			url = u;
			name = n;
			iconID = texID;
		}
	}
	private boolean on = true;
	private double volume = 0.1f;
	private double volumeMax = 0.1f;
	private double volumeStep = 0.01f;
	private volatile boolean volChanged = false;
	private volatile int station;
	private ArrayList<Station> stations = new ArrayList<Station>();
	private volatile boolean globalClose = false;
	
	public Radio(double volume, boolean on){
		this.volume = Math.max(0, Math.min(volume, volumeMax));
		this.on = on;
		stations.add(new Station("HARDBASE", "http://listen.hardbase.fm/tunein-mp3-pls", Texture.RADIO_HARDBASE));
		stations.add(new Station("81hiphop ", "http://69.175.13.133:8030/", Texture.RADIO_HIPHOP));
		stations.add(new Station("DEATH.fm", "http://hi.death.fm", Texture.RADIO_DEATH));
		stations.add(new Station("HARDCORERADIO.nl", "http://shoutcast1.hardcoreradio.nl:80/", Texture.RADIO_HARDCORE));
		stations.add(new Station("Jazz ", "http://199.180.72.2:8015/", Texture.RADIO_JAZZ)); //http://us1.internet-radio.com:11094/
		stations.add(new Station("Pagan Metal Radio", "http://62.210.209.179:8030/stream", Texture.RADIO_PAGAN));
		stations.add(new Station("OFF", "no radio", Texture.RADIO_PAGAN));
		/*stations.add(new Station("SR P1", "http://http-live.sr.se/p1-mp3-192"));
		stations.add(new Station("SR P2", "http://http-live.sr.se/p2-mp3-192"));
		stations.add(new Station("SR P3", "http://http-live.sr.se/p3-mp3-192"));
		stations.add(new Station("SR P4", "http://sverigesradio.se/topsy/direkt/218-hi-mp3"));*/
		loop();
	}
	
	public synchronized void pressButton(RadioButton button) {
		if (button == RadioButton.ON_OFF) {
			//on = !on;
			// kör med byte till off kanalen istället
		}
		
		if (on) {
			if (button == RadioButton.STATION_NEXT) {
				station++;
				if (station == stations.size()) {
					station = 0;
				}
			} else if (button == RadioButton.STATION_PREVIOUS) {
				station--;
				if (station == -1) {
					station = stations.size() - 1;
				}
			} else if (button  == RadioButton.VOLUME_DOWN) {
				volume -= volumeStep;
				if (volume < 0) {
					volume = 0;
				}
				volChanged = true;
			} else if (button  == RadioButton.VOLUME_UP) {
				volume += volumeStep;
				if (volume > volumeMax) {
					volume = volumeMax;
				}
				volChanged = true;
			}
		}
	}
	public synchronized boolean isOn(){
		return on;
	}
	public synchronized String getStationName(){
		if (on) {
			return stations.get(station).name;
		} else {
			return "";
		}
	}

	private void loop(){
		new Thread(){
			int prevStation;
			boolean prevOn;
			Player player;
			@Override
			public void run() {
				while(!globalClose){
					int currStation = station;
					if (currStation < 0 || currStation >= stations.size()) {
						continue; // Om man har otur med timingen kan man få en itermediate station, spinn tills den är ok
					}
					boolean currOn = on;
					if (currOn) {
						if (currStation != prevStation || !prevOn || volChanged) { // Bytt station, eller slagit på
							if (player != null) {
								player.close();
							}
							try{
								if (stations.get(station).name.equals("OFF")) {
									continue;
								}
								OkHttpClient client = new OkHttpClient();
								Request request = new Request.Builder().url(stations.get(station).url).build();
								Response response = client.newCall(request).execute();
								player = new Player(response.body().byteStream());
								new Thread(){
									@Override
									public void run() {
										try {
											player.play(1); // Så att source skapas i audio
											((JavaSoundAudioDevice) player.audio).setLineGain((float) (20 * Math.log10(volume)));
											volChanged = false;
											player.play();
										} catch (JavaLayerException e) {
											e.printStackTrace();
										} 
									}
								}.start();
							}catch(Exception e){
								e.printStackTrace();
							}
						}
					}
					if (!on && prevOn && player != null) { // Slog av
						player.close();
					}
					try {
						Thread.sleep(20);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					prevStation = currStation;
					prevOn = currOn;
				}
				if (player != null) {
					player.close();
				}
			}
		}.start();
	}
	public void close() {
		globalClose = true;
	}
	/*public static void main(String[] args) throws InterruptedException{
		Radio r = new Radio(0.1f, true);
	}*/

	public void render2D(SpriteBatch spriteBatch, Settings settings) {
		if (!on) {
			return;
		}
		//Matrix4f m = Matrix4f.translate(new Vector3f(settings.width / 2, 30, 0)).multiply(Matrix4f.scale(50));
		//spriteBatch.render(stations.get(station).iconID, m);
		
		//m = Matrix4f.translate(new Vector3f(settings.width / 2 - 80, 40, 0)).multiply(Matrix4f.scale(30));
		//spriteBatch.render(stations.get(station - 1 == -1 ? stations.size() - 1 : station - 1).iconID, m);
		//m = Matrix4f.translate(new Vector3f(settings.width / 2 + 100, 40, 0)).multiply(Matrix4f.scale(30));
		//spriteBatch.render(stations.get(station + 1 == stations.size() ? 0 : station + 1).iconID, m);
		
		spriteBatch.renderString("Radio: " + stations.get(station).name, new Vector3f(settings.width/2 - 170, 50, 0), 0.3f);
	}

	
}
