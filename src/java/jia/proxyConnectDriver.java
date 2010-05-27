// Internal action code for project JDriver2Itsumo

package jia;

import java.util.logging.Level;
import java.util.logging.Logger;

import jason.*;
import jason.asSemantics.*;
import jason.asSyntax.*;
import arch.AgProxyArch;
import arch.AgProxyImpl;

public class proxyConnectDriver extends DefaultInternalAction {

	private Logger logger = Logger.getLogger(proxyConnectDriver.class.getName());
	
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
       
    	int agId = -1;
    	agId = Integer.parseInt(args[0].toString().replace("driver", ""));
        logger.log(Level.INFO, "agId="+agId+" waiting connection...");
    	
    	//enviar mensagem para o itsumo solicitando conexão para o motorista
    	AgProxyArch arch = (AgProxyArch)ts.getUserAgArch();
    	arch.getProxy().sendMessage("a;"+agId+";");
    	
        // everything ok, so returns true
        return true;
    }
}
