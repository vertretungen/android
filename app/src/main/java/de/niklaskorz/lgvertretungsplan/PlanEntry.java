package de.niklaskorz.lgvertretungsplan;

import android.webkit.JavascriptInterface;

import com.google.common.base.CharMatcher;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Created by niklaskorz on 15.04.15.
 */
public class PlanEntry {
    String classes; // Klasse(n)
    String hours; // Std.
    String subject; // (Fach)
    String substituteTeacher; // Vertreter
    String substituteSubject; // Fach
    String room; // Raum
    String type; // Art
    String text; // Vertretungs-Text
    String replaces; // Statt

    public PlanEntry(Element element) throws Plan.ParserException{
        Elements columns = element.select("td");
        if (columns.size() < 9) {
            throw new Plan.ParserException();
        }
        classes = PlanEntry.trim(columns.get(0).text());
        hours = PlanEntry.trim(columns.get(1).text());
        subject = PlanEntry.trim(columns.get(2).text());
        substituteTeacher = PlanEntry.trim(columns.get(3).text());
        substituteSubject = PlanEntry.trim(columns.get(4).text());
        room = PlanEntry.trim(columns.get(5).text());
        type = PlanEntry.trim(columns.get(6).text());
        text = PlanEntry.trim(columns.get(7).text());
        replaces = PlanEntry.trim(columns.get(8).text());
    }

    public static String trim(String s) {
        return CharMatcher.WHITESPACE.trimFrom(s.replace("---", ""));
    }
}
