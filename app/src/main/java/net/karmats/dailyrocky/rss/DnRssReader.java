package net.karmats.dailyrocky.rss;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import net.karmats.dailyrocky.exceptions.RockyStripNotFoundException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class DnRssReader {

    private String dnRssUrl;

    /**
     * Creates a new instance of DnRssReader, with the dn rss url as argument.
     * 
     * @param dnRssUrl
     *            The url where the rss is
     */
    public DnRssReader(String dnRssUrl) {
        this.dnRssUrl = dnRssUrl;
    }

    /**
     * Fetch the ten latest rocky URL:s from the rss feed.
     * 
     * @return A map where the key is the number of days from today, and the value is the url.
     */
    public Map<Integer, String> fetchTenLatestUrls() throws RockyStripNotFoundException {
        Map<Integer, String> tenLatest = new HashMap<Integer, String>();
        try {
            URL aURL = new URL(dnRssUrl);
            URLConnection conn = aURL.openConnection();
            conn.connect();
            // Create new pull parser
            XmlPullParser pullParser = XmlPullParserFactory.newInstance().newPullParser();
            pullParser.setInput(aURL.openStream(), "UTF-8");
            int eventType = pullParser.getEventType();
            int count = 0;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (pullParser.getName().equals("content:encoded")) {
                        // Since the url is in a html we need to parse the html text
                        String url = fetchUrl(pullParser.nextText());
                        tenLatest.put(count, url);
                        count++;
                    }
                }
                try {
                    eventType = pullParser.nextTag();
                } catch (XmlPullParserException e) {
                    // Ignore not well formatted xml
                    eventType = pullParser.nextTag();
                }
            }
        } catch (Exception e) {
            return tenLatest;
        }
        return tenLatest;
    }

    // Fetch the rocky url source
    private String fetchUrl(String contentEncoded) throws RockyStripNotFoundException {
        try {
            XmlPullParser pullParser = XmlPullParserFactory.newInstance().newPullParser();
            pullParser.setInput(new ByteArrayInputStream(contentEncoded.getBytes()), "UTF-8");
            int eventType = pullParser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (pullParser.getName().equals("a")) {
                        return pullParser.getAttributeValue(null, "href");
                    }
                }
                try {
                    eventType = pullParser.nextTag();
                } catch (XmlPullParserException e) {
                    // Ignore not well formatted html
                    eventType = pullParser.nextTag();
                }
            }
        } catch (Exception e) {
            return null;
        }
        // If we have come here we haven't found the strip
        return null;
    }

}
