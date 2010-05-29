// Internal action code for project JDriver2Itsumo

package jia;

import java.util.logging.Level;
import java.util.logging.Logger;

import arch.AgProxyArch;
import jason.*;
import jason.asSemantics.*;
import jason.asSyntax.*;

public class doNothing extends DefaultInternalAction {

	private Logger logger = Logger.getLogger(doNothing.class.getName());
	
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
    	
    	AgProxyArch arch = (AgProxyArch)ts.getUserAgArch();
    	//arch.setMessage("x;0;");
    	
        // everything ok, so returns true
        return true;
    }
}
