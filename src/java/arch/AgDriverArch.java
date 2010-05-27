package arch;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import jason.architecture.AgArch;
import jason.asSyntax.Literal;
import jason.JasonException;
import jason.mas2j.ClassParameters;
import jason.runtime.Settings;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AgDriverArch extends AgArch {
	
	protected Logger logger = Logger.getLogger(AgDriverArch.class.getName());
	private List<Literal> percepts = new ArrayList<Literal>();
	
    @Override
    public void initAg(String agClass, ClassParameters bbPars, String asSrc, Settings stts) throws JasonException {
        super.initAg(agClass, bbPars, asSrc, stts);
        logger.log(Level.INFO, "Init Agent!");
//        processParameters();
//        createCheckThread();
    }
	
}
