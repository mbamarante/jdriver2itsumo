package arch;

import static jason.asSyntax.ASSyntax.createAtom;
import static jason.asSyntax.ASSyntax.createLiteral;
import static jason.asSyntax.ASSyntax.createNumber;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;

import jason.JasonException;
import jason.architecture.AgArch;
import jason.asSemantics.Message;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Structure;
import jason.mas2j.ClassParameters;
import jason.runtime.Settings;

public class ProxyArchAndImpl extends AgArch {

	protected Logger logger = Logger.getLogger(ProxyArchAndImpl.class.getName());
		
	@SuppressWarnings("serial")
	private class SocketClosedException extends Exception {}
	private List<Literal> percepts = new ArrayList<Literal>();
	private int networkport;
	private String networkhost;
	private Socket socket;
	
	private InputStream inputstream;
	private OutputStream outputstream;
	
	private boolean connected = false;
	
    @Override
    public void initAg(String agClass, ClassParameters bbPars, String asSrc, Settings stts) throws JasonException {
        super.initAg(agClass, bbPars, asSrc, stts);
        logger.log(Level.INFO, "Init Agent @ System.currentTimeMillis = " + System.currentTimeMillis());
        
        networkhost = stts.getUserParameter("host");
        networkport = Integer.parseInt(stts.getUserParameter("port"));
        
		if (networkhost.startsWith("\"")) {
			networkhost = networkhost.substring(1,networkhost.length()-1);
		}
        
        logger.log(Level.INFO, "Conecting Server " + networkhost + ":" + networkport + "...");
        if (connect()) logger.log(Level.INFO, "Connected!"); else logger.log(Level.INFO, "Conection Error!");
//        processParameters();
//        createCheckThread();
    }

	protected boolean connect() {
    	connected = false;
		try {
			//socketaddress = new InetSocketAddress(networkhost,networkport);
            socket = new Socket(networkhost,networkport);//socket.connect(socketaddress);
			inputstream  = socket.getInputStream();
			outputstream = socket.getOutputStream();
			connected = true;
            }                   
		 catch (Exception e) {
			logger.log(Level.SEVERE, "Connection exception "+e);			
		 }			
        return connected;
	}
    
    public boolean isConnected() {
    	return connected;
    }

	public byte[] receivePacket() throws IOException, SocketClosedException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		int read = inputstream.read();
		while (read!=0) {
			if (read==-1) {
				throw new SocketClosedException(); 
			}
			buffer.write(read);
			read = inputstream.read();
		}
		return buffer.toByteArray();
	}
    
    private void sendMessage(String message) throws SocketClosedException{
    	
    	try {
			outputstream.write(message.getBytes());
//			logger.log(Level.INFO, "receive answer: " + receivePacket().toString());
		} catch (IOException e) {
			logger.log(Level.SEVERE, "error sending message...");
		}
    	
    }
    
	@Override
	public List<Literal> perceive() {
    	//agDidPerceive(); // for crash control
			
		return new ArrayList<Literal>(percepts); // it must be a copy!
	}
    
	@Override
	public void checkMail() {
		
		super.checkMail();
		
//		System.out.println("message coming...");
		
		// remove messages related to obstacles and agent_position
		// and update the model
		Iterator<Message> im = getTS().getC().getMailBox().iterator();
		
		while (im.hasNext()) {
			
			Message m  = im.next();			
			String  ms = m.getPropCont().toString();
			
			logger.log(Level.INFO, "message content:" + ms);
			
			try {
				if (ms.contains("waitingCar")){
					if(isConnected()) sendMessage("a;1");
					Integer sender = Integer.parseInt(m.getSender().replace("driver", ""));
					logger.log(Level.INFO, "sender id:" + sender);
				}
				
//    			if (m.getIlForce().equals("tell-cows")) {
//    				im.remove();
//    			}
    			
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error checking email!",e);
            }
		}
    }
    
}
