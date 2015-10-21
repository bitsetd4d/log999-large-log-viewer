package com.blinglog.poc.benchmark;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by paul on 23/11/14.
 */
public class TestReadingFile {

    private static String FILE = "/Users/pauletlogic/Documents/testlargelogs/2014_10_15.stderrout.log";  // SSD
    //private static String FILE = "/Volumes/backup/professional_archive/logs/2014_10_15.stderrout.log";     // NAS

    public static void main(String[] args) throws Exception {
        benchmarkLucene();
        if (true) return;
        readWithReadLine();
        readWithReadLineAndBufferedReader();  // Doesnt seem quicker
        skipAheadReadWithReadLine();
        //readWithRandomAccess(); // really really really really slow
    }

    private static void benchmarkLucene() throws Exception {
        String INDEX_DIRECTORY = "/tmp/logindex";
        Analyzer analyzer = new StandardAnalyzer();
        boolean recreateIndexIfExists = true;

        Path indexDirLocation = Paths.get(INDEX_DIRECTORY);
        indexDirLocation.toFile().mkdirs();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

        Directory index = new MMapDirectory(indexDirLocation);
        IndexWriter indexWriter = new IndexWriter(index, config);


        long t1 = System.currentTimeMillis();
        LineNumberReader reader = new LineNumberReader(new FileReader(FILE));
        int lines = 0;
        while (reader.ready()) {
            String line = reader.readLine();
            lines++;
            if (line.length() < 32766) {
                Document document = new Document();
                document.add(new LongField("ln", lines, Field.Store.YES));
                document.add(new StringField("l", line, Field.Store.YES));
                indexWriter.addDocument(document);
            } else {
                System.out.println("Line too long. "+line.length());
            }
        }
        System.out.println("Committing");
        indexWriter.commit();
        reader.close();
//        long tt1 = System.currentTimeMillis();
//        indexWriter.forceMerge(1,true);
//        long tt2 = System.currentTimeMillis();
//        System.out.println("Force Merge = "+(tt2-tt1)+"ms");  //26247ms
        indexWriter.close();
        long t2 = System.currentTimeMillis();
        System.out.println("Lines = "+lines);
        System.out.println("Lucene = "+(t2-t1)+"ms");
    }

    private static void readWithReadLine() throws IOException {
        long t1 = System.currentTimeMillis();
        LineNumberReader reader = new LineNumberReader(new FileReader(FILE));
        int lines = 0;
        while (reader.ready()) {
            String line = reader.readLine();
            lines++;
        }
        reader.close();
        long t2 = System.currentTimeMillis();
        System.out.println("Lines = "+lines);
        System.out.println("Readline Time = "+(t2-t1)+"ms");
    }

    private static void skipAheadReadWithReadLine() throws IOException {
        long t1 = System.currentTimeMillis();
        LineNumberReader reader = new LineNumberReader(new FileReader(FILE));
        long skip500mb = 1024*1024*500;
        reader.skip(skip500mb);
        int lines = 0;
        while (reader.ready()) {
            String line = reader.readLine();
            lines++;
        }
        reader.close();
        long t2 = System.currentTimeMillis();
        System.out.println("Lines = "+lines);
        System.out.println("Skip500mb then ... Readline Time = "+(t2-t1)+"ms");
    }

    private static void readWithReadLineAndBufferedReader() throws IOException {
        long t1 = System.currentTimeMillis();
        LineNumberReader reader = new LineNumberReader(new BufferedReader(new FileReader(FILE)));
        int lines = 0;
        while (reader.ready()) {
            String line = reader.readLine();
            lines++;
        }
        reader.close();
        long t2 = System.currentTimeMillis();
        System.out.println("Lines = "+lines);
        System.out.println("Buffered Readline Time = "+(t2-t1)+"ms");
    }


    private static void readWithRandomAccess() throws IOException {
        long t1 = System.currentTimeMillis();
        RandomAccessFile raf = new RandomAccessFile(FILE, "r");
        int lines = 0;
        String line = raf.readLine();
        while (line != null) {
            int length = line.length();
            lines++;
            line = raf.readLine();
        }
        raf.close();
        long t2 = System.currentTimeMillis();
        System.out.println("Lines = "+lines);
        System.out.println("Random access fileTime = "+(t2-t1)+"ms");
    }

}
