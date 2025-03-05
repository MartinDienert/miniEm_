import java.text.*;
import org.json.*;

abstract class Wallbox extends Verbraucher{
    int zykluszeit;
	int leist_start;
	int leist_stop;
    int bezug_stop;
    float bezug_integral = 0;
	int leist_offset;
	int leist_hysterese;
	int[] strom;
	int[] leist;
	Eieruhr startUhr;
	Eieruhr plusUhr;
	Eieruhr minusUhr;
	Wartezeit startWz;
	Wartezeit stopWz;
	Wartezeit stromWz;
	int modus = 1;				// 0 - Aus
								// 1 - PV-Überschuss
								// 2 - PV-Minimal laden
								// 3 - Maximal laden

	int[] status = {-1, 0};		// Status 0: -1 - keine Http Verbindung
								//            0 - Fahrzeug nicht verbunden
								//            1 - Warte auf ladefreigabe
								//            2 - Ladebereit
								//            3 - Lädt
								//            4 - Fehler
								// Status 1:  maximal erlaubter Ladestrom
	
	public Wallbox(JSONObject w, int zy){
		super(w);
        zykluszeit = zy;
		leist_start = w.getInt("startleistung");
		leist_stop = w.getInt("stopleistung");
		bezug_stop = w.getInt("stopbezug");
		leist_offset = w.getInt("offsetleistung");
		leist_hysterese = w.getInt("hystereseleistung");
		strom = Sonstiges.getIntArray(w.getJSONArray("strom"));
		leist = Sonstiges.getIntArray(w.getJSONArray("leistung"));
		startUhr = new Eieruhr(name + "-Startuhr", w.getInt("startzeit"), new WbCbStart());
		plusUhr = new Eieruhr(name + "-Plusuhr", w.getInt("pluszeit"), new WbCbStrom());
		minusUhr = new Eieruhr(name + "-Minusuhr", w.getInt("minuszeit"), new WbCbStrom());
		startWz = new Wartezeit(name + "-Startwartezeit", (w.has("startwartezeit"))? w.getInt("startwartezeit"): 0, (w.has("wartetimeout"))? w.getInt("wartetimeout"): 10);
		stopWz = new Wartezeit(name + "-Stopwartezeit", (w.has("stopwartezeit"))? w.getInt("stopwartezeit"): 0, (w.has("wartetimeout"))? w.getInt("wartetimeout"): 10);
		stromWz = new Wartezeit(name + "-Stromwartezeit", (w.has("stromwartezeit"))? w.getInt("stromwartezeit"): 0, (w.has("wartetimeout"))? w.getInt("wartetimeout"): 10);
	}
	
	void alleUhrenaus(){
		startUhr.stop();
		plusUhr.stop();
		minusUhr.stop();
	}
	
	void wb_ein(){
		if(status[0] == 1){
			startWb();
			Log.log(name + " - Laden gestartet.", 2);
			startWz.start(2, -1);
		}
	}
	
	void wb_aus(){
		if(status[0] == 2 || status[0] == 3){				        // laden stoppen
			stopWb();
			Log.log(name + " - Laden gestopt.", 2);
			stopWz.start(1, -1);
		}
	}
	
	void wb_strom(int i){
		setStrom(strom[i]);
		Log.log(name + " - neuer Strom eingestellt : " + strom[i] + " mA, (" + leist[i] + " W)", 2);
		stromWz.start(-1, strom[i]);
	}
	
	public int setLeistung(int p){
		modus = Sonstiges.eintrag_lesen(new String[]{name + "_modus"})[0];
		status = getStatus();								        // Status der Box abrufen
		int iBox = Sonstiges.find(strom, status[1]);		        // Index nach Strom der Box
		int leistung = 0;
        if(status[0] > 1){
            leistung = getLeistung();								// Leistung abrufen
            if(leistung < 0)
                leistung = leist[iBox];
        }
		Log.log(name + " - Status: " + status[0] + ", Strom: " + status[1] + " mA, Leistung: " + leistung + " W", 5);
        Log.addMW(leistung, 1);
		Sonstiges.eintrag_schreiben(name + "_modus", modus);
		Sonstiges.eintrag_schreiben(new String[]{name + "_status", name + "_strom"}, status);
		Sonstiges.eintrag_schreiben(name + "_leistung", leistung);
		if(status[0] < 0 || status[0] == 0 || status[0] == 4)	    // keine Verbindung, kein Auto angeschlossen oder Fehler
			return 0;
		if(startWz.laeuft(status) | stopWz.laeuft(status) | stromWz.laeuft(status)){		// während eine Wartezeit läuft nichts tun
			return -1;
		}
		if(modus < 1){										        // Laden aus
			alleUhrenaus();
			wb_aus();
			return 0;
		}
		if(modus == 3){										        // Laden Maximalstrom
            alleUhrenaus();
            if(iBox != strom.length - 1)
                wb_strom(strom.length - 1);
            if(status[0] == 1){                                     // laden starten
                wb_ein();
                return leist[leist.length - 1];
            }
            return 0;
		}
		if(status[0] == 1){									        // laden starten
			if(modus == 2){
                if(iBox != 0)
                    wb_strom(0);
				wb_ein();
                return leist[0];
			}else if(-p >= leist_start){
				startUhr.run();
                return leist[0];
			}
			startUhr.stop();
			return 0;
		}
		if(modus == 1 && (status[0] == 2 || status[0] == 3)){	    // laden stoppen
            if(iBox == 0){
                if(p < 0){
                    bezug_integral = 0;
                }else{
                    bezug_integral += (float)p / 3600 * zykluszeit / 1000;
                    Log.log(name + " - Bezug Integral: " + new DecimalFormat("#.#").format(bezug_integral) + " Wh", 2);
                    if(bezug_integral > bezug_stop){
                        wb_aus();
                        return 0;
                    }
                }
            }
		}
		Log.log(name + " - Leistung ges.: " + (-p + leistung) + " W", 5);
		int p1 = leistung - p - leist_offset;
		if(-p > leist_offset + leist_hysterese)
			p1 -= leist_hysterese;
		int iNeu = Sonstiges.find(leist, p1);				        // Index nach akt. Leistung 
		Log.log(name + " - Index: " + iBox + ", Index Neu: " + iNeu, 2);
		if(iBox == iNeu){									        // keine Leistungsänderung -> nichts tun
			plusUhr.stop();
			minusUhr.stop();
			return 0;
		}
		if(iBox < iNeu){									        // Leistungserhöhung
			minusUhr.stop();
			plusUhr.run(iNeu);
            return leist[iNeu] - leistung;
		}else if(iBox > iNeu){								        // Leistungsverringerung
			plusUhr.stop();
			minusUhr.run(iNeu);
		}
		return 0;
	}
	
	abstract int[] getStatus();
	
//	abstract int getLeistung();
	
	abstract void startWb();
	
	abstract void stopWb();
	
	abstract void setStrom(int st);
	
	class WbCbStart implements WallboxCallback{
		public void start(int i){
		}
		public void abgelaufen(int i){
            wb_strom(i);
			wb_ein();
		}
	}
	
	class WbCbStrom implements WallboxCallback{
		public void start(int i){
			Log.log(name + " - neue Leistung: " + leist[i] + " W, Strom: " + strom[i] + " mA", 2);
		}
		public void abgelaufen(int i){
			wb_strom(i);
		}
	}
}

