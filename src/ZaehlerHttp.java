import org.json.JSONObject;

public class ZaehlerHttp extends Zaehler{
	JSONObject daten;
	HttpRequest hr;
    int[] leistung;
	
	public ZaehlerHttp(JSONObject z){
		super(z);
		daten = z.getJSONObject("daten");
        if(daten.has("messwerte")){
			hr = new HttpRequest(daten.getString("ip"), daten.getInt("port"), (daten.has("timeout"))? daten.getInt("timeout"): 1000);
		}else{
			fehler = 1;
			throw new ZaehlerModbusTcpException();
        }
	}
	 
	public int getLeistung(){
        int erg = -1;
        
        try{
            if(!daten.has("messwerte")){
				fehler = 1;
                return erg;
			}
			JSONObject messwerte = daten.getJSONObject("messwerte");
			String response = hr.getResponse(Main.progName, messwerte.getString("request"), messwerte.getString("resource"), null);
			if(response.equals("")){
				fehler = 2;
				return erg;
			}
			JSONObject res = new JSONObject(response);
            int i = 0;
            while(messwerte.has("leistung" + (i + 1)))
                i++;
            leistung = new int[i];
            erg = 0;
            for(i = 0; i < leistung.length; i++){
				int p = Sonstiges.getIntfromObjects(res, messwerte.getString("leistung" + (i + 1)));
				if(messwerte.has("faktorLeistung" + (i + 1)))
					p *= messwerte.getInt("faktorLeistung" + (i + 1));
                erg += p;
            }
		}catch(Exception e){
			Log.err("ZaehlerHttp.getLeistung: " + e);
		}
		return erg;
	}
	
	public float getSpannung(){
		float erg = 0;
		return erg;
	}
}

class ZaehlerHttpException extends RuntimeException{
	ZaehlerHttpException(){
		super("Kein gültigen Zählerdaten definiert.");
	}
}

