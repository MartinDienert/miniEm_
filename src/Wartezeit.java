public class Wartezeit{
	String name;
	long timer = -1;
	int dauer;
    int timeout;
    int strom = -1;
    int status = -1;
    
    public Wartezeit(String n, int d, int t){
		name = n;
		dauer = d;
        timeout = t;
    }
    
	public boolean start(int sta, int str){
        if(timer == -1 && (sta != -1 || str != -1)){
            strom = str;
            status = sta;
            timer = System.currentTimeMillis();
            Log.log(name + " gestartet.", 5);
			return true;
		}
		return false;
	}

	public void stop(){
        strom = -1;
        status = -1;
        timer = -1;
        Log.log(name + " gestopt.", 5);
	}
	
	public boolean laeuft(int sta, int str){
        int z = getZeit();
        if(strom != -1 || status != -1){
            if((strom == -1 || strom == str) && (status == -1 || status == sta)){
                strom = -1;
                status = -1;
                timer = System.currentTimeMillis();
                z = getZeit();
                Log.log(name + " restartet.", 5);
            }else{
                Log.log(name + " warten: " + z + "s", 2);
                if(z < timeout){
                    return true;
                }
                stop();
                return false;
            }
            
        }
        if(timer != -1){    
            Log.log(name + " laeuft: " + z + "s", 2);
            if(z < dauer){
                return true;
            }
            stop();
        }
        return false;
	}
    
	public boolean laeuft(int[] i){
        if(i.length > 1)
            return laeuft(i[0], i[1]);
        return false;
    }
	
	public int getZeit(){
		return Math.round((float)(System.currentTimeMillis() - timer) / 1000);
	}
}
