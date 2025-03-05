/*----------------------------------------------------------------------------------------------
* Das Programm miniEm ist ein einfacher Energie-Manager zum steuern von Heizungen, Warmwasserboilern,
* Wallboxen und ähnlichen elektrischen Verbrauchern zur Ausnutzung des PV-Überschußes.
*
* Copyright (C) 2003  Martin Dienert
*  Homepage: http:www.mdienert.de
*  E-Mail:   martin.dienert@gmx.de

* This program is free software; you can redistribute it and/or modify it under the terms
* of the GNU General Public License as published by the Free Software Foundation;
* either version 2 of the License, or (at your option) any later version.
* 
* This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
* without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License along with this program;
* if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
* 
* 
* Auf Deutsch:
* Dieses Programm ist freie Software. Sie können es unter den Bedingungen
* der GNU General Public License, wie von der Free Software Foundation veröffentlicht,
* weitergeben und/oder modifizieren, entweder gemäß Version 2 der Lizenz oder
* (nach Ihrer Option) jeder späteren Version.
* 
* Die Veröffentlichung dieses Programms erfolgt in der Hoffnung, daß es Ihnen von Nutzen
* sein wird, aber OHNE IRGENDEINE GARANTIE, sogar ohne die implizite Garantie der MARKTREIFE
* oder der VERWENDBARKEIT FÜR EINEN BESTIMMTEN ZWECK. Details finden Sie in der
* GNU General Public License.
* 
* Sie sollten eine Kopie der GNU General Public License zusammen mit diesem Programm erhalten haben.
* Falls nicht, schreiben Sie an die Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
* 
*--------------------------------------------------------------------------------------*/

import java.nio.file.*;
import java.util.*;
import org.json.*;

public class Main{
	final static String progName = "miniEm";
	final static String beschreibung = "mini Energie-Manager";
	final static String vers = "0.1";
	final static int debuglevel = 0;

	public static void main(String[] args){
		Hauptschleife hs;
		Debug.setDebuglevel(debuglevel);
		
		System.out.println(progName + " - "+ beschreibung + ", Version: " + vers);
		System.out.println();
		try{
			String s;
			Debug.start(1);
			if(args.length > 0)
				s = Files.readString(Path.of(args[0]));
			else
				s = Files.readString(Path.of("miniEm.json"));
			Debug.logZeit(1);
			JSONObject config = new JSONObject(s);
			Debug.logZeit(1);
			Log.setLoglevel(config.getInt("loglevel"));
			Log.setLoglevelDatei(config.getInt("logDatei"));
			Log.setLoglevelMw(config.getInt("logMessw"));
			Log.setLogDateiname(config.getString("logdateiname"));
			Log.setMWDateiname(config.getString("mwdateiname"));
			Log.log("---Programmstart---", 5);
			Sonstiges.eingabedatei = config.getString("eingabedatei");
			Sonstiges.ausgabedatei = config.getString("ausgabedatei");
			hs = new Hauptschleife(config);
			Timer timer = new Timer();
			timer.schedule(hs, 2000, config.getInt("zykluszeit"));
		}catch(Exception e){
			Log.err("Main.main: " + e);
		}
	}
}
