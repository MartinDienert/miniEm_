import com.intelligt.modbus.jlibmodbus.master.ModbusMasterFactory;
import com.intelligt.modbus.jlibmodbus.tcp.TcpParameters;
import java.net.InetAddress;

public class ModbusTCP extends Modbus{
	
	public ModbusTCP(String ip, int port){
		TcpParameters tcp = new TcpParameters();
		tcp.setPort(port);
		try{
			tcp.setHost(InetAddress.getByName(ip));
		}catch(Exception e){
			Log.err("Modbus: " + e);
		}
		modbusMaster = ModbusMasterFactory.createModbusMasterTCP(tcp);
		com.intelligt.modbus.jlibmodbus.Modbus.setAutoIncrementTransactionId(true);
	}
}
