public class Eieruhr{
	String name;
	long timer;
	int dauer;
	WallboxCallback wbCb = null;
	
	public Eieruhr(String n, int d, WallboxCallback w){
		name = n;
		timer = -1;
		dauer = d;
		wbCb = w;
	}

	public Eieruhr(String n, int d){
		this(n, d, null);
	}
	
	public void restart(){
		timer = System.currentTimeMillis();
		Log.log(name + " gestartet.", 5);
	}
	
	public boolean start(){
		if(timer == -1){
			restart();
			return true;
		}
		return false;
	}
	
	public void stop(){
		if(timer != -1){
			timer = -1;
			Log.log(name + " gestopt.", 5);
		}
	}
	
	public boolean laeuft(){
		int z = getZeit();
		boolean b = z < dauer;
		if(b)
			Log.log(name + " läuft: " + z + "s", 2);
		else
			stop();
		return b;
	}
	
	public boolean abgelaufen(){
		boolean b = getZeit() >= dauer;
		if(b){
			Log.log(name + " abgelaufen.", 5);
			stop();
		}
		return b;
	}
	
	public int getZeit(){
		return Math.round((float)(System.currentTimeMillis() - timer) / 1000);
	}
	
	public void logZeit(){
		Log.log(name + " läuft: " + getZeit() + "s", 2);
	}
	
	public boolean run(int i){
		if(wbCb == null)
			return false;								// kein Callback Objekt
		if(start()){									// Eieruhr gestartet
			wbCb.start(i);
		}
		logZeit();										// Eieruhr läuft noch
		if(abgelaufen())								// Eieruhr abgelaufen
			wbCb.abgelaufen(i);
		return true;
	}
	
	public boolean run(){
		return run(0);
	}
}
