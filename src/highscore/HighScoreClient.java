package highscore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Iterator;
public class HighScoreClient {
	
	public HighScoreClient() {

	}
	
	public Entry[] getList() throws UnknownHostException, IOException {
		Socket s = new Socket(HighScoreData.IP, HighScoreData.PORT_GET_LIST);
        BufferedReader input =
            new BufferedReader(new InputStreamReader(s.getInputStream()));
        Entry[] scores = new Entry[HighScoreData.LIST_SIZE];
        for (int i = 0; i < HighScoreData.LIST_SIZE; i++) {
        	//System.out.println("" + (i + 1) + ": " + input.readLine() + "    "  + input.readLine());
        	scores[i] = new Entry(input.readLine(), Long.parseLong(input.readLine()));
        }
        s.close();
		return scores;
	}
	
	public void postNew(String name, long score) throws UnknownHostException, IOException {
		Socket s = new Socket(HighScoreData.IP, HighScoreData.PORT_POST_NEW);
		PrintWriter out = new PrintWriter(s.getOutputStream(), true);
		out.println(name);
		out.println(score);
		s.close();
	}
	
    public static void main(String[] args) throws IOException {
    	HighScoreClient hsc = new HighScoreClient();
    	hsc.postNew("name", 1337);
    	hsc.postNew("apa", 1339);
    	hsc.postNew("apa1", 1338);
    	hsc.postNew("apa2", 1340);
    	hsc.postNew("apa0", 1336);
    	Entry[] scores = hsc.getList();
    	for (Entry entry : scores) {
    		System.out.println(entry.name +  " " + entry.score);
		}
    }
}