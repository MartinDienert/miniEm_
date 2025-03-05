import java.util.*;
import java.text.*;
import java.text.SimpleDateFormat;
import org.json.*;

class Hauptschleife extends TimerTask{
	long l = 0;
	int zykluszeit;
	String[] berechnungenLeistung;
	Zaehler zErzeugung;
	Zaehler zNetz;
	Zaehler[] zaehler;
	Wallbox wBox;
	Verbraucher[] verbraucher;
	static int wBLeistung = 0;	// TEST
	
	public Hauptschleife(JSONObject config){
		zykluszeit = config.getInt("zykluszeit");
		if(config.has("berechnungen")){
			JSONObject berechnungen = config.getJSONObject("berechnungen");
			if(berechnungen.has("leistung")){
				berechnungenLeistung = berechnungen.getString("leistung").split(" ");
			}
		}
		if(config.has("zaehlerErzeugung")){
			if(config.getJSONObject("zaehlerErzeugung").getString("type").equals("test"))
				zErzeugung = new ZaehlerTest(config.getJSONObject("zaehlerErzeugung"), config.getInt("zykluszeit"));
			else if(config.getJSONObject("zaehlerErzeugung").getString("type").equals("modbustcp"))
				zErzeugung = new ZaehlerModbusTcp(config.getJSONObject("zaehlerErzeugung"));
			else
				zErzeugung = null;										// weitere Zaehlertypen
		}else
			zErzeugung = null;
		if(config.has("zaehlerNetz")){
			if(config.getJSONObject("zaehlerNetz").getString("type").equals("test"))
				zNetz = new ZaehlerTest(config.getJSONObject("zaehlerNetz"), zykluszeit);
			else if(config.getJSONObject("zaehlerNetz").getString("type").equals("modbustcp"))
				zNetz = new ZaehlerModbusTcp(config.getJSONObject("zaehlerNetz"));
			else
				zNetz = null;											// weitere Zaehlertypen
		}else
			zNetz = null;
		int i = 0;
		while(config.has("zaehler_" + (i + 1)))
			i++;
		zaehler = new Zaehler[i];	
		for(i = 0; i < zaehler.length; i++){
			if(config.getJSONObject("zaehler_" + (i + 1)).getString("type").equals("modbustcp"))
				zaehler[i] = new ZaehlerModbusTcp(config.getJSONObject("zaehler_" + (i + 1)));
			else if(config.getJSONObject("zaehler_" + (i + 1)).getString("type").equals("http"))
				zaehler[i] = new ZaehlerHttp(config.getJSONObject("zaehler_" + (i + 1)));
			else
				zaehler[i] = null;
		}
		if(config.has("wallbox")){
			if(config.getJSONObject("wallbox").getString("type").equals("test"))
				wBox = new WallboxTest(config.getJSONObject("wallbox"), zykluszeit);
			else if(config.getJSONObject("wallbox").getString("type").equals("http"))
				wBox = new WallboxHttp(config.getJSONObject("wallbox"), zykluszeit);
			else
				wBox = null;											// weitere Wallboxtypen
		}else
			wBox = null;
		i = 1;
		while(config.has("verbraucher_" + i))
			i++;
		i--;
		verbraucher = new Verbraucher[i];	
		for(i = 0; i < verbraucher.length; i++){
			if(config.getJSONObject("verbraucher_" + (i + 1)).getString("type").equals("test"))
				verbraucher[i] = new VerbraucherStufenTest(config.getJSONObject("verbraucher_" + (i + 1)), zykluszeit);
			else if(config.getJSONObject("verbraucher_" + (i + 1)).getString("type").equals("http"))
				verbraucher[i] = new VerbraucherStufenHttp(config.getJSONObject("verbraucher_" + (i + 1)), zykluszeit);
			else
				verbraucher[i] = null;
		}
	}
	
	public void run(){
		long l2 = System.currentTimeMillis();
		Log.log("Start Hauptschleife, " + (l2 -l) + " ms", 5);
		l = l2;
		Debug.start(1);
		Sonstiges.eingabe_lesen();
		Sonstiges.ausgabe_lesen();
		int e;
		if((e = Sonstiges.eintrag_lesen("loglevel")) > -1)	Log.setLoglevel(e);
		if((e = Sonstiges.eintrag_lesen("logDatei")) > -1)	Log.setLoglevelDatei(e);
		if((e = Sonstiges.eintrag_lesen("logMessw")) > -1)	Log.setLoglevelMw(e);
		if(zErzeugung != null){
			int p = zErzeugung.getLeistung();
			if(zErzeugung.getFehler() == 0){
				Log.addMW(p, 1);
				Log.log("Erzeugung: " + (p) + " W", 2);
				Sonstiges.eintrag_schreiben("Erzeugung", p);
			}
		}
		if(zNetz != null){
			int p = zNetz.getLeistung();
			float u = zNetz.getSpannung();
			if(zNetz.getFehler() == 0){
				Log.addMW(p, 1);
				Log.log("Spannung: " + new DecimalFormat("#.#").format(u) + " V, Leistung: " + (p + wBLeistung) + " W", 2);
				Sonstiges.eintrag_schreiben("Spannung", (int)(u * 10));
				Sonstiges.eintrag_schreiben("Einspeisung", p);		
				p = berechneleistung(berechnungenLeistung, p);
				if(wBox != null){
					p += wBox.setLeistung(p + wBLeistung);
					Log.log("noch verfügbare Leistung: " + p + " W", 5);
				}
				for(int i = 0; i < verbraucher.length; i++)
					if(verbraucher[i] != null){
						p += verbraucher[i].setLeistung(p + wBLeistung);
						Log.log("noch verfügbare Leistung: " + p + " W", 5);
					}
			}
		}
		Log.logDatei();
		Log.logMW();
		Sonstiges.eintrag_schreiben("Zeit", new SimpleDateFormat("kk:mm:ss").format(new Date()));
		Sonstiges.ausgabe_schreiben();
		Debug.logZeit(1);
	}
	
	int berechneleistung(String[] b, int p){
		if(b != null){
			int i = Sonstiges.istInteger(b[2]);
//			if(i == -1) ...
			Berechnen ber = (b[1].equals("zaehler"))? zaehler[i - 1]: (b[1].equals("verbraucher"))? verbraucher[i - 1]: null;
			if(ber != null){
				if(b[0].equals("-"))
					return p - ber.getLeistung();
				else if(b[0].equals("+"))
					return p + ber.getLeistung();
			}
		}
		return p;
	}
}
