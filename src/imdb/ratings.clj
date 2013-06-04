(ns imdb.ratings
  (use imdb.utils)
  (require [imdb.lucene.index :as index]))


(def file-path "/Users/faust45/Desktop/imdb/ratings.list")
;(def file-path "./ratings_short.list")

(def data (atom {}))

(defn by-title-year
  [title year]
  (let [id (str title "-" year)]
       (get @data id)))

(defn parse-item
  [s]
  (if s
    (if-let [[all distribution votes rank title year series] (re-find #"\s*(.*\d+)\s+(\d+)\s+(\d.\d)\s+(.+)\((\d+)\)(\s+\{.+\})?" s)]
      [(s-to-int votes) (s-to-double rank) (clean title) (s-to-int year) series])))

(def data-seq
  (line-seq (clojure.java.io/reader file-path)))

(def to-start-point
  (partial drop-while (partial not= "New  Distribution  Votes  Rank  Title")))

(def parsed-items 
  (partial map parse-item))

(def drop-series  
  (partial filter (comp blank? last)))

(def stream 
  (-> data-seq to-start-point parsed-items drop-series))

(defn build-index []
  (reset! data {})
  (doseq [[votes rank title year] stream]
      (swap! data assoc (str title "-" year) [votes rank])))

