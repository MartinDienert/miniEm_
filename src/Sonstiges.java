
import java.nio.file.Files;
import java.nio.file.Path;

import org.json.JSONArray;
import org.json.JSONObject;

public class Sonstiges{
	public static String eingabedatei;
	public static String ausgabedatei;
	public static String eingabe;
	public static String ausgabe;
	
	public static int istInteger(String z){
		if (z == null) {
			return -1;
		}
		int i;
		try {
			i = Integer.parseInt(z);
		} catch (NumberFormatException nfe) {
			return -1;
		}
		return i;
	}
	
	public static int find(int[] a, int w){
		int i = 0;
		while(i < a.length && a[i] <= w)
			i++;
		if(i > 0)
			i--;
		return i;
	}
	
	public static int[] getIntArray(JSONArray a){
		int[] ar = new int[a.length()];
		for(int i = 0; i < a.length(); i++)
			ar[i] = a.getInt(i);
		return ar;
	}
	
	public static int[][] getIntArray2(JSONArray a){
		int[][] ar = new int[a.length()][];
		for(int i = 0; i < a.length(); i++)
			ar[i] = getIntArray(a.getJSONArray(i));
		return ar;
	}
	
	public static String[] getStringArray(JSONArray a){
		String[] ar = new String[a.length()];
		for(int i = 0; i < a.length(); i++)
			ar[i] = a.getString(i);
		return ar;
	}
	
	public static String[][] getStringArray2(JSONArray a){
		String[][] ar = new String[a.length()][];
		for(int i = 0; i < a.length(); i++)
			ar[i] = getStringArray(a.getJSONArray(i));
		return ar;
	}
	
//	public String[] split
	
	public static int getIntfromObjects(JSONObject j, String key){
		JSONObject jo = j;
		String[] strA = key.split("\\{");
		int l = strA.length - 1;
		for(int i = 0; i < l; i++)
			jo = jo.getJSONObject(strA[i]);
		String[] strB = strA[l].split("\\[");
		if(strB.length > 1)
			return jo.getJSONArray(strB[0]).getInt(Integer.parseInt(strB[1]));
		else
			return jo.getInt(strA[l]);
	}
	
	public static String getStringfromObjects(JSONObject j, String key){
		JSONObject jo = j;
		String[] strA = key.split("\\{");
		int l = strA.length - 1;
		for(int i = 0; i < l; i++)
			jo = jo.getJSONObject(strA[i]);
		String[] strB = strA[l].split("\\[");
		if(strB.length > 1)
			return jo.getJSONArray(strB[0]).getString(Integer.parseInt(strB[1]));
		else
			return jo.getString(strA[l]);
	}
	
	public static void eingabe_lesen(){
		try{
			eingabe = Files.readString(Path.of(eingabedatei));
		}catch(Exception e){
			Log.err("Sonstiges.eingabe_lesen: " + e);
			eingabe = "modus:1";
		}
	}
	
	public static void ausgabe_lesen(){
		try{
			ausgabe = Files.readString(Path.of(ausgabedatei));
		}catch(Exception e){
			Log.log("Sonstiges.ausgabe_lesen: " + e, 5);
			ausgabe = "";
		}
	}
	
	public static void ausgabe_schreiben(){
		try{
			Files.writeString(Path.of(ausgabedatei), ausgabe);
		}catch(Exception e){
			Log.err("Sonstiges.ausgabe_schreiben: " + e);
		}
	}
	
	public static int eintrag_lesen(String s){
		int p1 = eingabe.indexOf(s + ":");
		if(p1 > -1){
			p1 += s.length() + 1;
			int p2 = eingabe.indexOf("\n", p1);
			if(p2 == -1)
				p2 = eingabe.length();
			return Integer.parseInt(eingabe.substring(p1, p2));
		}
		return -1;
	}
	
	public static int[] eintrag_lesen(String[] s){
		int[] erg = new int[s.length];
		for(int i = 0; i < s.length; i++){
			erg[i] = eintrag_lesen(s[i]);
		}
		return erg;
	}
	
	public static void eintrag_schreiben(String s, String w){
		int p1 = ausgabe.indexOf(s + ":");
		if(p1 == -1){
			if(ausgabe.length() > 0 && !ausgabe.endsWith("\r\n"))
				ausgabe += "\r\n";
			ausgabe += s + ":" + w;
		}else{
			p1 += s.length() + 1;
			int p2 = ausgabe.indexOf("\r\n", p1);
			if(p2 == -1){
				p2 = ausgabe.length();
				ausgabe += "\r\n";
			}
			String s1 = ausgabe.substring(0, p1);
			String s2 = ausgabe.substring(p2);
			ausgabe = s1 + w + s2;
		}
	}
	
	public static void eintrag_schreiben(String[] s, String[] w){
		for(int i = 0; i < s.length; i++){
			if(i < w.length)
				eintrag_schreiben(s[i], w[i]);
		}
	}
	
	public static void eintrag_schreiben(String s, int w){
		eintrag_schreiben(s, Integer.toString(w));
	}
	
	public static void eintrag_schreiben(String[] s, int[] w){
		for(int i = 0; i < s.length; i++){
			if(i < w.length)
				eintrag_schreiben(s[i], w[i]);
		}
	}
}
