import java.util.*;
import java.text.SimpleDateFormat;

public class Debug{
	static int debuglevel = 0;
	static long l1 = 0;
	
	public static void setDebuglevel(int d){
		debuglevel = d;
	}
	
	public static void log(String n, int d){
		if(debuglevel >= d){
			Date datum = new Date();
			SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy kk:mm:ss");
			System.out.println(format.format(datum) + " " + n);
		}		
	}
	
	public static void log(String n){
		log(n, 0);
	}
	
	public static void start(int d){
		if(debuglevel >= d){
			l1 = System.currentTimeMillis();
		}
	}
	
	public static void logZeit(int d){
		if(debuglevel >= d){
			log("Dauer: " + (System.currentTimeMillis() - l1), d);
		}
	}
	
	public static void logZeit(){
		logZeit(0);
	}
}
