/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.console;

import fi.wsnusbcollect.App;
import javax.persistence.EntityManager;
import org.python.core.ThreadState;
import org.python.util.InteractiveConsole;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 *
 * @author ph4r05
 */
public interface ConsoleHelper {

    /**
     * On console restart
     */
    void consoleRestarted();

    void debug();

    /**
     * Exit shell
     */
    void exitShell();

    App getAppInstance();

    /**
     * Returns ThreadState for current workign thread (can be signal dispatcher
     * - different from main execution thread)
     * @return
     */
    ThreadState getCurrTs();

    EntityManager getEm();

    InteractiveConsole getInterp();

    JdbcTemplate getTemplate();

    /**
     * ThreadState for main execution thread
     * @return
     */
    ThreadState getTs();
    
    Thread getMasterThread();

    /**
     * Executes scripts needed - autocomplete, sigint handler
     */
    void prepareConsoleBeforeStart(InteractiveConsole interp);

    void setAppInstance(App appInstance);

    void setEm(EntityManager em);

    void setTemplate(JdbcTemplate template);
}
