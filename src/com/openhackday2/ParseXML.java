package com.openhackday2;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;
import android.util.Xml;

public class ParseXML {
	private XmlPullParser xmlPullParser;
	private ArrayList<HashMap<String, String>> points;
	
	public String getItemValue(String key) {
		String value = null;
		if (points != null && points.size() > 0) {
			value = points.get(0).get(key);
		}
		return value;
	}

	public void parse(String xmlStr) {
		xmlPullParser = Xml.newPullParser();
		try {
			xmlPullParser.setInput(new StringReader(xmlStr));
			HashMap<String, String> point = new HashMap<String, String>();
			int eventType = xmlPullParser.getEventType();
			boolean done = false;
			boolean hasUrl = false;
			while (eventType != XmlPullParser.END_DOCUMENT && !done) {
				String name = null;
				switch (eventType) {
				case XmlPullParser.START_DOCUMENT:
					points = new ArrayList<HashMap<String, String>>();
					break;
				case XmlPullParser.START_TAG:
					name = xmlPullParser.getName();
					if (name.equalsIgnoreCase("ITEMID")) {	
						point.put("itemid",
								xmlPullParser.nextText());
					} else if (name.equalsIgnoreCase("LARGEIMAGE") && !hasUrl) {
						while ((eventType = xmlPullParser.next()) > XmlPullParser.END_DOCUMENT) {
							if (eventType == XmlPullParser.START_TAG) {
								String childname = xmlPullParser.getName();
								if (childname.equals("URL")) {
									hasUrl = true;
									break;
								}// ループ脱出
							}
						}
						point.put("imageurl", xmlPullParser.nextText());
					}
					break;
				case XmlPullParser.END_TAG:
					name = xmlPullParser.getName();
					if (name.equalsIgnoreCase("ITEMID") && point != null) {
						points.add(point);
					} else if (name.equalsIgnoreCase("LARGEIMAGE")) {
						points.add(point);
					} else if (name.equalsIgnoreCase("TEEMLOOKUPRESPONSE")) {
						done = true;
					}
					break;
				default:
					break;
				}
				eventType = xmlPullParser.next();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
