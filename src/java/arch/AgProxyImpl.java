package arch;

import static jason.asSyntax.ASSyntax.createLiteral;
import static jason.asSyntax.ASSyntax.createNumber;
import static jason.asSyntax.ASSyntax.createStructure;
import jason.RevisionFailedException;
import jason.asSyntax.Literal;
import jason.environment.grid.Location;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**  
 * Handle the (XML) communication with contest simulator. 
 * 
 * @author Jomi
 */
public class AgProxyImpl implements Runnable {

	@SuppressWarnings("serial")
	public class SocketClosedException extends Exception {}
	public static ByteBuffer buffer = ByteBuffer.allocate(10000);
	static int BUFFSIZE = 128000;
	
    String         	rid; // the response id of the current cycle
	AgProxyArch    	arq;
	boolean        	running = true;
	private boolean connected = false;
	
	private Socket socket;
	private InputStream inputstream;
	private OutputStream outputstream;
	
	private int networkport;
	private String networkhost;
	
	private Logger logger = Logger.getLogger(AgProxyImpl.class.getName());
	private DocumentBuilder documentbuilder;

	ConnectionMonitor monitor = new ConnectionMonitor();
	
	public AgProxyImpl(AgProxyArch arq, String host, int port) {
		
		logger = Logger.getLogger(AgProxyImpl.class.getName()+"."+arq.getAgName());
		logger.setLevel(Level.FINE);
		
		if (host.startsWith("\"")) {
			host = host.substring(1,host.length()-1);
		}
		setPort(port);
		setHost(host);
		
		this.arq = arq;
        
		connect();
		monitor.start();
	}
	
	protected boolean connect() {
    	connected = false;
		try {
			//socketaddress = new InetSocketAddress(networkhost,networkport);
            socket = new Socket(networkhost,networkport);//socket.connect(socketaddress);
			inputstream  = socket.getInputStream();
			outputstream = socket.getOutputStream();

			logger.log(Level.FINE, "connection established successuly!");
			connected = true;
			
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Connection exception "+e);			
		}			
        return connected;
    }
	
	public String getHost() {
		return networkhost;
	}
	public void setHost(String host) {
		this.networkhost = host;
	}
	
	public int getPort() {
		return networkport;
	}
	public void setPort(int port) {
		this.networkport=port;
	}	
	
	public void finish() {
	    running = false;
	    monitor.interrupt();
	}
	
    public boolean isConnected() {
    	return connected;
    }
    
	public byte[] receivePacket() throws IOException, SocketClosedException {
		
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		
		int read = inputstream.read();
		
		while (read!=0 && read!=-1) {
			if (read==-1) {
				throw new SocketClosedException(); 
			}
			
			buffer.write(read);

			if (inputstream.available()>0)
				read = inputstream.read(); else
					break;
		}
		
		logger.log(Level.FINE, buffer.toString());
		return buffer.toByteArray();
	}
	
	public String receiveStringPacket() throws IOException, SocketClosedException {
		
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		
		int read = inputstream.read();
		
		while (read!=0 && read!=-1) {
			if (read==-1) {
				throw new SocketClosedException(); 
			}
			
			buffer.write(read);

			if (inputstream.available()>0)
				read = inputstream.read(); else
					break;
		}
		
		return buffer.toString();
	}
	
    public void sendMessage(String message) throws SocketClosedException{
    	
    	try {
			outputstream.write(message.getBytes());
			outputstream.flush();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "error sending message...");
		}
    	
    }
	
    private void processMessage(String message) throws RevisionFailedException, SocketClosedException{
    	
    	if (message.length()>0){
    		
    		//logger.log(Level.INFO, "receive answer: " + message);

    		//inserir origem/destino 
    		if (message.contains("od;")){
    			String od[] = message.split(";");
    			logger.log(Level.INFO, od[1] + "|" + od[2]);
    			arq.delBel(Literal.parseLiteral("start(_)"));
    			arq.delBel(Literal.parseLiteral("goal(_)"));
	    		arq.addBel(Literal.parseLiteral("start("+od[1]+")"));
	    		arq.addBel(Literal.parseLiteral("goal("+od[2]+")"));
    		}
	    	else
	    	//solicita uma ação do agente (r=request)
	    	if (message.charAt(0) == 'r'){
	    		arq.delBel(Literal.parseLiteral("decide(_)"));
	    		arq.addBel(Literal.parseLiteral("decide(something)"));
	    		
	    		String msg = arq.getMessage();
	    		if (msg.length()>0){
		    		sendMessage(msg);
		    		if (msg.contains("goto"))
		    			logger.log(Level.INFO, "goto sent @ step " + message.substring(2));
		    		//arq.setMessage("x;0;");
	    		}
	    	}
	    	else
	    	//fim da simulação, não processa mais mensagens
	    	if (message.contains("end;")){
	    		finish();
	    		sendMessage("x;0;");
	    	}
    		
    	}
    	
    }
    
    public void run() {
    	
        while (running) {
            try {
                if (isConnected()) {
                	
            		//receiving info from socket
                	processMessage(receiveStringPacket());
                	
                } else {
                    // wait auto-reconnect
                    logger.info("waiting reconnection...");
                    try { Thread.sleep(2000); } catch (InterruptedException e1) {}
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "ACProxy run exception", e);
            }
        }
    }

	public void sendAction(String action) {
		try {
			//logger.info("sending action "+action+" for rid "+rid+" at "+arq.model.getAgPos(arq.getMyId()) );
			Document doc = documentbuilder.newDocument();
			Element el_response = doc.createElement("message");
			
			el_response.setAttribute("type","action");
			doc.appendChild(el_response);

			Element el_action = doc.createElement("action");
			if (action != null) {
				el_action.setAttribute("type", action);
			}
			el_action.setAttribute("id",rid);
			el_response.appendChild(el_action);

			//sendDocument(doc);
		} catch (Exception e) {
			logger.log(Level.SEVERE,"Error sending action.",e);
		}
	}

	/** checks the connection */
	class ConnectionMonitor extends Thread {
		long    sentTime = 0;
		int     count = 0;
		boolean ok = true;
		String  pingMsg;
		
		synchronized  public void run() {
			int d = new Random().nextInt(15000);
            try {
                while (running) {
                    if (isConnected())
                        sleep(40000+d);
                    else 
                        sleep(5000);
					count++;
					sentTime = System.currentTimeMillis();
                    ok = false;
					if (isConnected()) {
					    pingMsg = "test:"+count;
					    logger.info("Sending ping "+pingMsg);
						//sendPing(pingMsg);
						waitPong();
					} else {
					    logger.info("*** not connected!!! so no ping");
					}
					if (!ok) {
						logger.info("I likely loose my connection, reconnecting!");
						//reconnect();
						connect();
					}
			    }
            } catch (InterruptedException e) {
            } catch (Exception e) {
                logger.log(Level.WARNING,"Error in communication ",e);
            }
		}
		
		synchronized void waitPong() throws Exception {
			wait(10000);
		}
		
		synchronized void processPong(String pong) {
			long time = System.currentTimeMillis() - sentTime;
			logger.info("Pong "+pong+" for ping "+pingMsg+" in "+time+" milisec");
			ok = true;
			notify();
		}
	}
}
