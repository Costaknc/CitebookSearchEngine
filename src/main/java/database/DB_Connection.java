package database;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import main.Documents;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.impl.XMLResponseParser;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

/**
 *
 * @author csd3829
 */
public class DB_Connection {

    private static final String url = "jdbc:mysql://localhost";
    private static final String databaseName = "citebookdb";
    private static final int port = 3306;
    private static final String username = "root";
    private static final String password = "";

    /**
     * Attempts to establish a database connection
     *
     * @return a connection to the database
     * @throws SQLException
     * @throws java.lang.ClassNotFoundException
     */
    public static java.sql.Connection getConnection() throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.jdbc.Driver");
        return DriverManager.getConnection(url + ":" + port + "/" + databaseName, username, password);
    }

    public static java.sql.Connection getInitialConnection() throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.jdbc.Driver");
        return DriverManager.getConnection(url + ":" + port, username, password);
    }

    public static void Upload(Documents doc) throws SolrServerException, IOException {
        System.out.println("Uploading...");
        String urlString = "http://localhost:8983/solr/CitebookSolr";
        SolrClient solrClient = new HttpSolrClient.Builder(urlString).build();

        String text = new String();

        if (doc.getFile() != null) {
            try ( PDDocument document = PDDocument.load(doc.getFile())) {
                PDFTextStripper textStripper = new PDFTextStripper();
                text = textStripper.getText(document);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            text = null;
        }

        System.out.println("Entering '" + doc.getTitle() + "' in Solr");

        SolrInputDocument solrDoc = new SolrInputDocument();
        String metadata = doc.getTitle() + " " + doc.getAuth() + " " + doc.getDes() + " " + doc.getKey();
        solrDoc.setField("title", doc.getTitle());
        solrDoc.setField("authors", doc.getAuth());
        solrDoc.setField("abstract", doc.getDes());
        solrDoc.setField("keywords", doc.getKey());

        solrDoc.setField("metadata", metadata);
        solrDoc.setField("content", text);

        solrClient.add(solrDoc);
        solrClient.commit();

        System.out.println("Done");
    }

    static public ArrayList<String> SendQuery(String term) throws SolrServerException, IOException {
        ArrayList<String> titles = new ArrayList<String>();
        String urlString = "http://localhost:8983/solr/CitebookSolr";

        HttpSolrClient solr = new HttpSolrClient.Builder(urlString).build();
        solr.setParser(new XMLResponseParser());

        String queryBuild = "metadata: " + term;

        SolrQuery query = new SolrQuery();
        query.setQuery(queryBuild);

        query.setStart(0);
        query.setRows(10);

        QueryResponse response = solr.query(query);
        SolrDocumentList docList = response.getResults();

        System.out.println("\n\nQuery Results (Found " + docList.getNumFound() + " documents): ");

        for (SolrDocument doc : docList) {
            System.out.println(new String(doc.getFieldValue("title").toString().getBytes("ISO-8859-7"), StandardCharsets.UTF_8));
            titles.add(new String(doc.getFieldValue("title").toString().getBytes("ISO-8859-7"), StandardCharsets.UTF_8));
        }

        queryBuild = "content: " + term;

        query.setQuery(queryBuild);

        query.setStart(0);
        query.setRows(10);

        response = solr.query(query);
        docList = response.getResults();

        System.out.println("\n\nQuery Results (Found " + docList.getNumFound() + " documents): ");

        int check = 0;

        for (SolrDocument doc : docList) {
            byte[] inputBytes = doc.getFieldValue("title").toString().getBytes("ISO-8859-7");
            String convertedText = new String(inputBytes, StandardCharsets.UTF_8);
            System.out.println(convertedText);

            check = 0;

            for (String title : titles) {
                if (title.equals(doc.getFieldValue("title"))) {
                    System.out.println("File '" + doc.getFieldValue("title") + "' has already been delivered");
                    check = 1;
                }
            }
            if (check == 0) {
                System.out.println("Adding file");
                titles.add(convertedText);
            }
        }
        solr.close();

        return titles;
    }
}
