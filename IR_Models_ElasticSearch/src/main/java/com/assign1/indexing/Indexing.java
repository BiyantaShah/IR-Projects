package com.assign1.indexing;

import com.assign1.app.Driver;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * Created by Biyanta on 26/05/17.
 */
public class Indexing {

    Pattern compilePattern = Pattern.compile("^ap");
    Properties properties = new Properties();

    public void indexFile(File[] listOfFiles) throws IOException {
        for(File file : listOfFiles) {
            if (validateFile(file)) {
                List<Doc> listOfDocs =  parseFile(file);
                Driver.elasticSearchConnection.createIndex(listOfDocs);
            }
        }
        try {
            properties.store(new FileOutputStream("src/main/resources/docLength.properties"), "com.assign1.indexing.Doc length");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<Doc> parseFile(File file) throws IOException {

        org.jsoup.nodes.Document document = Jsoup.parse(file, "UTF-8");

        Elements docs = document.getElementsByTag("DOC");
        List<Doc> listOfDocs = new ArrayList<Doc>();
        Iterator<Element> docsIterator = docs.iterator();

        while(docsIterator.hasNext()){
            Element element = docsIterator.next();
            Doc createdDoc = createDocument(element);

            if(createdDoc!= null){
                listOfDocs.add(createdDoc);
            }
        }

        return listOfDocs;
    }

    private Doc createDocument(Element element) {

        Doc doc = new Doc();
        String[] tags = {"DOCNO","TEXT"};
        for(String tag : tags){
            Elements eleTag = element.getElementsByTag(tag);
            Iterator<Element> eleIter = eleTag.iterator();

            while(eleIter.hasNext()){

                String textValue = eleIter.next().toString();

                if(tag.equals("DOCNO"))
                    doc.setDOCNO(textValue);

                else if(tag.equals("TEXT"))
                    doc.setTEXT(textValue);
            }
        }
        int docLength = doc.getTEXT().split(" ").length;
        properties.setProperty(doc.getDOCNO(), String.valueOf(docLength));
        return doc;
    }

    private boolean validateFile(File file) {
        return compilePattern.matcher(file.getName()).find();
    }
}
