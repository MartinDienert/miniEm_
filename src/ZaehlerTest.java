import org.json.*;
import java.util.Random;

public class ZaehlerTest extends Zaehler{
	int zyklus;
	long zeit;
	int[] leistung;
	int[] zeiten;
	
	public ZaehlerTest(JSONObject z, int zy){
		super(z);
		zyklus = zy;
		zeit = -zy;
		JSONObject d = z.getJSONObject("daten");
		leistung = Sonstiges.getIntArray(d.getJSONArray("leistung"));
		zeiten = Sonstiges.getIntArray(d.getJSONArray("zeiten"));
	}
	 
	public int getLeistung(){
		zeit += zyklus;
		int ar = (int)(zeit / 1000);
		int i = Sonstiges.find(zeiten, ar);
//		float[] daten = new float[2];
		return leistung[i];
	}
	
	public float getSpannung(){
		return 225.0F + (float)new Random().nextInt(100) / 10;
	}
}

