import org.json.*;

public class VerbraucherStufenHttp extends VerbraucherStufen{
	JSONObject daten;
	HttpRequest hr;
	boolean verbunden;
    int[][] rel_stufe = null ;
    boolean[] relais = null;
//    int[][] rel_stufe = {{1, 2}, {1, 3}, {2, 3}, {1}};
//    boolean[] relais = {false, true, true};
    int[] leistung;
	
	public VerbraucherStufenHttp(JSONObject w, int zy){
		super(w, zy);
		daten = w.getJSONObject("daten");
        if(daten.has("stufen")){
            JSONObject st = daten.getJSONObject("stufen");
            if(st.has("relais")){
                rel_stufe = Sonstiges.getIntArray2(st.getJSONArray("relais"));
                int i = 1;
                while(st.has("rel" + i))
                    i++;
                i--;
                relais = new boolean[i];
            }
        }
		hr = new HttpRequest(daten.getString("ip"), daten.getInt("port"), (daten.has("timeout"))? daten.getInt("timeout"): 1000);
	}
    
    private boolean inArray(int[] a, int x){
        boolean b = false;
        int i = 0;
        while(!b && i < a.length){
            if(x == a[i])
                b = true;
            i++;
        }
        return b;
    }
    
    private int getStufe(){
        if(rel_stufe != null && relais != null){
            boolean b = false;
            int i = 0;
            while(!b && i < rel_stufe.length){
                b = true;
                int ii = 0;
                while(b && ii < relais.length){
                    if(relais[ii]){
                        b = false;
                        for(int iii = 0; iii < rel_stufe[i].length; iii++)
                            if(ii + 1 == rel_stufe[i][iii])
                                b = true;
                    }else{
                        b = true;
                        for(int iii = 0; iii < rel_stufe[i].length; iii++)
                            if(ii + 1 == rel_stufe[i][iii])
                                b = false;
                    }
                    ii++;
                }
                i++;
            }
            if(b)
                return i - 1;
            else
                return -1;
        }
        return 0;
    }
    
	public int[] getStatus(){
        int[] erg = {-1, 0};
		
		try{
			JSONObject status = daten.getJSONObject("status");
			String response = hr.getResponse(Main.progName, status.getString("request"), status.getString("resource"), null);
			if(response.equals(""))
				return erg;
			JSONObject res = new JSONObject(response);
            int st;
            String s;
            if(relais != null){
                JSONObject stufen = daten.getJSONObject("stufen");
                for(int j = 0; j < relais.length; j++){
                    s = Sonstiges.getStringfromObjects(res, stufen.getJSONObject("rel" + (j + 1)).getString("status"));
                    relais[j] = (s.toUpperCase().equals("ON") || s.toUpperCase().equals("EIN") || s.equals("1"))? true: false;
                }
                st = getStufe();
            }else
                st = 0;

            s = status.getString("status");
            if(s.equals("stufe")){
                erg[0] = (st > -1)? 1: 0;
            }else{    
                s = Sonstiges.getStringfromObjects(res, s);
                erg[0] = (s.toUpperCase().equals("ON") || s.toUpperCase().equals("EIN") || s.equals("1"))? 1: 0;
            }
            if(st < 0) st = 0;
            erg[1] = stufe[st];
		}catch(Exception e){
			Log.err("VerbraucherStufenHttp.getStatus: " + e);
		}
		return erg;
	}
    
    public int getLeistung(){
        int erg = -1;
        
        try{
            if(!daten.has("messwerte"))
                return erg;
			JSONObject messwerte = daten.getJSONObject("messwerte");
			String response = hr.getResponse(Main.progName, messwerte.getString("request"), messwerte.getString("resource"), null);
			if(response.equals(""))
				return erg;
			JSONObject res = new JSONObject(response);
            int i = 1;
            while(messwerte.has("leistung" + i))
                i++;
            i--;
            leistung = new int[i];
            erg = 0;
            for(i = 0; i < leistung.length; i++){
                leistung[i] = Sonstiges.getIntfromObjects(res, messwerte.getString("leistung" + (i + 1)));
                erg += leistung[i];
            }
		}catch(Exception e){
			Log.err("VerbraucherStufenHttp.getLeistung: " + e);
		}
		return erg;
    }
	
	public void setEin(){
        JSONObject ein = daten.getJSONObject("ein");
        if(ein.has("relais"))
            setRelais(Sonstiges.getIntArray(ein.getJSONArray("relais")));
        else if(ein.has("request"))
            hr.getResponse(Main.progName, ein.getString("request"), ein.getString("resource"), ein.getString("inhalt"));
	}
	
	public void setAus(){
        JSONObject aus = daten.getJSONObject("aus");
        if(aus.has("relais"))
            setRelais(Sonstiges.getIntArray(aus.getJSONArray("relais")));
        else if(aus.has("request"))
            hr.getResponse(Main.progName, aus.getString("request"), aus.getString("resource"), aus.getString("inhalt"));
	}

	public void setRelais(int[] r){
        if(relais != null){
            JSONObject stufen = daten.getJSONObject("stufen");
            for(int i = 0; i < relais.length; i++)
                if(relais[i] && !inArray(r, i + 1)){
                    JSONObject rel = stufen.getJSONObject("rel" + (i + 1));
                    hr.getResponse(Main.progName, rel.getString("request"), rel.getString("resource_aus"), rel.getString("inhalt"));
//                    Log.log(name + " - Relais " + (i + 1) + " ausgeschaltet", 5);
                }
            for(int i = 0; i < relais.length; i++)
                if(!relais[i] && inArray(r, i + 1)){
                    JSONObject rel = stufen.getJSONObject("rel" + (i + 1));
                    hr.getResponse(Main.progName, rel.getString("request"), rel.getString("resource_ein"), rel.getString("inhalt"));
//                    Log.log(name + " - Relais " + (i + 1) + " eingeschaltet", 5);
                }
        }
	}

	public void setStufe(int st){
        if(rel_stufe != null){
            int index = Sonstiges.find(stufe, st);
//            Log.log(name + " - Setze Stufe " + st, 5);
            setRelais(rel_stufe[index]);
        }else{
            if(daten.has("stufen")){
                JSONObject stufe = daten.getJSONObject("stufe");
                String inhalt = stufe.getString("inhalt");
                inhalt = inhalt.replaceAll("%i", Integer.toString(st));
                hr.getResponse(Main.progName, stufe.getString("request"), stufe.getString("resource"), inhalt);
            }
        }
	}
}

