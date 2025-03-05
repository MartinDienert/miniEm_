import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Log{
	static int loglevel = 0;
    static int loglevelDatei = 0;
    static StringBuilder logString = new StringBuilder();
    static String logDateiName;
	static int loglevelMw = 0;
	static StringBuilder mwString = new StringBuilder();
	static String mwDateiname;
	
	public static void setLoglevel(int l){
		loglevel = l;
	}
	
	public static void setLoglevelDatei(int l){
		loglevelDatei = l;
	}
	
	public static void setLogDateiname(String s){
		logDateiName = s;
	}
	
	public static void setLoglevelMw(int l){
		loglevelMw = l;
	}
	
	public static void setMWDateiname(String s){
		mwDateiname = s;
	}
	
	public static void err(String n){
		Date datum = new Date();
		SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy kk:mm:ss");
		System.err.println(format.format(datum) + " Fehler: " + n);
        logString.append(format.format(datum)).append(" Fehler: ").append(n).append("\n");
	}
	
	public static void log(String n, int l){
		if(loglevel >= l || loglevelDatei >= l){
			Date datum = new Date();
			SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy kk:mm:ss");
            if(loglevel >= l)
                System.out.println(format.format(datum) + " " + n);
            if(loglevelDatei >= l)
                logString.append(format.format(datum)).append(" ").append(n).append("\n");
        }
	}
	
	public static void log(String n){
		log(n, 0);
	}
	
	public static void logDatei(){
 		if(logString.toString().length() > 0){
			try{
				Files.writeString(Path.of(logDateiName), logString.toString(), StandardOpenOption.APPEND);
			}catch(Exception e){
                try{
                    Files.writeString(Path.of(logDateiName), logString.toString(), StandardOpenOption.CREATE);
                }catch(Exception ee){
                    Log.err("Log.logDatei: " + ee);
                }
			}
            finally{
                logString = new StringBuilder();
            }
        }
    }

	public static void addMW(int w, int l){
		if(loglevelMw >= l){
            if(mwString.length() > 0)
                mwString.append(";");
            mwString.append(Integer.toString(w));
        }
	}

	public static void logMW(){
		if(mwString.length() > 0){
            Date datum = new Date();
            SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy kk:mm:ss");
            mwString.insert(0, format.format(datum) + " ");
            mwString.append("\n");
			try{
				Files.writeString(Path.of(mwDateiname), mwString.toString(), StandardOpenOption.APPEND);
			}catch(Exception e){
                try{
                    Files.writeString(Path.of(mwDateiname), mwString.toString(), StandardOpenOption.CREATE);
                }catch(Exception ee){
                    Log.err("Log.logMW: " + ee);
                }
			}
            finally{
                mwString = new StringBuilder();
            }
		}		
	}

}
