import net.tetromi.score.HighScore;
import net.tetromi.score.ScoreEntry;

import java.util.List;
import java.util.Properties;
import java.util.Random;

/**
 * Example application to test out the high score API.
 *
 * @author will
 * @date Feb 28, 2009 9:02:59 PM
 */
public class TestHighScore {

    /** Hard-coded identifier of this game's high score table. */
    private static final String id = "TestSkore";

    /** Hard-coded passcord unique to this game's high score table. */
    private static final String password = "password";

    public static void main(String[] args) throws Exception {
        // Set a custom server URL
        HighScore.setServer("http://skore.wjordan.staxapps.net");

        // Create the new high score chart (usually do this outside of the application)
        HighScore.create(id,password);

        // Instantiate a high score session (retrieves a one-time-use HTTP token from the server)
        final HighScore score = new HighScore(id, password);

        // Sets the score using an internal EncryptedInt
        score.setScore(new Random().nextInt(1000000));

        // Set some custom properties we want to submit along with our score
        Properties p = new Properties();
        p.load(TestHighScore.class.getResourceAsStream("score.properties"));

        // Performs the actual score submission
        score.submit(p);
        System.out.println("score.getScore() = " + score.getScore());

        final List<ScoreEntry> out = HighScore.list(id);
        for(ScoreEntry entry : out) {
            System.out.println("score = " + entry.getScore());
            System.out.println("start time = " + entry.getStartTime());
            System.out.println("finish time = " + entry.getFinishTime());
            System.out.println("properties = " + entry.getProperties());
        }

        System.out.println("Done!");
    }
}
