package com.assign2.indexing;

import com.assign2.app.Driver;
import com.assign2.indexing.Doc;
import com.assign2.indexing.MergeSort;
import com.assign2.indexing.TokenInfo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.tartarus.snowball.ext.PorterStemmer;

import java.io.*;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by Biyanta on 03/06/17.
 */
public class Indexing {

    Pattern compilePattern = Pattern.compile("^ap");
    Properties properties = new Properties();

    static final Map<String,List<Integer>> termPositionsOfDocument = new HashMap<String, List<Integer>>();
    private int fileCount = 1;

    public void processStopWords() throws IOException {

        File stopList_path = new File("stoplist.txt");
        BufferedReader br = new BufferedReader(new FileReader(stopList_path));
        String readLine;

        while ((readLine = br.readLine()) != null) {
            Driver.stopWords.add(readLine);
        }
    }

    public void indexDocs(File[] listOfFiles) throws IOException {

        List<Doc> docsToIndex = new ArrayList<Doc>();

        for(File file : listOfFiles) {
            if (validateFile(file)) {
                List<Doc> listOfDocs =  parseFile(file);

                if((docsToIndex.size()+listOfDocs.size()) > 5000){

                    indexTermsForDocs(docsToIndex);
                    docsToIndex.clear();
                    docsToIndex.addAll(listOfDocs);
                }
                else{
                    
                    docsToIndex.addAll(listOfDocs);
                }
            }
        }
        indexTermsForDocs(docsToIndex);
        
        try {
            properties.store(new FileOutputStream("src/main/resources/docLength.properties"), "Document length");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void indexTermsForDocs(List<Doc> docsToIndex) {

        // term , (doc no, number of times this term appears and its position in the document)
        Map<String,List<TokenInfo>> tempInvertedTokenMap = new HashMap<String,List<TokenInfo>>();

        String[] tokens = null;

        for (Doc docToIndex: docsToIndex) {

            tokens = cleanDocument(docToIndex.getTEXT().trim().toLowerCase());

            Map<String, Integer> tokenFreqMap = processTokens(tokens);

            Iterator<Map.Entry<String, Integer>> tokenIter = tokenFreqMap.entrySet().iterator();

            while (tokenIter.hasNext()) {
                Map.Entry<String, Integer> tokenFreq = tokenIter.next();

                TokenInfo tokenInfo = new TokenInfo(Long.toString(docToIndex.getDOCNO()), null,
                        tokenFreq.getValue(), getPos(tokenFreq.getKey()));

                if(tokenFreq.getKey().length() == 0)
                    continue;

                if (tempInvertedTokenMap.containsKey(tokenFreq.getKey())) {
                    tempInvertedTokenMap.get(tokenFreq.getKey()).add(tokenInfo);
                }
                else {
                    List<TokenInfo> tempTokenInfoList = new ArrayList<TokenInfo>();
                    tempTokenInfoList.add(tokenInfo);

                    tempInvertedTokenMap.put(tokenFreq.getKey(), tempTokenInfoList);
                }
            }

        }

        printIntermediateTokens(tempInvertedTokenMap, fileCount);
        generateIntermediateCatalogFile(fileCount);

        try {
            mergeIntermediateIndexes(fileCount);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        ++fileCount;

    }

    private void mergeIntermediateIndexes(int fileCount) throws IOException {

        // Intermediate files
        final File toMergeIndexFile = new File("Indexed/InvertedFile_"+ fileCount +".txt");
        final File toMergeCatalogFile = new File("Indexed/CatalogFile_"+ fileCount +".txt");

        // Final files
        final File finalInvertedFile = new File("Indexed/FinalInvertedFile.txt");
        final File finalCatalogFile = new File("Indexed/FinalCatalogFile.txt");

        // Temporary Files
        final File tempIndexFile = new File("Indexed/tempFinalInvertedFile.txt");
        final File tempCatalogFile  = new File("Indexed/tempCatalogFile.txt");

        if(fileCount == 1){
            mergingForFirstFile(toMergeIndexFile, toMergeCatalogFile, tempIndexFile);
            toMergeIndexFile.delete();
            System.out.println("final file written");
           return;
        }

        RandomAccessFile randomIntermediateInvertedFile = new RandomAccessFile(toMergeIndexFile, "r");
        RandomAccessFile randomFinalInvertedFile = new RandomAccessFile(finalInvertedFile, "r");
        BufferedWriter writer = new BufferedWriter(new FileWriter(tempIndexFile));

        // Load the catalog files into memory
        Map<String, String> intermediateCatalogMap = deserializeCatalogFile(toMergeCatalogFile);
        Map<String, String> finalCatalogMap = 	deserializeCatalogFile(finalCatalogFile);

        Iterator<Map.Entry<String, String>> intermediateCatalogIter = intermediateCatalogMap.entrySet().iterator();

        while (intermediateCatalogIter.hasNext()) {
            Map.Entry<String, String> catalogEntry = intermediateCatalogIter.next();
            String[] offset = catalogEntry.getValue().split("@");
            randomIntermediateInvertedFile.seek(Long.parseLong(offset[0]));

            byte[] bArray = new byte[Integer.parseInt(offset[1])];
            randomIntermediateInvertedFile.read(bArray);

            List<TokenInfo> tokenInfoList = getTokenInfoList(new String(bArray));

            if(finalCatalogMap.containsKey(catalogEntry.getKey())){

                String[] value = finalCatalogMap.get(catalogEntry.getKey()).split("@");

                randomFinalInvertedFile.seek(Long.parseLong(value[0]));

                byte[] b = new byte[Integer.parseInt(value[1])];
                randomFinalInvertedFile.read(b);

                tokenInfoList.addAll(getTokenInfoList(new String(b)));
                finalCatalogMap.remove(catalogEntry.getKey());
            }

            MergeSort.MergeSort(tokenInfoList, 0, tokenInfoList.size()-1);

            writer.write(catalogEntry.getKey().trim()+"="+getDFandTTF(tokenInfoList)+"%"
                    + getStringFromTokenInfoList(tokenInfoList) + "\n");
        }

        Iterator<Map.Entry<String, String>> finalCatalogIter = finalCatalogMap.entrySet().iterator();

        while(finalCatalogIter.hasNext()){

            Map.Entry<String, String> catalogEntry = finalCatalogIter.next();

            String[] value = finalCatalogMap.get(catalogEntry.getKey()).split("@");

            randomFinalInvertedFile.seek(Long.parseLong(value[0]));

            byte[] b =  new byte[Integer.parseInt(value[1])];

            randomFinalInvertedFile.read(b);
            List<TokenInfo> tokenInfoList = new ArrayList<TokenInfo>();
            tokenInfoList.addAll(getTokenInfoList(new String(b)));

            MergeSort.MergeSort(tokenInfoList, 0, tokenInfoList.size()-1);

            writer.write(catalogEntry.getKey().trim()+"="+getDFandTTF(tokenInfoList)+"%"
                    + getStringFromTokenInfoList(tokenInfoList)+"\n");
        }

        writer.flush();
        writer.close();

        randomIntermediateInvertedFile.close();
        randomFinalInvertedFile.close();

        //generate the offset file for the final
        generateFinalCatalogFile(tempIndexFile,tempCatalogFile);

        //rename the temp files to the final files.
        tempIndexFile.renameTo(finalInvertedFile);
        tempCatalogFile.renameTo(finalCatalogFile);

        toMergeIndexFile.delete();
        toMergeCatalogFile.delete();

        return;
    }

    private String getDFandTTF(List<TokenInfo> tokenInfoList) {

        int DF = tokenInfoList.size();
        int TTF = 0;

        StringBuilder sb = new StringBuilder();

        Iterator<TokenInfo> iter = tokenInfoList.iterator();

        while(iter.hasNext()) {

            TokenInfo token = iter.next();
            TTF += token.getCount();
        }
        sb.append(DF+"!"+TTF);
        return sb.toString();

    }

    private void mergingForFirstFile(File toMergeIndexFile,
                                     File toMergeCatalogFile,
                                     File tempIndexFile) throws IOException {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(tempIndexFile));
            RandomAccessFile randomIntermediateInvertedFile = new RandomAccessFile(toMergeIndexFile, "r");
            Map<String,String> intermediateCatalogMap = deserializeCatalogFile(toMergeCatalogFile);

            Iterator<Map.Entry<String, String>> intermediateCatalogIter = intermediateCatalogMap.entrySet().iterator();

            while (intermediateCatalogIter.hasNext()) {
                Map.Entry<String, String> catalogEntry = intermediateCatalogIter.next();
                String[] offset = catalogEntry.getValue().split("@");
                randomIntermediateInvertedFile.seek(Long.parseLong(offset[0]));

                byte[] b = new byte[Integer.parseInt(offset[1])];
                randomIntermediateInvertedFile.read(b);
                List<TokenInfo> tokenInfoList = getTokenInfoList(new String(b));

                MergeSort.MergeSort(tokenInfoList, 0, tokenInfoList.size()-1);

                writer.write(catalogEntry.getKey().trim()+"="+getDFandTTF(tokenInfoList)+"%"
                        + getStringFromTokenInfoList(tokenInfoList) + "\n");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        writer.flush();
        writer.close();

        tempIndexFile.renameTo(new File("Indexed/FinalInvertedFile.txt"));
        toMergeCatalogFile.renameTo(new File ("Indexed/FinalCatalogFile.txt"));

        return;

    }

    private void generateFinalCatalogFile(File tempIndexFile, File tempCatalogFile) {
        try {

            BufferedWriter writer = new BufferedWriter(new FileWriter(tempCatalogFile));

            BufferedReader reader = new BufferedReader(new FileReader(tempIndexFile));

            String tokenString = null;
            int offsetCount = 0;

            while ((tokenString = reader.readLine()) != null) {

                String[] offsetString = tokenString.split("=");
                writer.write(offsetString[0]+" "+offsetCount+" "+
                        tokenString.length() + System.getProperty("line.separator"));

                offsetCount += tokenString.length() + 1;
            }

            writer.flush();
            writer.close();
            reader.close();


            System.out.println("final file written");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getStringFromTokenInfoList(List<TokenInfo> tokenInfoList) {

        StringBuilder sb = new StringBuilder();

        Iterator<TokenInfo> iter = tokenInfoList.iterator();

        while(iter.hasNext()){

            TokenInfo token = iter.next();
            sb.append(token.getDocId()+"#"+token.getCount()+"~"+token.getTermPos() +";");
        }
        return sb.toString();
    }

    private List<TokenInfo> getTokenInfoList(String tokenString) {

        List<TokenInfo> temp = new ArrayList<TokenInfo>();

        String[] tokenInfoList = tokenString.split("=")[1].split(";");

        for (String tokenInfo : tokenInfoList) {

            if (tokenInfo.contains("!")) {
                String[] tfData = tokenInfo.split("%");
                String[] tokenData = tfData[1].split("#");
                String[] posData = tokenData[1].split("~");

                try{
                    temp.add(new TokenInfo(tokenData[0], null, Integer.parseInt(posData[0]), posData[1]));
                }
                catch(Exception e){
                    e.printStackTrace();
                }

            }
            else  {
                String[] tokenData = tokenInfo.split("#");
                String[] posData = tokenData[1].split("~");

                try{
                    temp.add(new TokenInfo(tokenData[0], null, Integer.parseInt(posData[0]), posData[1]));
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }

        }
        return temp;
    }

    private Map<String, String> deserializeCatalogFile(File catalogFile) throws IOException {

        Map<String, String> catalogMap = new HashMap<String, String>();
        BufferedReader reader = new BufferedReader(new FileReader(catalogFile));

        String line = new String();

        while((line = reader.readLine())!=null ){

            String[] data = line.split(" ");
            StringBuilder sb = new StringBuilder();
            sb.append(data[1]+"@"+data[2]);
            catalogMap.put(data[0], sb.toString());
        }
        reader.close();

        return catalogMap;

    }

    private void generateIntermediateCatalogFile(int fileCount) {

        final File catalogFile = new File("Indexed/CatalogFile_"+ fileCount +".txt");
        final File indexFile = new File("Indexed/InvertedFile_"+ fileCount +".txt");

        try {

            BufferedWriter writer = new BufferedWriter(new FileWriter(catalogFile));
            BufferedReader reader = new BufferedReader(new FileReader(indexFile));

            String tokenString = null;
            int offsetCount = 0;

            while ((tokenString = reader.readLine()) != null) {

                String[] offsetString = tokenString.split("=");
                writer.write(offsetString[0]+" "+offsetCount+" "+
                        tokenString.length() + System.getProperty("line.separator"));
                offsetCount += tokenString.length() + 1;
            }
            writer.flush();
            writer.close();
            reader.close();

            System.out.println("Catalog " + fileCount +" written");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printIntermediateTokens(Map<String, List<TokenInfo>> tempInvertedTokenMap, int fileCount) {

        File file = new File("Indexed/InvertedFile_"+ fileCount +".txt");

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));

            Iterator<Map.Entry<String, List<TokenInfo>>> tokenIter = tempInvertedTokenMap.entrySet().iterator();

            while (tokenIter.hasNext()) {
                Map.Entry<String, List<TokenInfo>> token = tokenIter.next();

                writer.write(token.getKey()+"="+printDF_TTF(token.getValue())+"%");

                for (TokenInfo tokenInfo : token.getValue()) {
                    writer.write(tokenInfo.getDocId()+"#"+tokenInfo.getCount()
                            +"~"+tokenInfo.getTermPos()+";");
                }
                writer.write(System.getProperty("line.separator"));

            }
            writer.flush();
            writer.close();

            System.out.println("Inverted Index "+ fileCount+" Written");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String printDF_TTF(List<TokenInfo> value) {
        int DF = value.size();
        int TTF = 0;

        StringBuilder sb = new StringBuilder();

        for (TokenInfo tokenInfo: value) {
            TTF += tokenInfo.getCount();
        }
        sb.append(DF+"!"+TTF);
        return sb.toString();
    }


    private String getPos(String key) {

        List<Integer> termPos = termPositionsOfDocument.get(key);
        StringBuilder sb = new StringBuilder();

        for (Integer pos : termPos) {
            sb.append(pos);
            sb.append("-");
        }

        String str = sb.toString().substring(0, sb.toString().length()-1);
        return str;
    }


    private Map<String,Integer> processTokens(String[] tokens) {

        Map<String, Integer> tokenFreqForDocument = new HashMap<String, Integer>();

        for (String token : tokens) {
            token = getStemOfWord(token);

            if (tokenFreqForDocument.containsKey(token)) {
                tokenFreqForDocument.put(token, tokenFreqForDocument.get(token)+1);
            }
            else {
                tokenFreqForDocument.put(token, 1);
            }
        }

        return tokenFreqForDocument;
    }

    private String[] cleanDocument(String docText) {

        StringBuilder sb = new StringBuilder();

        // tokenize each line
        sb.append(docText.replaceAll("<text>", "")
                .replaceAll("</text>", "")
                .replaceAll("'s", " is")
                .replaceAll("-", " ")
                .replaceAll("[^A-Za-z0-9. ]", ""));

        String tempString = sb.toString().trim();
        String[] tokens = tempString.split(" ");

        // replace punctuations
        for (int i = 0; i < tokens.length; i++) {
            tokens[i] = tokens[i].replaceAll("(\\.$|^\\.)", "");
        }

        termPositionsOfDocument.clear();
        int pos = 1;

        List<String> cleanedTokens = new ArrayList<String>();

        for (String token : tokens) {

            String stemmed = null;

            // update pos in document
            if (Driver.stopWords.contains(token.trim())) {
                pos++;
                continue;
            }
            else if (token.length() == 1) {
                pos++;
                continue;
            }
            else if (token.equals(".")) {
                pos++;
                continue;
            }
            else if (token.matches("[\\d]+(\\.)[a-z]+")) {

                stemmed = getStemOfWord(token.split("\\.")[1].trim());
//                stemmed = token.split("\\.")[1].trim();
                try {
                    cleanedTokens.add(token.split("\\.")[1].trim());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else {
                stemmed = getStemOfWord(token);
//                stemmed = token;
                cleanedTokens.add(token);
            }

            if (termPositionsOfDocument.containsKey(stemmed)) {

                termPositionsOfDocument.get(stemmed).
                        add(pos - termPositionsOfDocument.get(stemmed)
                                .get(termPositionsOfDocument.get(stemmed).size()-1));

            }
            else  {

                List<Integer> posList = new ArrayList<Integer>();
                posList.add(pos);
                termPositionsOfDocument.put(stemmed, posList);
            }

            pos ++;

        }

        String[] cleaned = new String[cleanedTokens.size()];

        return cleanedTokens.toArray(cleaned);
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

    public static String getStemOfWord(String term) {

        PorterStemmer stemValue = new PorterStemmer();
        stemValue.setCurrent(term);
        stemValue.stem();
        return stemValue.getCurrent();

    }

    private Doc createDocument(Element element) throws IOException {
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

        properties.setProperty(String.valueOf(doc.getDOCNO()), String.valueOf(docLength));

        return doc;
    }

    private boolean validateFile(File file) {

        return compilePattern.matcher(file.getName()).find();
    }
}
