package highscore;
public class Entry{
	public String name = "-";
	public long score = 0;
	public Entry(String n, long s) {
		name = n;
		score = s;
	}
	
	public Entry clone(){
		return new Entry(name, score);
	}
}