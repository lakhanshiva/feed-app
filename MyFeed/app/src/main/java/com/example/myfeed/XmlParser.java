package com.example.myfeed;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class XmlParser {

    private static final String ns = null;

    public List<Entry> analyze(InputStream in) throws XmlPullParserException, IOException {
        try {
            //XMLPullParser
            XmlPullParser parser = Xml.newPullParser();
            //No namespaces are used
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            //Set entry
            parser.setInput(in, null);
            //We get the first tag
            parser.nextTag();
            //Return the list of news
            return readNews(parser);
        } finally {
            in.close();
        }
    }

    //Read a list of news from the parser and return a list of Posts
    private List<Entry> readNews(XmlPullParser parser) throws XmlPullParserException, IOException {
        List<Entry> listItems = new ArrayList<Entry>();
        //Check if the tag is of the type "channel"
        parser.nextTag();
        parser.require(XmlPullParser.START_TAG, ns, "channel");

        // While we do not reach the end of the tag
        while (parser.next() != XmlPullParser.END_TAG) {
            //Ignore all events that are not a labeled with START_TAG
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                //Jump to the next event
                continue;
            }
            // We get the name of the tag
            String name = parser.getName();
            //  If this tag is a news entry
            if (name.equals("item")) {
                // Add the entry to the list
                listItems.add(readItem(parser));
            } else {
                skipTag(parser);
            }
        }
        return listItems;
    }

    // This function serves to skip a tag and its nested sub tags.
    private void skipTag(XmlPullParser parser) throws XmlPullParserException, IOException {
        // If it is not a start tag then ERROR
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;

        // Check that it has gone through so many start labels as end tag

        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    // Every time a label is closed decrement depth counter
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    // Every time a label is opened increment depth counter
                    depth++;
                    break;
            }
        }
    }

    // Analyze the content of an entry. If you find a topic, summary or link, call the reading methods
    // to process them. If not, ignore the tag.
    private Entry readItem(XmlPullParser parser) throws XmlPullParserException, IOException {
        String title = null;
        String link = null;
        String author = null;
        String description = null;
        String date = null;
        String image = null;

        //The current tag must be "item"
        parser.require(XmlPullParser.START_TAG, ns, "item");

        // While the "item" tag is not finished
        while (parser.next() != XmlPullParser.END_TAG) {
            // Ignore until we can not find a label start
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            // We get the name of the tag
            String tagName = parser.getName();
            if (tagName.equals("title")) { // news title
                title = readTag(parser, "title");
            } else if (tagName.equals("description")) { // summary
                description = readTag(parser, "description");
            } else if (tagName.equals("link")) { // link
                link = readTag(parser, "link");
            } else if (tagName.equals("pubDate")) { // date
                date = readTag(parser, "pubDate");
            } else if (tagName.equals("dc:creator")) { // author
                author = readTag(parser, "dc:creator");
            } else if (tagName.equals("media:thumbnail")) { // image
                image = readImage(parser);
            } else {
                // we skip other labels
                skipTag(parser);
            }
        }
        // We create a new entry with these data and return it
        return new Entry(title, description, link, author, date, image);
    }

    private String readTag(XmlPullParser parser, String tagName) throws IOException, XmlPullParserException {
        // Start tag - "pubDate"
        parser.require(XmlPullParser.START_TAG, ns, tagName);
        // read
        String content = readText(parser);
        // End tag
        parser.require(XmlPullParser.END_TAG, ns, tagName);
        return content;
    }

    private String readImage(XmlPullParser parser) throws IOException, XmlPullParserException {
        // Start tag -  "media:thumbnail"
        parser.require(XmlPullParser.START_TAG, ns, "media:thumbnail");
        // Read URL attribute
        String image = parser.getAttributeValue(null, "url");
        // Does not end
        parser.next();
        return image;
    }

    //  Extract the text for the title tags, summary
    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";

        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    //This class represents a news entry of the RSS Feed
    public static class Entry implements Serializable {
        public final String title;
        public final String link;
        public final String author;
        public final String description;
        public final String date;
        public final String image;

        public Entry(String title, String description, String link, String author, String date, String image) {
            this.title = title;
            this.description = description;
            this.link = link;
            this.author = author;
            this.date = date;
            this.image = image;
        }
    }

}
