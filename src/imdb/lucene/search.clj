(ns imdb.lucene.search
  (:require [imdb.lucene.index :as index])
  (:import [org.apache.lucene.index IndexReader DirectoryReader Term]
           [org.apache.lucene.search IndexSearcher TermQuery]
	   [org.apache.lucene.util Version]
           [org.apache.lucene.search FieldCache]
	   [org.apache.lucene.analysis.standard StandardAnalyzer]
	   [org.apache.lucene.analysis.core WhitespaceAnalyzer]
           [org.apache.lucene.queryparser.classic QueryParser]))


(def reader index/reader)
(def searcher (IndexSearcher. index/reader))
(def analyzer (WhitespaceAnalyzer. Version/LUCENE_CURRENT))
(def q-parser (QueryParser. Version/LUCENE_CURRENT, "title", analyzer))

(defn query
  [q] 
  (TermQuery. (Term. "title", q)))

(defn value
  [scoreDoc field]
  (.get (.doc searcher (.doc scoreDoc)) field))

(defn to-map
  [doc fields]
  (map #(value doc %) fields))

(defn search
  [q]
  (let [query (.parse q-parser q)
        hits (.search searcher query, 1000)
        scoreDocs (.scoreDocs hits)]
        (println "debug totalHits: " (.totalHits hits))
        (map #(value % "title") (map #(get scoreDocs %) (range 0 (.totalHits hits))))))
