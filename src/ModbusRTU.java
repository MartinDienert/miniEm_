import com.intelligt.modbus.jlibmodbus.master.ModbusMasterFactory;
import com.intelligt.modbus.jlibmodbus.serial.SerialUtils;
import com.intelligt.modbus.jlibmodbus.serial.SerialParameters;
import com.intelligt.modbus.jlibmodbus.serial.SerialPort;

//Parity:	None  - 0
//			Odd   - 1
//			Even  - 2
//			Mark  - 3
//			Space - 4

public class ModbusRTU extends Modbus{
	
	public ModbusRTU(String dev, int baudrate, int bits, int parity, int stopbit){
		SerialParameters sp = new SerialParameters();
		try{
			sp.setDevice(dev);
			sp.setBaudRate(SerialPort.BaudRate.getBaudRate(baudrate));
			sp.setDataBits(bits);
			sp.setParity(SerialPort.Parity.getParity(parity));
			sp.setStopBits(stopbit);
		}catch(Exception e){
			Log.err("ModbusRTU: " + e);
		}
		SerialUtils.setSerialPortFactoryJSSC();
		try{
			modbusMaster = ModbusMasterFactory.createModbusMasterRTU(sp);
		}catch(Exception e){
			Log.err("ModbusRTU: " + e);
			e.printStackTrace();
		}
	}
}
