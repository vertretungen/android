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
    Date date;
    String dateString;
    Date lastUpdate;
    String lastUpdateString;
    ArrayList<PlanEntry> entries;

    private Plan(Document document) {
        loadMetadata(document);
        loadEntries(document);
    }

    private void loadMetadata(Document document) {
        Element dateElement = document.getElementById("date");
        dateString = dateElement.text();
        String[] components = dateString.split(" ");
        String[] dateComponents = components[components.length - 1].split("\\.");

        int day = Integer.parseInt(dateComponents[0]);
        int month = Integer.parseInt(dateComponents[1]);
        int year = Integer.parseInt(dateComponents[2]);

        Calendar calendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"));

        calendar.set(year, month, day);
        date = calendar.getTime();

        Element lastUpdateElement = document.getElementById("stand");
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

    private void loadEntries(Document document) {
        Elements rows = document.select("#vertretungsplan tr");
        Iterator<Element> rowIterator = rows.iterator();
        entries = new ArrayList<>();

        // Skip head row
        rowIterator.next();

        while (rowIterator.hasNext()) {
            Element row = rowIterator.next();
            entries.add(new PlanEntry(row));
        };
    }

    public static Plan parse(String source) {
        return new Plan(Jsoup.parse(source));
    }
}
