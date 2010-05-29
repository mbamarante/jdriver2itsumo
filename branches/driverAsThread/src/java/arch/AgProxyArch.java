package arch;

import static jason.asSyntax.ASSyntax.createLiteral;
import static jason.asSyntax.ASSyntax.createNumber;
import jason.JasonException;
import jason.RevisionFailedException;
import jason.architecture.AgArch;
import jason.asSemantics.ActionExec;
import jason.asSyntax.Literal;
import jason.mas2j.ClassParameters;
import jason.runtime.Settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import arch.AgProxyImpl.SocketClosedException;

/** 
 * 
 * Jason agent architecture customisation 
 * (it links the AgentSpeak interpreter to the contest simulator)
 * 
 * @author Jomi
 *
 */			 //Agent Customisation Architecture (ACArchitecture)  
public class AgProxyArch extends AgArch {
	
    public static final int   actionTimeout = 2000; // timeout to send an action
    
    int     simStep  = 0;
    
	private Logger logger;
	private int networkport;
	private String networkhost;
	private String networkmessage;

	private AgProxyImpl       proxy;
	private List<Literal> percepts = new ArrayList<Literal>();
	private WaitSleep     waitSleepThread;
	
	@Override
    public void initAg(String agClass, ClassParameters bbPars, String asSrc, Settings stts) throws JasonException {
		
		super.initAg(agClass, bbPars, asSrc, stts);
		logger = Logger.getLogger(AgProxyArch.class.getName()+"."+getAgName());

        networkhost = stts.getUserParameter("host");
        networkport = Integer.parseInt(stts.getUserParameter("port"));
        setMessage("x;0;");
        
		if (networkhost.startsWith("\"")) {
			networkhost = networkhost.substring(1,networkhost.length()-1);
		}

		waitSleepThread = new WaitSleep();
		waitSleepThread.start();

		proxy = new AgProxyImpl(this, 
								networkhost, 
								networkport);
		
		new Thread(proxy,"AgProxy").start();
	
		int agId = Integer.parseInt(getAgName().replace("driver", ""));
		
		try {
			proxy.sendMessage("a;"+agId+";");
		} catch (SocketClosedException e) {
			logger.log(Level.SEVERE, "error sending message...");
		}

	}
	
	public AgProxyImpl getProxy() {
		
		return proxy;
	}
	
	public void setMessage(String message){
		
		networkmessage = message;
	}

	public String getMessage(){
		
		return networkmessage;
	}
	@Override
	public void stopAg() {
	    super.stopAg();
	    logger.log(Level.FINE, "stopping agent...");
	    proxy.finish();
	    waitSleepThread.interrupt();
	}
	
	@Override
	public List<Literal> perceive() {
    	//agDidPerceive(); // for crash control
		return new ArrayList<Literal>(percepts); // it must be a copy!
	}

	@Override
	/** when the agent can sleep, i.e. has nothing else to decide, sent its last action to the simulator */ 
	public void sleep() {
        waitSleepThread.go();
	    super.sleep();
	}
	
	public int getSimStep() {
	    return simStep;
	}
	
    void setSimStep(int s) {
//        ((SelectEvent)getTS().getAg()).cleanCows();
//    	simStep = s;
//    	super.setCycleNumber(simStep);
//		if (view != null) view.setCycle(simStep);
//        if (writeStatusThread != null) writeStatusThread.go();
    }
	
	public void startNextStep(int step, List<Literal> p) {
		percepts = p;
		waitSleepThread.newCycle();
		getTS().getUserAgArch().getArchInfraTier().wake();
    	setSimStep(step);
	}
	
	/** all actions block its intention and succeed in the end of the cycle, 
	 *  only the last action of the cycle will be sent to simulator */ 
	@Override
	public void act(ActionExec act, List<ActionExec> feedback) {
        if (act.getActionTerm().getFunctor().equals("do")) {
            waitSleepThread.addAction(act);
        } else {
        	logger.info("ignoring action "+act+", it is not a 'do'.");
        }
	}

//    void simulationEndPerceived(String result) throws RevisionFailedException {
//    	getTS().getAg().addBel(Literal.parseLiteral("end_of_simulation("+result+")"));
//    	model   = null;
//        playing = false;
//        perceivedCows.clear();
//        lastSeen.clear();
//        if (view != null) view.dispose();
//    }
	
    private void setStart(int s) throws RevisionFailedException {
    	addBel(createLiteral("start", createNumber(s)));
    }
	
    protected void addBel(Literal l) throws RevisionFailedException {
    	getTS().getAg().addBel(l);
    }
	
    protected void delBel(Literal l) throws RevisionFailedException {
    	getTS().getAg().abolish(l, null);
    }
    
	//@Override
    void simulationEndPerceived(String result) throws RevisionFailedException {
	    percepts = new ArrayList<Literal>();
	    //super.simulationEndPerceived(result);
    	getTS().getAg().addBel(Literal.parseLiteral("end_of_simulation("+result+")"));
    	
//    	model   = null;
//        playing = false;
//        perceivedCows.clear();
//        lastSeen.clear();
//        if (view != null) view.dispose();
    }

    // TODO: create a new agent and plug it on the connection
	
	/** this method is called when the agent crashes and other approaches to fix it (fix1 and fix2) does not worked */
    /*
	@Override
    protected boolean fix3() throws Exception {
        getTS().getLogger().warning("Cloning!");
        
        RuntimeServicesInfraTier services = getArchInfraTier().getRuntimeServices();

        // really stops the agent (since stop can block, use a thread to run it)
        new Thread() {   public void run() {
            getArchInfraTier().stopAg();
        }}.start();
        
        // create a new overall agent (arch, thread, etc.)
        ChangeArchFixer arch = (ChangeArchFixer)services.clone(getTS().getAg(), this.getClass().getName(), getTS().getUserAgArch().getAgName()+"_clone");
        arch.processParameters();
        arch.createCheckThread();
        arch.getTS().getC().create(); // use a new C.
        
        //arch.getTS().getLogger().info("Cloned!");
        
        // just to test, add !start
        arch.getTS().getC().addAchvGoal(Literal.parseLiteral("start"), Intention.EmptyInt);
        return false;
    }
     */
    
    /*
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
					if(proxy.isConnected()) proxy.sendMessage("a;1");
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
    */
	
    class WaitSleep extends Thread {
	    
	    ActionExec        lastAction = null;
        String            lastActionInCurrentCycle;
	    Queue<ActionExec> toExecute = new ConcurrentLinkedQueue<ActionExec>();
	    Lock              lock = new ReentrantLock();
	    Condition         cycle = lock.newCondition();
	    long              timestartcycle = 0;
	    long              timeLastAction = 0;
	    int               cycleCounter = 0;
	    StringBuilder     notsent;
	    
	    WaitSleep() {
	        super("WaitSleepToSendAction"+getAgName());
	    }
	    
	    void addAction(ActionExec action) {
	        if (action == null) return;
	        logger.info("adding action "+action.getActionTerm()+" to be executed.");
	    	lock.lock();
            try {
    	        if (lastAction != null)
    	            toExecute.offer(lastAction);
    	        lastAction = action;
            } finally {
            	lock.unlock();
            }
        }
	    
	    void newCycle() {
            lock.lock();
            try {
    	    	cycleCounter++;
    	    	if (getSimStep() == 1) cycleCounter = 1;
    	    	
                notsent = new StringBuilder();
                if (toExecute.size() > 1) {
                	notsent.append(" The following was not sent: ");
                }
            
                // set all actions as successfully executed
                List<ActionExec> feedback = getTS().getC().getFeedbackActions();
                synchronized (feedback) {
                    while (!toExecute.isEmpty()) {
                        ActionExec action = toExecute.poll();
                        action.setResult(true);
                        feedback.add(action);
                        if (!toExecute.isEmpty())
                            notsent.append(action.getActionTerm()+" ");
                    }                
                }
                
                // prepare msg to print out
                String w = "";
                if (lastActionInCurrentCycle == null && cycleCounter > 10) { // ignore problem in the first cycles (the agent is still in setup!)
                    //addRestart();
                    w = "*** ";
                }
                
                String timetoact = ". ";
                if (lastActionInCurrentCycle != null && timestartcycle > 0) {
                    timetoact = " (act in "+ (timeLastAction -  timestartcycle) +" ms)";
                }
                timestartcycle = System.currentTimeMillis();
    
                logger.info(w+"Last sent action was "+lastActionInCurrentCycle+" for cycle "+getSimStep()+ timetoact + notsent);            
                //setLastAct(lastActionInCurrentCycle);
                lastActionInCurrentCycle = null;
                
                lastAction = null; // so that the run will not execute the action anymore
                
                go(); // reset the wait
            } finally {
                lock.unlock();
            }
	    }
	    
    	void go() {
	    	lock.lock();
            try {
            	cycle.signal();
            } finally {
            	lock.unlock();
            }
        }
	    
	    boolean waitSleep() throws InterruptedException {
	    	lock.lock();
            try {
            	return !cycle.await(actionTimeout, TimeUnit.MILLISECONDS);
            } finally {
            	lock.unlock();
            }
        }
	    
	    @Override
	    public void run() {
	        while (true) {
	            lock.lock();
	            try {
                    waitSleep();
                    
	                if (lastAction != null) {
                        lastActionInCurrentCycle = lastAction.getActionTerm().getTerm(0).toString();
	                    proxy.sendAction(lastActionInCurrentCycle);
	                    toExecute.offer(lastAction);
	                    timeLastAction = System.currentTimeMillis();
	                    lastAction = null; // to not sent it again in the same cycle due several sleep
	                }
	            } catch (InterruptedException e) {
	                return; // condition to stop the thread 
	            } catch (Exception e) {
	                logger.log(Level.SEVERE, "Error sending "+lastAction+" to simulator.",e);
	                toExecute.clear();
	            } finally {
	                lock.unlock();
	            }
	        }
	    }
	}

}
