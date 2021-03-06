package in.nimbo.moama.crawler;

import in.nimbo.moama.document.Link;
import in.nimbo.moama.UrlHandler;
import in.nimbo.moama.document.WebDocument;
import in.nimbo.moama.crawler.language.LangDetector;
import in.nimbo.moama.exception.IllegalLanguageException;
import in.nimbo.moama.metrics.FloatMeter;
import in.nimbo.moama.metrics.IntMeter;
import org.jsoup.nodes.Document;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;


public class Parser {
    private static LangDetector langDetector;
    private static Parser ourInstance=new Parser();
    private static final IntMeter CRAWLED_PAGE_METER =new IntMeter("crawled page");
    private static final IntMeter LANGUAGE_PASSED_METER =new IntMeter("language passed");
    private static final IntMeter URL_RECEIVER_METER =new IntMeter("url received");
    private static final FloatMeter langDetectTime   = new FloatMeter("lang detect Time") ;
    private static final FloatMeter webdocCreateTime = new FloatMeter("webdoc create Time");

    public synchronized static Parser getInstance() {
        return ourInstance;
    }

    private Parser() {
    }

    public static void setLangDetector(LangDetector langDetector) {
        Parser.langDetector = langDetector;
    }

    WebDocument parse(Document document, String url) throws IllegalLanguageException, MalformedURLException {
        URL_RECEIVER_METER.increment();
        String text = extarctText(document);
        WebDocument webDocument = new WebDocument();
        checkLanguage(text);
        Link[] links = UrlHandler.getLinks(document.getElementsByTag("a"), new URL(url).getHost());
        webDocument.setTextDoc(text);
        webDocument.setTitle(document.title());
        webDocument.setPageLink(url);
        webDocument.setLinks(new ArrayList<>(Arrays.asList(links)));
        CRAWLED_PAGE_METER.increment();
        return webDocument;
    }

    private String extarctText(Document document) {
        long tempTime = System.currentTimeMillis();
        String text = document.text();
        webdocCreateTime.add((float) (System.currentTimeMillis() - tempTime) / 1000);
        return text;
    }

    private void checkLanguage(String text) throws IllegalLanguageException {
        long tempTime = System.currentTimeMillis();
        langDetector.languageCheck(text);
        LANGUAGE_PASSED_METER.increment();
        langDetectTime.add((float) (System.currentTimeMillis() - tempTime) / 1000);

    }
}

