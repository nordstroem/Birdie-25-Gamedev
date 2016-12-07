package updater;
import java.awt.Dimension;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.JProgressBar;


public class Updater extends JFrame{
    String updateurl;
    JProgressBar progress;
    public static void main(String[] args){
    	int localVersion = localVersion();
    	int latestVersion = latestVersion();
    	System.out.println("local: " + localVersion);
    	System.out.println("latest: " + latestVersion);
    	/*
        Updater up = new Updater("http://my.server.com/~me/stuff/foo.jar");
        up.downloadLatestVersion();
        try {
            Process foo = Runtime.getRuntime().exec("java -jar foo.jar");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.exit(0);*/

    }
    
    public static int localVersion(){
        /*BufferedReader is;
        try {
            is = new BufferedReader(
                    new InputStreamReader(ClassLoader.getSystemResource("revision.txt").openStream()));
            int rev = Integer.valueOf(is.readLine());
            return rev;
        } catch(NullPointerException e){
        }catch (Exception e) {
            e.printStackTrace();
        }
        return 1<<31-1;*/
    	return -1;
    }
    
    public static int latestVersion(){
        URL url;
        try {
            url = new URL("http://my.server.com/~me/stuff/revision.txt");
            HttpURLConnection hConnection = (HttpURLConnection) url
                    .openConnection();
            HttpURLConnection.setFollowRedirects(true);
            if (HttpURLConnection.HTTP_OK == hConnection.getResponseCode()) {
                BufferedReader is = new BufferedReader(new
                        InputStreamReader(hConnection.getInputStream()));
                int rev = Integer.valueOf(is.readLine());
                return rev;
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        return -1;
    }
    
    
    public Updater(String url){
        updateurl = url;
        this.setPreferredSize(new Dimension(300, 80));
        this.setSize(new Dimension(300, 80));
        this.setTitle("Updater");
        progress = new JProgressBar(0,100);
        progress.setValue(0);
        progress.setStringPainted(true);
        this.add(progress);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.requestFocus(true);
    }
    void downloadLatestVersion(){
        URL url;
        try {
            url = new URL(updateurl);
            HttpURLConnection hConnection = (HttpURLConnection) url
                    .openConnection();
            HttpURLConnection.setFollowRedirects(true);
            if (HttpURLConnection.HTTP_OK == hConnection.getResponseCode()) {
                InputStream in = hConnection.getInputStream();
                BufferedOutputStream out = new BufferedOutputStream(
                        new FileOutputStream("foo.jar"));
                int filesize = hConnection.getContentLength();
                progress.setMaximum(filesize);
                byte[] buffer = new byte[4096];
                int numRead;
                long numWritten = 0;
                while ((numRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, numRead);
                    numWritten += numRead;
                    System.out.println((double)numWritten/(double)filesize);
                    progress.setValue((int) numWritten);
                }
                if(filesize!=numWritten)
                    System.out.println("Wrote "+numWritten+" bytes, should have been "+filesize);
                else
                    System.out.println("Downloaded successfully.");
                out.close();
                in.close();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    public static void downloadFile(String sourceurl, String dest){
        URL url;
        try {
            url = new URL(sourceurl);
            HttpURLConnection hConnection = (HttpURLConnection) url
                    .openConnection();
            HttpURLConnection.setFollowRedirects(true);
            if (HttpURLConnection.HTTP_OK == hConnection.getResponseCode()) {
                InputStream in = hConnection.getInputStream();
                BufferedOutputStream out = new BufferedOutputStream(
                        new FileOutputStream(dest));
                int filesize = hConnection.getContentLength();
                byte[] buffer = new byte[4096];
                int numRead;
                long numWritten = 0;
                while ((numRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, numRead);
                    numWritten += numRead;
                    System.out.println((double)numWritten/(double)filesize);
                }
                if(filesize!=numWritten)
                    System.out.println("Wrote "+numWritten+" bytes, should have been "+filesize);
                else
                    System.out.println("Downloaded successfully.");
                out.close();
                in.close();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}