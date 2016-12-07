package updater;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerUpdater {
	private ServerSocket getVersionListener;
	private ServerSocket getDataListener;
	private volatile int latestVersion = -1;
	public ServerUpdater() throws IOException {
		getVersionListener = new ServerSocket(UpdateData.PORT_GET_VERSION);
		getDataListener = new ServerSocket(UpdateData.PORT_GET_DATA);
	}
	
	public void start() throws IOException {
		new Thread(){
			@Override
			public void run() {
				while(true){
					try{
						Thread.sleep(5000);
						updateLatestVersion();
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			}
		}.start();
		
		new Thread(){
			@Override
			public void run() {
				try {
					getVersionServer();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}.start();
		
		getDataServer();
		
	}
	
	protected void updateLatestVersion() {
		File folder = new File(".");
		int version = -1;
		for (final File fileEntry : folder.listFiles()) {
	        if (!fileEntry.isDirectory()) {
	        	String name = fileEntry.getName();
	        	if (name.startsWith("DATA_") && name.endsWith(".jar")) {
	        		 version = Math.max(version, Integer.parseInt(name.substring(5, 9)));
	        	}
	        }
	    }
		System.out.println("Latest version: " + version);
		
	}

	private void getVersionServer() throws IOException{
		try {
            while (true) {
                Socket socket = getVersionListener.accept();
                try {
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    out.println(latestVersion);
                } finally {
                    socket.close();
                }
            }
        } finally {
        	getVersionListener.close();
        }
	}
	
	private void getDataServer() throws IOException{
		try {
            while (true) {
                Socket socket = getVersionListener.accept();
                try {
                	String padded = ""+latestVersion;
                	if (latestVersion < 10) {
                		padded = "0" + padded; 
                	}
                	if (latestVersion < 100) {
                		padded = "0" + padded; 
                	}
                	InputStream is = new FileInputStream("DATA_" + padded + ".jar");
                	OutputStream os = socket.getOutputStream();
                	int count;
                	byte[] fileByte = new byte[100 * 1024 * 1024]; // TODO om den är större än 100MB problem
                	while((count =is.read(fileByte))>=0 ){
                		os.write(fileByte, 0, count);
                      }
                	os.flush();
                	os.close();
                   //PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    //out.println(x);
                } finally {
                    socket.close();
                }
            }
        } finally {
        	getVersionListener.close();
        }
	}
	
	public static void main(String[] args){
		try {
			new ServerUpdater().start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
