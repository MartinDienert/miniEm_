import org.json.*;

abstract class Zaehler implements Berechnen{
	String name;
	int fehler = 0;
	
	public Zaehler(JSONObject z){
		name = z.getString("name");
	}
	
//	abstract int getLeistung();
	
	abstract float getSpannung();
	
	public String getName(){
		return name;
	}

	public int getFehler(){
		return fehler;
	}
}

