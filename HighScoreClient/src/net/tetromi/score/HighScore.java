package net.tetromi.score;

import net.tetromi.log.LOG;
import net.tetromi.pulpcore.net.Upload;
import static util.CryptUtil.getKey;
import static util.CryptUtil.hash;
import util.EncryptedInt;
import static util.StreamUtil.readStream;
import static util.StringUtil.*;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Properties;

/**
 * @author will
 * @date Feb 28, 2009 4:23:02 PM
 */
public class HighScore {
    private final String id;
    /** Permanent key for this highscore table. */
    private final byte[] key;
    /** Single-use token for this play session. */
    private final byte[] token;

    /** Score (encrypted integer to prevent memory hack). */
    private final EncryptedInt score;

    public String getId() {
        return id;
    }

    // Set the default server location.
    private static URL server;
    static {
        try {
            server = new URL("http://localhost:8080/");
        } catch (MalformedURLException ignore) {}
    }

    /** Sets the server URL to a custom location. */
    public static void setServer(String URL) {
        try {
            server = new URL(URL);
        } catch (MalformedURLException ignore) {}
    }

    /**
     * Instantiates a new HighScore session, with the specified id and password
     * as authentication.
     * If this is the first time this id is accessed, a new high score chart
     * will be created. Otherwise an exception is thrown if the password
     * does not match the original.
     */
    public HighScore(String id, String password) throws IOException {
        this.id = id;
        this.key = getKey(password);
        URL restUrl = new URL(server,id);
        String str = getToken(restUrl);
        LOG.debug("New token = " + str);
        token = dec64(str);
        score = new EncryptedInt(this.key,0);
    }

    /**
     * Creates a new HighScore Chart with the specified id and password.
     * Initiates HTTP PUT http://{server}/{id}, k={password}
     */
    public static void create(String id, String password) throws IOException {
        final URL url = new URL(server, id);
        final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("k", password);
        conn.setRequestProperty("Content-Length","0");
        if (conn.getResponseCode() != 200) LOG.warn("Error creating chart.");
    }

    private String getToken(URL url) throws IOException {
        final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Length","0");
        conn.setRequestProperty("Content-Type","text/plain");
        return toStr(readStream(conn.getInputStream()));
    }

    private String post(String extraInfo) throws IOException {
        final URL url1 = new URL(server, id);
        final byte[] sc = score.getSecure();
        Upload u = new Upload(url1);
        final Properties props = new Properties();
        props.setProperty("s", enc64(sc));
        props.setProperty("t", enc64(hash(sc, key, token)));
        props.setProperty("k", enc64(token));
        u.addRequestFields(props);
        u.addField("p",extraInfo);
        u.start(false);
        if(u.getResponseCode() != 200) throw new IOException("Server returned error "+
                u.getResponseCode()+": "+u.getResponse());
        return u.getResponse();
    }

    /** Sets the securely stored score variable. */
    public void setScore(int s) {
        score.set(s);
    }

    /** @return score from the encrypted memory variable. */
    public int getScore() {
        return score.get();
    }

    /** Submits the current high score to the server. Blocks until submission completes.
     * @param extraInfo Extra set of custom properties to record eg. 'name=x, ip=y',
     * or null for no extra properties.
     * @throws java.io.IOException if there was an error submitting the high score. */
    public void submit(Properties extraInfo) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final String s1;
        if(extraInfo != null) {
            extraInfo.store(baos,"Scorechart "+id);
            baos.flush();
             s1 = baos.toString("UTF-8");
        } else s1 = "";
        post(s1);
    }

    /**
     * Retrieves a high score list from the server.
     * The list is retrieved via XML and converted into a list of ScoreEntry objects.
     * @param id name of the score chart.
     * @return List of ScoreEntry objects.
     * @throws IOException if there was any problem retrieving the list.
     */
    public static List<ScoreEntry> list(String id) throws IOException {
        final URL url = new URL(server, id);
        final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept","application/xml");
        if (conn.getResponseCode() != 200) throw new IOException("Error getting chart list.");

        final String xml = getResponse(conn);
        return ScoreEntry.parse(xml);
    }

    private static String getResponse(HttpURLConnection conn) throws IOException {
        // Get response data.
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuffer responseBuffer = new StringBuffer();

        char[] buffer = new char[1024];
        while (true) {
            int charsRead = in.read(buffer);
            if (charsRead == -1) {
                break;
            }
            responseBuffer.append(buffer, 0, charsRead);
        }

        in.close();
        return responseBuffer.toString();
    }
}
