package de.niklaskorz.lgvertretungsplan;

import android.util.Log;
import android.webkit.JavascriptInterface;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.TimeZone;

/**
 * Created by niklaskorz on 15.04.15.
 */
public class Plan {
    String userFullname;
    Date date;
    String dateString;
    Date lastUpdate;
    String lastUpdateString;
    ArrayList<PlanEntry> entries;

    public static class ParserException extends Exception {}

    public Plan(String raw) throws ParserException {
        Document document = Jsoup.parse(raw);

        loadMetadata(document);
        loadEntries(document);
    }

    private void loadMetadata(Document document) throws ParserException{
        Element nameElement = document.getElementById("right");
        if (nameElement == null) {
            throw new ParserException();
        }

        userFullname = nameElement.text();

        Element dateElement = document.getElementById("date");
        if (dateElement == null) {
            throw new ParserException();
        }

        dateString = dateElement.text();
        String[] components = dateString.split(" ");
        String[] dateComponents = components[components.length - 1].split("\\.");

        int day = Integer.parseInt(dateComponents[0]);
        int month = Integer.parseInt(dateComponents[1]) - 1;
        int year = Integer.parseInt(dateComponents[2]);

        Calendar calendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"));

        calendar.set(year, month, day);
        date = calendar.getTime();

        Element lastUpdateElement = document.getElementById("stand");
        if (lastUpdateElement == null) {
            throw new ParserException();
        }

        lastUpdateString = lastUpdateElement.text();
        components = lastUpdateString.split(" ");
        dateComponents = components[1].split("\\.");
        String[] timeComponents = components[2].split(":");

        day = Integer.parseInt(dateComponents[0]);
        month = Integer.parseInt(dateComponents[1]);
        year = Integer.parseInt(dateComponents[2]);
        int hours = Integer.parseInt(timeComponents[0]);
        int minutes = Integer.parseInt(timeComponents[1]);
        int seconds = Integer.parseInt(timeComponents[2]);

        calendar.set(year, month, day, hours, minutes, seconds);
        lastUpdate = calendar.getTime();
    }

    private void loadEntries(Document document) throws ParserException {
        Elements rows = document.select("#vertretungsplan tr");
        // There has to be at least one row: the head row
        if (rows.isEmpty()) {
            throw new ParserException();
        }
        Iterator<Element> rowIterator = rows.iterator();
        entries = new ArrayList<>();

        // Skip head row
        rowIterator.next();

        while (rowIterator.hasNext()) {
            Element row = rowIterator.next();
            entries.add(new PlanEntry(row));
        };
    }
}
