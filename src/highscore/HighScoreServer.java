package highscore;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JFrame;


public class HighScoreServer {
	

	private ServerSocket getListListener;
	private ServerSocket postNewListener;
	private Scores scores = new Scores();
	
	public HighScoreServer() throws IOException {
		JFrame frame = new JFrame("HighScore Server");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(100, 100);
		JButton save = new JButton("Save & Close");
		save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				Entry[] entries = scores.getEntries();
				try {
					PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter("highscore.txt", false)));
					for (int i = 0; i < entries.length; i++) {
						pw.println(entries[i].name);
						pw.println(entries[i].score);
					}
					pw.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				System.exit(0);
			}
		});
		frame.add(save);
		frame.setVisible(true);
		readEntries();
		getListListener = new ServerSocket(HighScoreData.PORT_GET_LIST);
		postNewListener = new ServerSocket(HighScoreData.PORT_POST_NEW);
	}

	private void readEntries() {
		Scanner s = new Scanner(getClass().getResourceAsStream("highscore.txt"));
		while(s.hasNextLine()){
			scores.add(new Entry(s.nextLine(), Long.parseLong(s.nextLine())));
		}
		s.close();
	}

	public void start() throws IOException {
		new Thread(){
			@Override
			public void run() {
				try {
					getListServer();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}.start();
		
		postNewServer();
		
	}
	
	private void postNewServer() throws IOException{
		try {
            while (true) {
                Socket socket = postNewListener.accept();
                try {
                	BufferedReader in = new BufferedReader(
                            new InputStreamReader(socket.getInputStream()));
                	String name = in.readLine();
                	String score = in.readLine();
                	scores.add(new Entry(name, Long.parseLong(score)));
                } finally {
                    socket.close();
                }
            }
        }
        finally {
        	postNewListener.close();
        }
	}
	private void getListServer() throws IOException{
		try {
            while (true) {
                Socket socket = getListListener.accept();
                try {
                    PrintWriter out =
                        new PrintWriter(socket.getOutputStream(), true);
                    Entry[] entries = scores.getEntries(); 
                    for (Entry e: entries) {
                    	out.println(e.name);
                    	out.println(e.score);
                    }
                } finally {
                    socket.close();
                }
            }
        }
        finally {
            getListListener.close();
        }
	}
    public static void main(String[] args){
        try {
			new HighScoreServer().start();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}
// Thread safe acess to list of scores
class Scores{
	private Entry[] scores = new Entry[HighScoreData.LIST_SIZE];
	
	synchronized void add(Entry e){
		for (int i = 0; i < scores.length; i++) {
			if (scores[i] == null || e.score > scores[i].score) {
				for (int j = scores.length - 2; j >= i; j--) {
					scores[j + 1] = scores[j];
				}
				scores[i] = e;
				break;
			}
		}
	}
	
	// Return a new copy of scores to avoid simultainous access to same array.
	synchronized Entry[] getEntries(){
		Entry[] copy = new Entry[scores.length];
		for (int i = 0; i < copy.length; i++) {
			copy[i] = scores[i].clone();
		}
		return copy;
	}
	
}
