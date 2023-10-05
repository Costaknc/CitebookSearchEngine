package main;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import database.DB_Connection;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.apache.solr.client.solrj.SolrServerException;

/**
 *
 * @author csd3829
 */
public class utilities {

    public static JsonObject getJSON(HttpServletRequest request) throws IOException {
        String json = "";
        String temp = null;
        BufferedReader reader = request.getReader();
        while ((temp = reader.readLine()) != null) {
            json += temp;
        }
        return new Gson().fromJson(json, JsonObject.class);
    }

    public static String jsonAsString(JsonObject jsonObj, String key) {
        JsonElement jelem = jsonObj.get(key);
        if (jelem == null) {
            return null;
        }
        return jelem.getAsString();
    }

    public static ArrayList<String> getFileFromSolr(String query) throws SolrServerException, IOException {

        ArrayList<String> titles = new ArrayList<String>();

        String finalQuery;
        byte[] bytes;

        bytes = query.getBytes("ISO-8859-7");
        finalQuery = new String(bytes, "ISO-8859-7");

        System.out.println("\nSearching for " + finalQuery);

        titles = DB_Connection.SendQuery(finalQuery);

        return titles;
    }

    private static void closeDBConnection(Statement stmt, Connection con) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException ex) {
                Logger.getLogger(utilities.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (con != null) {
            try {
                con.close();
            } catch (SQLException ex) {
                Logger.getLogger(utilities.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static void updateSolrDatabase(ArrayList<Documents> docs) throws ClassNotFoundException, SolrServerException, IOException {
        System.out.println("Updating...");

        Statement stmt = null;
        Connection con = null;

        Documents newDoc = null;
        ArrayList<Documents> tmpList = new ArrayList();
        String query = "SELECT * FROM posts;";

        String title;
        String authors;
        String descr;
        String keywords;
        File file;

        // we get all the files from citebook into a temp list
        try {
            con = DB_Connection.getConnection();

            stmt = con.createStatement();

            stmt.execute(query);

            ResultSet res = stmt.getResultSet();

            while (res.next() == true) {
                title = res.getString("title");
                authors = res.getString("authors");
                descr = res.getString("description");
                keywords = res.getString("keywords");
                if (res.getString("file_path").equals("null")) {
                    file = null;
                } else {
                    file = new File(res.getString("file_path"));
                }

                newDoc = new Documents(title, authors, descr, keywords, file);
                tmpList.add(newDoc);
            }
        } catch (SQLException ex) {
            Logger.getLogger(utilities.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            closeDBConnection(stmt, con);
        }

//        for debugging
//        System.out.println("CITEBOOK CONTENTS:");
//        printList(tmpList);
//
//        System.out.println("Solr CONTENTS:");
//        printList(docs);

        // we check that list against the citebook docs list
        for (int x = 0; x < tmpList.size(); x++) {
            int check = 0;
            for (int y = 0; y < docs.size(); y++) {
                if (tmpList.get(x).getTitle().equals(docs.get(y).getTitle())) {
                    check = 1;
                }
            }
            if (check == 0) {
                docs.add(tmpList.get(x));
                DB_Connection.Upload(tmpList.get(x));
            }
        }
    }

    public static void printList(ArrayList<Documents> docs) {
        for (int x = 0; x < docs.size(); x++) {
            System.out.println(x + ") Title: " + docs.get(x).getTitle());
        }
    }

// <editor-fold defaultstate="collapsed" desc="Some debugging methods. Click on the + sign on the left to edit the code.">
//curl http://localhost:8983/solr/CitebookSolr/update?commit=true -d "<delete><query>*:*</query></delete>"
//    public static String sendQueryToCitebook() throws ClassNotFoundException {
//        Statement stmt = null;
//        Connection con = null;
//        String title = new String();
//        Scanner myObj = new Scanner(System.in);
//        String query;
//
//        System.out.println("Enter query: ");
//        query = myObj.nextLine();
//
//        try {
//            con = DB_Connection.getConnection();
//
//            stmt = con.createStatement();
//
//            stmt.execute(query);
//
//            ResultSet res = stmt.getResultSet();
//
//            while (res.next() == true) {
//                title = res.getString("title");
//                System.out.println("Title returned: " + title);
//            }
//
//        } catch (SQLException ex) {
//            Logger.getLogger(utilities.class.getName()).log(Level.SEVERE, null, ex);
//        } finally {
//            closeDBConnection(stmt, con);
//        }
//
//        return title;
//    }
//
//    public static void getFileFromSolr() throws SolrServerException, IOException {
//        Scanner myObj;
//
//        String query;
//        String finalQuery;
//        byte[] bytes;
//
//        myObj = new Scanner(System.in);
//
//        while (true) {
//            System.out.println("\nEnter query: ");
//
//            query = myObj.nextLine();
//
//            bytes = query.getBytes("ISO-8859-7");
//            finalQuery = new String(bytes, "ISO-8859-7");
//
//            if (query.equals("exit")) {
//                break;
//            }
//
//            System.out.println("Searching for " + finalQuery);
//
//            DB_Connection.SendQuery(finalQuery);
//        }
//    }
//
//    public static void uploadFileToSolr() throws SolrServerException, IOException {
//        File tmp = new File("C:\\Users\\STALKER\\Desktop\\CSD\\Diploma Thesis\\Thesis_Report_csd4082.pdf");
//        Documents testDoc = new Documents("Test Document", "Konstantinos", "Just a test nothing more", "#test, #testing", tmp);
//
//        ArrayList<Documents> doc_list = new ArrayList();
//
//        DB_Connection.Upload(testDoc);
//        doc_list.add(testDoc);
//
//        for (int x = 0; x < doc_list.size(); x++) {
//            System.out.println("Title: " + doc_list.get(x).getTitle() + " | Author:  " + doc_list.get(x).getAuth() + " | Abstract:  " + doc_list.get(x).getDes()
//                    + "| Keywords:  " + doc_list.get(x).getKey() + " | Path:  " + doc_list.get(x).getPath());
//        }
//
//    }
// </editor-fold>
}
