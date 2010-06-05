// Internal action code for project JDriver2Itsumo

package jia;

import java.util.logging.Level;
import java.util.logging.Logger;

import arch.AgProxyArch;
import jason.*;
import jason.asSemantics.*;
import jason.asSyntax.*;

public class driveOnPath extends DefaultInternalAction {

	private Logger logger = Logger.getLogger(driveOnPath.class.getName());
	
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
    	
    	int agId = -1;
    	agId = Integer.parseInt(args[0].toString().replace("driver", ""));
    	
    	AgProxyArch arch = (AgProxyArch)ts.getUserAgArch();
    	arch.setMessage("goto;"+args[0]+";"+args[1]+";");

        logger.log(Level.INFO, "(" + arch.getAgName() + ") setMessage: goto;"+args[0]+";"+args[1]+";");
    	
        // everything ok, so returns true
        return true;
    }
}
