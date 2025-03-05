import com.intelligt.modbus.jlibmodbus.master.ModbusMaster;

abstract class Modbus{
	int fehler = 0;
	ModbusMaster modbusMaster;
	
	public int[] getModbusHoldingRegister(int id, int reg, int anzahl){
		int[] ret = null;

		try{
			if (!modbusMaster.isConnected()) {
				modbusMaster.connect();
			}
			ret = modbusMaster.readHoldingRegisters(id, reg, anzahl);
			for(int i = 0; i < ret.length; i++)
				ret[i] = int16ToInt32(ret[i]);
		}catch (Exception e){
			fehler = 1;
			Log.err("Modbus.getModbusRegisterInt: " + e);
		} finally {
			try {
				modbusMaster.disconnect();
			}catch (Exception e) {
			}
		}
		return ret;
	}
	
	public int getModbusHoldingRegister(int id, int reg){
		int[] erg = getModbusHoldingRegister(id, reg, 1);
        if(fehler == 0 && erg != null)
            return erg[0];
        else
            return 0;
	}
	
	public int[] getModbusInputRegister(int id, int reg, int anzahl){
		int[] ret = null;

		try{
			if (!modbusMaster.isConnected()) {
				modbusMaster.connect();
			}
			ret = modbusMaster.readInputRegisters(id, reg, anzahl);
			for(int i = 0; i < ret.length; i++)
				ret[i] = int16ToInt32(ret[i]);
		}catch (Exception e){
			fehler = 1;
			Log.err("Modbus.getModbusRegisterInt: " + e);
		} finally {
			try {
				modbusMaster.disconnect();
			}catch (Exception e) {
			}
		}
		return ret;
	}
	
	public int getModbusInputRegister(int id, int reg){
		int[] erg = getModbusInputRegister(id, reg, 1);
        if(fehler == 0 && erg != null)
            return erg[0];
        else
            return 0;
	}
	
	int int16ToInt32(int i){
		if(i > 32767)
			return i + -65536;
		else
			return i;
	}
	
	public int getFehler(){
		return fehler;
	}
}
