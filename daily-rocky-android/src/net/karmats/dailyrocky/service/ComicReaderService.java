package net.karmats.dailyrocky.service;

import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import net.karmats.dailyrocky.constant.ComicType;
import net.karmats.dailyrocky.exceptions.RockyStripNotFoundException;
import net.karmats.dailyrocky.rss.DnRssReader;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class ComicReaderService {

    public static final String ROCKY_BLACK_WHITE_URL = "http://www.scandinavian-comics.com/strips/rocky-sv/";
    public static final String ROCKY_URL = "http://www.scandinavian-comics.com/strips/rocky/";

    public static final String DN_ROCKY_RSS_URL = "http://www.dn.se/blogg/pa-stan/kategori/rocky/feed/";

    private static final String IMG_ALT_ATTR = "Dagens Rocky";

    private final SimpleDateFormat urlDateFormat = new SimpleDateFormat("yyyy/MM/dd");

    private static Map<Integer, String> tenLatestUrls;

    private static ComicReaderService instance;

    private ComicReaderService() {
    }

    public static ComicReaderService getInstance() {
        if (instance == null) {
            instance = new ComicReaderService();
        }
        return instance;
    }

    /**
     * Get the rocky strip image url based on a date and comic type
     * 
     * @param date
     *            The date
     * @param comicType
     *            The comic type
     * @return A string url
     * @throws RockyStripNotFoundException
     *             If the strip wasn't found
     */
    public String getRockyUrl(Date date, ComicType comicType) throws RockyStripNotFoundException {
        switch (comicType) {
        case BLACK_WHITE:
            return getRockyBlackWhiteUrl(date);
        case COLOR:
            return getRockyColorUrl(date);
        default:
            return getRockyBlackWhiteUrl(date);
        }
    }

    /**
     * Get rocky black white url
     * 
     * @param date
     *            The date to get the strip for
     * @return An url
     * @throws RockyStripNotFoundException
     */
    private String getRockyBlackWhiteUrl(Date date) throws RockyStripNotFoundException {
        // Check how many days from today, since the rss only takes the 10 latest rocky strips
        Calendar today = resetHourMinutes(new Date());
        Calendar stripDate = resetHourMinutes(date);
        stripDate.setTime(date);
        int daysBetween = 0;
        while (stripDate.before(today)) {
            stripDate.add(Calendar.DATE, 1);
            daysBetween++;
        }
        if (daysBetween >= 10) {
            String rockyUrl = ROCKY_BLACK_WHITE_URL + urlDateFormat.format(date);
            return fetchScandinavianComicsUrl(rockyUrl);
        } else {
            if (tenLatestUrls == null) {
                DnRssReader rssReader = new DnRssReader(DN_ROCKY_RSS_URL);
                tenLatestUrls = rssReader.fetchTenLatestUrls();
            }
            String urlToReturn = tenLatestUrls.get(daysBetween);
            if (urlToReturn != null) {
                return urlToReturn;

            }
            // If the url is null, try to get from scandinavian comics
            return fetchScandinavianComicsUrl(ROCKY_BLACK_WHITE_URL + urlDateFormat.format(date));
        }
    }

    /**
     * Get the color rocky url
     * 
     * @param date
     *            The date to get the strip for
     * @return An url
     * @throws RockyStripNotFoundException
     */
    private String getRockyColorUrl(Date date) throws RockyStripNotFoundException {
        String rockyUrl = ROCKY_URL + urlDateFormat.format(date);
        return fetchScandinavianComicsUrl(rockyUrl);
    }

    private String fetchScandinavianComicsUrl(String url) throws RockyStripNotFoundException {
        try {
            URL aURL = new URL(url);
            URLConnection conn = aURL.openConnection();
            conn.connect();
            // Create new pull parser
            XmlPullParser pullParser = XmlPullParserFactory.newInstance().newPullParser();
            pullParser.setInput(aURL.openStream(), "UTF-8");
            int eventType = pullParser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (pullParser.getName().equals("img") && pullParser.getAttributeValue(null, "alt").startsWith(IMG_ALT_ATTR)) {
                        return pullParser.getAttributeValue(null, "src");
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
            throw new RockyStripNotFoundException("The rocky strip could not be found, reson: \n" + e.getMessage());
        }
        // If we have come here we haven't found the strip
        throw new RockyStripNotFoundException("Rocky strip not found, came to the end of method");
    }

    private Calendar resetHourMinutes(Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.set(Calendar.HOUR, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c;
    }

}
