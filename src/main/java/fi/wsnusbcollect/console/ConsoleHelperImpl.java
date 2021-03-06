/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.console;

import fi.wsnusbcollect.App;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.python.core.Py;
import org.python.core.ThreadState;
import org.python.util.InteractiveConsole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Console helper class.
 * Should helps when working with Jython shell. Initialization, basic commands.
 * Common util methods, debugging output.
 * 
 * Sometimes Jython shell throws ugly and long exception when program is interrupted. This
 * exception comes from python code - if interrupted. If I place whole
 * python code to try, except then no exception is returned.
 * 
 * @author ph4r05
 */
@Repository
@Transactional
public class ConsoleHelperImpl implements ConsoleHelper {
    private static final Logger log = LoggerFactory.getLogger(ConsoleHelperImpl.class);
    
    @PersistenceContext
    private EntityManager em;
    
    @Autowired
    private JdbcTemplate template;
    
    // interactive console 
    protected InteractiveConsole interp;
    
    // flag determining whether restart shell after exit
    protected boolean restartShell=true;
    
    // autocomplete script
    public final static String AUTOCOMPLETE = "import rlcompleter, readline\n" + 
                        "readline.parse_and_bind('tab: complete')";
    
    public final static String EXITROUTINE = "import sys\n"
                        + "def exitNow():\n"
                        + " sys._jy_console.exitShell()\n";
    
    public final static String UNSUSPENDROUTINE = "import sys\n"
                        + "def unsuspend():\n"
                        + " sys._jy_expCoord.unsuspend()\n";
    
    // python based sighandler. Useful when needed to stop execution of frozen script
    // For details you can see: http://bugs.jython.org/issue1313
    public final static String SIGHANDLER = 
//                        "import signal, thread, sys\n" +
                        "import signal, sys\n" +            
                        "def intHandler(signum, frame):\n" +
                        " print \"user pressed ctrl-c\"\n" +
                        " sys._jy_interpreter.interrupt(sys._jy_ch.getTs())\n" +            
                        " mth=sys._jy_ch.getMasterThread()\n" +  
                        " mth=mth.interrupt()\n" +  
//                        " print(thread.get_ident())\n" + 
//                        " th = sys._jy_ch.getCurrTs()\n" +
//                        " thp = sys._jy_ch.getTs()\n" +
//                        " print(th.thread.toString())\n" +
//                        " print(thp.thread.toString())\n" +
//                        " sys._jy_interpreter.interrupt(thp)\n" +
                        "signal.signal(signal.SIGINT, intHandler)\n";
    
    // threadstate of main execution thread - to be able to send interrupt signal
    private ThreadState ts;
    
    // master thread for interruption
    private Thread masterThread;
    
    // application instance
    private App appInstance;

    @Override
    public void debug(){
        System.out.println("Debug command started");
        System.out.println("EntityManager NotNull: " + (this.em==null ? "false":"true"));
        System.out.println("EntityManager isOpen: " + (this.em.isOpen() ? "true":"false"));   
    }
    
    /**
     * Executes scripts needed - autocomplete, sigint handler
     */
    @Override
    public void prepareConsoleBeforeStart(InteractiveConsole interp) {
        if (interp==null){
            throw new NullPointerException("Passed empty jython console instance");
        }
        
        // enable autocomplete by default:)
        interp.exec(AUTOCOMPLETE);
        
        // register exit routine
        interp.exec(EXITROUTINE);
        
        // unsuspend experiment
        interp.exec(UNSUSPENDROUTINE);
        
        // set custom SIGINT handler
        interp.exec(SIGHANDLER);
    }
    
    /**
     * On console restart
     */
    @Override
    public void consoleRestarted(){
        // store current threadState
        // IMPORTANT step - set thread state for main execution thread
        this.ts = Py.getThreadState();
        this.masterThread = Thread.currentThread();
    }
     
    @Override
    public InteractiveConsole getInterp() {
        return interp;
    }

    /**
     * Returns ThreadState for current workign thread (can be signal dispatcher 
     *  - different from main execution thread)
     * @return 
     */
    @Override
    public ThreadState getCurrTs(){
        return Py.getThreadState();
    }
    
    /**
     * ThreadState for main execution thread
     * @return 
     */
    @Override
    public synchronized ThreadState getTs() {
        return ts;
    }

    @Override
    public synchronized Thread getMasterThread() {
        return masterThread;
    }
    
    /**
     * Exit shell
     */
    @Override
    public synchronized void exitShell(){
        this.restartShell=false;
        
        if (this.ts!=null){
            this.interp.interrupt(ts);
        }
    }
    
    @Override
    public EntityManager getEm() {
        return em;
    }

    @Override
    public void setEm(EntityManager em) {
        this.em = em;
    }

    @Override
    public JdbcTemplate getTemplate() {
        return template;
    }

    @Override
    public void setTemplate(JdbcTemplate template) {
        this.template = template;
    }

    public static Logger getLog() {
        return log;
    }

    @Override
    public App getAppInstance() {
        return appInstance;
    }

    @Override
    public void setAppInstance(App appInstance) {
        this.appInstance = appInstance;
    }
}
