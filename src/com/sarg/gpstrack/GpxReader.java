package com.sarg.gpstrack;

import java.io.File;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.google.android.maps.GeoPoint;

public class GpxReader extends DefaultHandler {
    private List<GeoPoint> track = new ArrayList<GeoPoint>();
    private StringBuffer buf = new StringBuffer();
    private double lat;
    private double lon;

    public static GeoPoint[] readTrack(InputStream in) throws IOException {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            //factory.setValidating(true);
            SAXParser parser = factory.newSAXParser();
            GpxReader reader = new GpxReader();
            parser.parse(in, reader);
            return reader.getTrack();
        } catch (ParserConfigurationException e) {
            throw new IOException(e.getMessage());
        } catch (SAXException e) {
            throw new IOException(e.getMessage());
        }
    }

    public static GeoPoint[] readTrack(File file) throws IOException {
        InputStream in = new FileInputStream(file);
        try {
            return readTrack(in);
        } finally {
            in.close();
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        buf.setLength(0);
        if (localName.equals("trkpt")) {
            lat = Double.parseDouble(attributes.getValue("lat"));
            lon = Double.parseDouble(attributes.getValue("lon"));
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (localName.equals("trkpt")) {
            track.add(new GeoPoint((int) (lat*1E6), (int) (lon*1E6)));
        }
    }

    @Override
    public void characters(char[] chars, int start, int length)
            throws SAXException {
        buf.append(chars, start, length);
    }

    private GeoPoint[] getTrack() {
        return track.toArray(new GeoPoint[track.size()]);
    }
}
