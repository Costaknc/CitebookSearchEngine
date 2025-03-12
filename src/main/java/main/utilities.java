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
}
