package fi.wsnusbcollect;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.ExampleMode;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hello world!
 *
 */
public class App {

    private static final Logger log = LoggerFactory.getLogger(App.class);
    // receives other command line parameters than options
    @Argument
    private List<String> arguments = new ArrayList<String>(8);
    
    @Option(name = "--debug", usage = "enables debug output")
    private boolean debug;
    
    @Option(name = "--detect-nodes", usage = "performs node detection")
    private boolean detectNodes;
    
    @Option(name = "--check-nodes-connection", usage = "checks whether node connection corresponds to DB settings")
    private boolean checkNodesConnection;
    
    @Option(name = "-c", usage = "read configuration from this config file")
    private File configFile = null;

    public static void main(String[] args) {
        log.info("Starting application");
        try {
            // some inits in static scope
            // ...

            // do main on instance
            new App().doMain(args);

            // ending application
            // ... 

        } catch (IOException ex) {
            log.error("Exception thrown: ", ex);
        } catch (CmdLineException ex) {
            log.error("Error in processing command line arguments", ex);
        } catch (RuntimeException ex) {
            log.error("Runtime exception occurred", ex);
        } catch (Exception ex) {
            log.error("Generic exception occurred", ex);
        }


        log.info("Everything OK, exiting");
    }

    public void doMain(String[] args) throws IOException, CmdLineException {
        CmdLineParser parser = new CmdLineParser(this);

        // if you have a wider console, you could increase the value;
        // here 80 is also the default
        parser.setUsageWidth(80);

        try {
            // parse the arguments.
            parser.parseArgument(args);

            // you can parse additional arguments if you want.
            //parser.parseArgument("include","mote");

            // after parsing arguments, you should check
            // if enough arguments are given.
//            if (arguments.isEmpty()) {
//                throw new CmdLineException("No argument is given");
//            }

        } catch (CmdLineException e) {
            // if there's a problem in the command line,
            // you'll get this exception. this will report
            // an error message.
            System.err.println(e.getMessage());
            System.err.println("java SampleMain [options...] arguments...");
            // print the list of available options
            parser.printUsage(System.err);
            System.err.println();

            // print option sample. This is useful some time
            System.err.println(" Example: java SampleMain" + parser.printExample(ExampleMode.ALL));

            return;
        }

        // this will redirect the output to the specified output
        //System.out.println(out);

        if (debug) {
            System.out.println("-d flag is set");
        }

        if (detectNodes) {
            System.out.println("detectNodes flag is set");
        }

        if (checkNodesConnection) {
            System.out.println("checkNodesConnection flag is set");
        }

        if (configFile != null && (configFile instanceof File)) {
            System.out.println("Config file set: " + configFile.getName());
        }

//        if( data )
//            System.out.println("-custom flag is set");
//
//        System.out.println("-str was "+str);
//
//        if( num>=0 )
//            System.out.println("-n was "+num);

        // access non-option arguments
        System.out.println("other arguments are:");
        for (String s : arguments) {
            System.out.println(s);
        }
    }
}
