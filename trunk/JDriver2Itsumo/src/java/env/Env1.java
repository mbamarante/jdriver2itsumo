package env;

// Environment code for project delegate-tasks

import jason.asSyntax.*;
import jason.environment.*;
import java.util.logging.*;

public class Env1 extends Environment {

    private Logger logger = Logger.getLogger("delegate-tasks."+Env1.class.getName());

    /** Called before the MAS execution with the args informed in .mas2j */
    @Override
    public void init(String[] args) {
        super.init(args);
        addPercept(Literal.parseLiteral("percept(demo)"));
        //addPercept(Literal.parseLiteral(".create_agent(worker10,\"/asl/worker0.asl\")"));
    }

    @Override
    public boolean executeAction(String agName, Structure action) {
		if (action.getFunctor().equals("doTask")) {
			logger.info(agName + ": doing task " + action.getTerm(0));
			
			int sfinish = 1 + (int)(Math.random() * 10);  
			
			if (sfinish > 7)
				addPercept(Literal.parseLiteral("completedTask(" + action.getTerm(0) + ")"));
		}
		else {
			logger.info("executing: "+action+", but not implemented!");
		}
        return true;
    }

    /** Called before the end of MAS execution */
    @Override
    public void stop() {
        super.stop();
    }
}
