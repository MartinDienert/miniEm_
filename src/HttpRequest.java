import java.io.*;
import java.net.*;

public class HttpRequest{
	String ip;
	int port;
	int timeout = 1000;
	Socket socket;
	BufferedReader reader;
	PrintWriter writer;
	String str;
	int fehler;															// 0 = kein Fehler, 1 = Timeout, 2 = Timeout2
	
	public HttpRequest(String ip, int port, int timeout){
		this.ip = ip;
		this.port = port;
		this.timeout = timeout;
	}
	
	public HttpRequest(String ip, int port){
		this(ip, port, 1000);
	}
	
	public String getResponse(String name, String req, String res, String con){
		try{
			fehler = 0;
			str = null;
			socket = new Socket();
			InetSocketAddress addresse = new InetSocketAddress(ip, port);
			socket.connect(addresse, timeout);
			fehler = (socket.isConnected())? 0: 1;
			long lx = System.currentTimeMillis();
			long threadDauer = 0;
			new RequestThread(socket, name, req, res, con).start();
			while(str == null && threadDauer <= timeout){
				Thread.sleep(10);
				threadDauer = System.currentTimeMillis() - lx;
			}
			if(socket.isConnected()){
				socket.close();
			}	
			if(threadDauer > timeout){
				str = "";
				fehler = 2;
			}
			return str;
		}catch(Exception e){
			fehler = 1;
			Log.log("HttpRequest.getResponse: " + e, 5);
			return "";
		}
	}
	
	public class RequestThread extends Thread{
		Socket socket;
		String name;
		String req;
		String res;
		String con;
		
		public RequestThread(Socket s, String name, String req, String res, String con){
			socket = s;
			this.name = name;
			this.req = req;
			this.res = res;
			this.con = con;
		}
		
		@Override
		public void run(){
			try{
				InputStream input = socket.getInputStream();
				reader = new BufferedReader(new InputStreamReader(input));
				OutputStream output = socket.getOutputStream();
				writer = new PrintWriter(new OutputStreamWriter(output));
				writer.printf("%s %s HTTP/1.1\r\n", req, res);
				writer.printf("Host: %s\r\n", ip);
				writer.printf("User-Agent: %s\r\n", name);
				writer.print("Accept: application/json\r\n");
				if(con != null){
					writer.print("Content-Type: application/json\r\n");
					writer.print("Content-Length: " + con.length() + "\r\n");
				}
				writer.print("\r\n");
				if(con != null)
					writer.println(con);
				writer.flush();
				String line;
				line = reader.readLine();
				int pos;
				int laenge = 0;
				boolean chunked = false;
				StringBuilder response = new StringBuilder();
				while ((line = reader.readLine()) != null){
					line = line.trim();
					if (line.equals("")) {
						break;
					}
					pos = line.indexOf(":");
					if (pos > 0 && line.substring(0, pos).equals("Content-Length")){
						laenge = Integer.parseInt(line.substring(pos + 1).trim());
					}
					if (pos > 0 && line.substring(0, pos).equals("Transfer-Encoding")){
						if(line.indexOf("chunked", pos) > -1)
							chunked = true;
					}
				}
				if(chunked){
					line = reader.readLine();
					laenge = Integer.parseInt(line.trim(), 16);
				}
				if(laenge > 0){
					char[] puffer = new char[laenge];
					laenge = reader.read(puffer, 0, laenge);
					if(laenge > 0)
						response.append(String.valueOf(puffer)).append("\n");
				}
				socket.close();
				str = response.toString();
			}catch(IOException e){
				Log.log("HttpRequest.getResponse.RequestThread: " + e, 5);
			}
		}
	}
}

