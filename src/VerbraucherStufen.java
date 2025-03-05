import java.text.*;
import org.json.*;

abstract class VerbraucherStufen extends Verbraucher{
    int zykluszeit;
	int leist_start;
    int bezug_stop;
    float bezug_integral = 0;
	int leist_offset;
	int leist_hysterese;
	int[] stufe;
	int[] leist;
	int modus = 1;				// 0 - Aus
								// 1 - PV-Überschuss
								// 2 - PV-Minimal
								// 3 - Ein

	int[] status = {-1, 0};		// Status 0: -1 - keine Verbindung
								//            0 - Aus
								//            1 - Ein
								//            2 - Fehler
								// Status 1:  Stufe (1, 2 ...)
	
	public VerbraucherStufen(JSONObject w, int zy){
		super(w);
        zykluszeit = zy;
		leist_start = w.getInt("startleistung");
		bezug_stop = w.getInt("stopbezug");
		leist_offset = w.getInt("offsetleistung");
		leist_hysterese = w.getInt("hystereseleistung");
		stufe = Sonstiges.getIntArray(w.getJSONArray("stufe"));
		leist = Sonstiges.getIntArray(w.getJSONArray("leistung"));
	}
	
	void ein(){
		if(status[0] == 0){
			setEin();
			Log.log(name + " - eingeschaltet.", 2);
		}
	}
	
	void aus(){
		if(status[0] == 1){
			setAus();
			Log.log(name + " - ausgeschaltet.", 2);
		}
	}
	
	void stufe(int i){
		setStufe(stufe[i]);
		Log.log(name + " - neue Stufe eingestellt : " + stufe[i] + ", (" + leist[i] + " W)", 2);
	}
	
	public int setLeistung(int p){
		modus = Sonstiges.eintrag_lesen(name + "_modus");
		status = getStatus();										// Status abrufen
		int index = Sonstiges.find(stufe, status[1]);				// Index nach Stufe
		int leistung = 0;
		if(status[0] > 0){
			leistung = getLeistung();								// Leistung abrufen
			if(leistung < 0)
				leistung = leist[index];
		}
		Log.log(name + " - Status: " + status[0] + ", Stufe: " + status[1] + ", Leistung: " + leistung + " W" + ", Modus: " + modus, 5);
		Log.addMW(leistung, 1);
		Sonstiges.eintrag_schreiben(name + "_modus", modus);
		Sonstiges.eintrag_schreiben(new String[]{name  + "_status", name + "_stufe"}, status);
		Sonstiges.eintrag_schreiben(name + "_leistung", leistung);
		if(status[0] < 0)	    							// keine Verbindung oder Fehler
			return 0;
		if(modus < 1){										// aus
			aus();
			return 0;
		}
		if(modus == 3){
			if(status[0] == 0){								// ein
				ein();
				return leist[0];
			}
			if(index != stufe.length - 1){
				stufe(stufe.length - 1);
				return leist[stufe.length - 1];
			}
			return 0;
		}
		if(status[0] == 0){									// ein
			if(modus == 2 || -p >= leist_start){
				if(index != 0)
					stufe(stufe[0]);
				ein();
				return leist[0];
			}
			return 0;
		}
		if(modus == 1 && status[0] == 1){					// aus
            if(index == 0){
                if(p < 0){
                    bezug_integral = 0;
                }else{
                    bezug_integral += (float)p / 3600 * zykluszeit / 1000;
                    Log.log(name + " - Bezug Integral: " + new DecimalFormat("#.#").format(bezug_integral) + " Wh", 2);
                    if(bezug_integral > bezug_stop){
                        aus();
                        return -leistung;
					}
                }
            }
		}
		if(leistung == 0){
			if(index != 0)
				stufe(stufe[0]);
			return p;
		}
		Log.log(name + " - Leistung ges.: " + (-p + leistung) + " W", 5);
		int p1 = leistung - p - leist_offset;
		if(-p > leist_offset + leist_hysterese)
			p1 -= leist_hysterese;
		int iNeu = Sonstiges.find(leist, p1);				// Index nach akt. Leistung 
		Log.log(name + " - Index: " + index + ", Index Neu: " + iNeu, 2);
		if(index == iNeu){									// keine Leistungsänderung -> nichts tun
			return 0;
		}
		if(index < iNeu){									// Leistungserhöhung
			stufe(iNeu);
			return leist[iNeu] - leistung;
		}else if(index > iNeu){								// Leistungsverringerung
			stufe(iNeu);
		}
		return 0;
	}
	
	abstract int[] getStatus();
	
//	abstract int getLeistung();
	
	abstract void setEin();
	
	abstract void setAus();
	
	abstract void setStufe(int st);
}

