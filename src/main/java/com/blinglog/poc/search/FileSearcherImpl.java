package com.blinglog.poc.search;

import com.blinglog.poc.util.IdGenerator;
import com.log999.logchunk.LoadableLogChunk;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class FileSearcherImpl implements FileSearcher {

    private static Logger logger = LoggerFactory.getLogger(FileSearcherImpl.class);

    private static StandardAnalyzer analyzer = new StandardAnalyzer();
    private Directory index;
    private DirectoryReader indexReader;
    private IndexSearcher indexSearcher;
    private IndexWriter indexWriter;
    private Analyzer standardAnalyser = new StandardAnalyzer();

    private static String LINE_NO = "LN";
    private static String LINE = "LINE";

    private boolean available = true;

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public void init() {
        try {
            String baseIndexDir = "/tmp/blinglog"; // FIXME
            String temp = IdGenerator.getNextId();
            Path indexDirLocation = Paths.get(baseIndexDir, temp);
            indexDirLocation.toFile().mkdirs();
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            index = new MMapDirectory(indexDirLocation);
            indexWriter = new IndexWriter(index, config);
        } catch (IOException e) {
            logger.error("Error starting indexing",e);
            available = false;
        }
    }

    @Override
    public void index(LoadableLogChunk chunk) {
        if (!available) return;
        try {
            long start = chunk.getLogLineStartIndex();
            List<String> lines = chunk.getLines();
            for (int i = 0; i < lines.size(); i++) {
                indexLine(start + i, lines.get(i));
            }
        } catch (IOException e) {
            logger.error("Indexing exception",e);
        }
    }

    private void indexLine(long lineNo, String line) throws IOException {
        Document doc = new Document();
        doc.add(new StoredField(LINE_NO,lineNo));
        String id = String.valueOf(lineNo);
        doc.add(new TextField(LINE, line, Field.Store.YES));
        indexWriter.updateDocument(new Term(LINE_NO, id), doc);
        //indexWriter.addDocument(doc);
    }

    public void foo() throws Exception {
        indexWriter.commit();
        //QueryParser p = new QueryParser(LINE,standardAnalyser);
        String text = "info";
        //text = text.toLowerCase().replaceAll("[^a-zA-Z0-9\\s]", "");
        BooleanQuery booleanQuery = new BooleanQuery();
        String[] split = text.split(" ");
        for (int i = 0; i < split.length; i = i + 1) {
            String str = split[i];
            str = str.trim();
            if (str.isEmpty()) continue;
//            if (i == split.length - 1) {
//                booleanQuery.add(new PrefixQuery(new Term(LINE, str)), BooleanClause.Occur.SHOULD);
//            } else {
                booleanQuery.add(new TermQuery(new Term(LINE, str)), BooleanClause.Occur.MUST);
//            }
            //booleanQuery.setBoost(1);
        }
        indexReader = DirectoryReader.open(indexWriter, true);
        indexSearcher = new IndexSearcher(indexReader);

        TopDocs search = indexSearcher.search(booleanQuery, 1000);
        //int hits = search.totalHits;
        logger.info("SEARCH.... {} hits",search.scoreDocs.length);
        for (int i=0; i<search.scoreDocs.length; i++) {
            ScoreDoc score = search.scoreDocs[i];
            DocumentStoredFieldVisitor visitor = new DocumentStoredFieldVisitor();
            indexSearcher.doc(score.doc, visitor);
            IndexableField f = visitor.getDocument().getField(LINE_NO);
            System.out.println("SEARCH >> " + f);
            List<IndexableField> fields = visitor.getDocument().getFields();
            fields.forEach(fi -> System.out.println("  fi = "+fi));
        }

    }

//    void addDocumentToIndex(String id, Map<String, Object> fields) throws IOException {
//        Document doc = new Document();
//        doc.add(new StringField(ID_FIELD, id, Field.Store.YES));
//        for (Map.Entry<String, Object> entry : fields.entrySet()) {
//            Object value = parseValue(entry.getValue());
//            addIndexField(doc, entry.getKey(), value);
//        }
//
//        indexWriter.updateDocument(new Term(ID_FIELD, id), doc);
//    }
//
//    private void addDocumentToIndex(String id, Map<String, Object> fields) throws IOException {
//        Document doc = new Document();
//        doc.add(new StringField(ID_FIELD, id, Field.Store.YES));
//        for (Map.Entry<String, Object> entry : fields.entrySet()) {
//            Object value = parseValue(entry.getValue());
//            addIndexField(doc, entry.getKey(), value);
//        }
//
//        indexWriter.updateDocument(new Term(ID_FIELD, id), doc);
//    }
//
//    private void addIndexField(Document doc, String field, Object value) {
//        if (value == null) return;
//        if (value instanceof String) {
//            if (((String) value).length() < 200) {
//                String lowerCase = ((String) value).toLowerCase();
//                doc.add(new SortedDocValuesField(field, new BytesRef(lowerCase)));
//                doc.add(new StringField(field + ORIGINAL, ((String) value), Field.Store.YES));
//                doc.add(new TextField(field, lowerCase, Field.Store.NO));
//            } else {
//                doc.add(new TextField(field, (String) value, Field.Store.YES));
//            }
//        } else if (value instanceof Long || value instanceof Integer) {
//            long value2 = ((Number) value).longValue();
//            doc.add(new NumericDocValuesField(field, value2));
//            doc.add(new StoredField(field, value2));
//        } else if (value instanceof Double) {
//            doc.add(new DoubleDocValuesField(field, (double) value));
//            doc.add(new StoredField(field, (double) value));
//        } else if (value instanceof Float) {
//            doc.add(new FloatDocValuesField(field, (float) value));
//            doc.add(new StoredField(field, (float) value));
//        } else {
//            throw new UnsupportedOperationException("Need to map: " + value);
//        }
//    }
//
//    static Object parseValue(Object value) {
//        if (value == null) return null;
//        if (value instanceof Number) {
//            return value;
//        }
//
//        if (value instanceof String) return value;
//        if (value instanceof Boolean) {
//            return String.valueOf(value);
//        }
//
//        if (value instanceof Date) {
//            value = ((Date) value).getTime();
//        } else if (value instanceof DateTime) {
//            value = ((DateTime) value).asDate().getTime();
//        } else if (value instanceof Timestamp) {
//            value = ((Timestamp) value).asLong();
//        } else if (value instanceof DateOnly) {
//            value = ((DateOnly) value).toDate().getTime();
//        } else if (value instanceof TimeOnly) {
//            value = ((TimeOnly) value).asString();
//        } else if (value instanceof Enum) {
//            value = ((Enum) value).name();
//        } else {
//            throw new UnsupportedOperationException("need to know how to parse: " + value);
//        }
//        return value;
//    }


}
