import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

public class VerbraucherStufenTest extends VerbraucherStufen{
	int[] status = {0, 1};
	
	public VerbraucherStufenTest(JSONObject z, int zy){
		super(z, zy);
	}
	 
	public int[] getStatus(){
		return status;
	}
	
	public int getLeistung(){
		return -1;
	}
	
	public void setEin(){
		EinAus ea = new EinAus(1);
		Timer timer = new Timer();
		timer.schedule(ea, 1000);
	}
	
	public void setAus(){
		EinAus ea = new EinAus(0);
		Timer timer = new Timer();
		timer.schedule(ea, 1000);
	}
	
	public void setStufe(int st){
		Strom str = new Strom(st);
		Timer timer = new Timer();
		timer.schedule(str, 300);
	}
	
	class EinAus extends TimerTask{
		int st;
		
		public EinAus(int s){
			st = s;
		}
		
		public void run(){
			status[0] = st;
			if(st == 0)
				Hauptschleife.wBLeistung = 0;
			else if(st == 1)
				Hauptschleife.wBLeistung = leist[Sonstiges.find(stufe, status[1])];
		}
	}
		
	class Strom extends TimerTask{
		int st;
		
		public Strom(int s){
			st = s;
		}
		
		public void run(){
			status[1] = st;
			Hauptschleife.wBLeistung = leist[Sonstiges.find(stufe, st)];
		}
	}
}

