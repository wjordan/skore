package net.tetromi.score;

import nanoxml.XMLElement;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * @author will
 * @date Mar 8, 2009 7:26:11 AM
 */
public class ScoreEntry {
    private final int score;
    private final Date startTime;
    private final Date finishTime;
    private final Properties properties;

    // Static helper method to create a ScoreEntry from a XML string input.
    public static List<ScoreEntry> parse(String xml) {
        XMLElement xmlElem = new XMLElement();
        xmlElem.parseString(xml);
        List<ScoreEntry> scoreList = new ArrayList<ScoreEntry>();
        for(Object o : xmlElem.getChildren()) {
            XMLElement xe = (XMLElement) o;
            final String name = xe.getName();
            if(name.equals("entry")) {
                scoreList.add(new ScoreEntry(xe));
            }
        }
        return scoreList;
    }

    // Create a ScoreEntry from an "entry" XML element
    public ScoreEntry(XMLElement xml) {
        int score = 0;
        Date finishTime = new Date();
        Date startTime = new Date();
        Properties properties = new Properties();

        for(Object o : xml.getChildren()) {
            XMLElement xe = (XMLElement) o;
            final String name = xe.getName();
            final String val = xe.getContent();
            if(name.equals("score")) {
                score = Integer.parseInt(val);
            } else if(name.equals("startTime")) {
                try {
                    startTime = DateFormat.getInstance().parse(val);
                } catch (Exception ignore) {}
            } else if(name.equals("finishTime")) {
                try {
                    finishTime = DateFormat.getInstance().parse(val);
                } catch (Exception ignore) {}
            } else if(name.equals("properties")) {
                for(Object o2 : xe.getChildren()) {
                    XMLElement prop = (XMLElement) o2;
                    if(prop.getName().equals("property")) {
                        properties.setProperty(prop.getStringAttribute("name"),prop.getStringAttribute("value"));
                    }
                }
            }
        }

        this.score = score;
        this.finishTime = finishTime;
        this.startTime = startTime;
        this.properties = properties;
    }

    public int getScore() {
        return score;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getFinishTime() {
        return finishTime;
    }

    public Properties getProperties() {
        return properties;
    }
}
