package main;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.solr.client.solrj.SolrServerException;
import servlets.SearchHandler;

/**
 *
 * @author csd3829
 */
public class Engine {

    static ArrayList<Documents> docs = new ArrayList();

    static public void main(String[] args) throws ClassNotFoundException, SolrServerException, IOException, SQLException, InterruptedException {

        System.out.println("Checking Systems...");
        System.out.println("All systems operational");

        TimerTask repeatedTask = new TimerTask() {
            public void run() {
                try {
                    utilities.updateSolrDatabase(docs);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SolrServerException ex) {
                    Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        Timer timer = new Timer("Timer");

        long delay = 1000L;
        long period = 1000L * 60L * 60L * 12L; //ms * s * m * h
        timer.scheduleAtFixedRate(repeatedTask, delay, period);

        // Create a separate thread for user input or other concurrent tasks
        Thread userInputThread = new Thread(new Runnable() {
            public void run() {
                try {
                    System.out.println("\n\nService Up\n");
                    HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
                    server.createContext("/search", new SearchHandler());
                    server.setExecutor(null);
                    server.start();
                    System.out.println("\n\nWaiting for a request...\n");
                } catch (IOException ex) {
                    Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        userInputThread.start();
    }
}
