(ns imdb.lucene.index
  (:require [clojure.string :as s])
  (:require [imdb.lucene.document :as document])
  (:import java.io.File)
  (:import [org.apache.lucene.index IndexWriterConfig IndexWriter IndexWriter Term]
           [org.apache.lucene.queryparser.classic QueryParser]
	   [org.apache.lucene.store RAMDirectory FSDirectory]
           [org.apache.lucene.index DirectoryReader]
           [org.apache.lucene.document SortedDocValuesField Field$ReusableStringReader]
           [org.apache.lucene.search Sort SortField SortField$Type Collector FieldCache IndexSearcher TermQuery]
	   [org.apache.lucene.util Version BytesRef]
           [org.apache.lucene.analysis.miscellaneous WordDelimiterFilter]
           [org.apache.lucene.analysis.tokenattributes CharTermAttribute]
           [org.apache.lucene.analysis.util CharArraySet] 
           [org.apache.lucene.analysis Analyzer Analyzer$TokenStreamComponents]
	   [org.apache.lucene.analysis.core LowerCaseFilter WhitespaceTokenizer StopAnalyzer SimpleAnalyzer WhitespaceAnalyzer]
	   [org.apache.lucene.analysis.standard StandardAnalyzer]))

(def version Version/LUCENE_CURRENT)

(defn custom-analyzer
  []
  (proxy [Analyzer] []
    (createComponents [fieldName reader]
      (let [wt  (WhitespaceTokenizer. version reader)
            lcf (LowerCaseFilter. version wt)
            wdf (WordDelimiterFilter. lcf WordDelimiterFilter/ALPHA (CharArraySet. version [] true))]
           (Analyzer$TokenStreamComponents. wt wdf)))))

(def config (IndexWriterConfig. version (custom-analyzer)))

(defn index-dir [] (FSDirectory/open (File. "/Volumes/NO NAME/index")))

(defn reader []
    (DirectoryReader/open (index-dir)))

(defn writer
  []
  (IndexWriter. (index-dir) config))

(def q-parser (QueryParser. version, "movies", (WhitespaceAnalyzer. version)))

(defn query
  [q] 
  (TermQuery. (Term. "title", q)))

(defn searcher 
  [reader]
  (IndexSearcher. reader))

(defn commit
  [iwriter]
  (.commit iwriter))

;(def debug (.setInfoStream config System/out))

(defn term
  ([^String field] (Term. field))
  ([^String field ^String text] (Term. field text)))

(defn count-terms
  [s]
  (.docFreq (reader) (term "title" s)))

(defn add
  [iwriter data]
  (.addDocument iwriter (document/build data)))

(defn pp-add
  [iwriter data]
  (println data))

(defn query
  [field q] 
  (TermQuery. (Term. field, q)))

(defn field-to-kv
  [field]
  {(keyword (.name field)) (.stringValue field)})

(defn to-record
  [ireader doc]
  (apply merge (map #(field-to-kv %) (.getFields (.document ireader (.doc doc))))))

(defn select-parser
  [field-type]
  (case field-type
    :int    SortField$Type/INT
    :double SortField$Type/DOUBLE))

(defn q-sort-by 
  [field field-type]
  (Sort. (SortField. field (select-parser field-type) true)))

(defn search
  [q q-sort]
  (let [ireader (reader)
        isearcher (searcher ireader)
        hits (.search isearcher (.parse q-parser q) 1000 q-sort)
        ireader2 (reader)
        docs (map #(to-record ireader2 %) (.scoreDocs hits))]
    (.close ireader)
    docs))

(defn debug
  []
  (let [text "T'his is a \"demo\" \tof\t \t\t'the' \"TokenStream\" API"
        analyzer (custom-analyzer)
        stream   (.tokenStream analyzer "body" (java.io.StringReader. text))
        termAtt  (.addAttribute stream CharTermAttribute)]
    (try ((.reset stream)
          (while (.incrementToken stream)
            (println (.toString termAtt)))
          (.end stream))
          (finally (.close stream)))
  ))

(defn ss-run
  [q]
  (try (search q)
       (catch Exception e (str "Caught exception: " (clojure.stacktrace/print-stack-trace e)))))



