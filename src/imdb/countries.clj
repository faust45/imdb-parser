(ns imdb.countries
  (:require [clojure.string :as s])
  (require [imdb.lucene.index :as index])
  (require digest))

;(def file-path "/Users/faust45/Desktop/imdb/movies.list")
(def file-path "countries_short.list")

(defn move-to-start-point
  [coll]
  (drop-while #(not= "COUNTRIES LIST" %) coll))

(def data-seq
  (line-seq (clojure.java.io/reader file-path)))

(defn parse-item
  [s]
  (s/split s #"\t+"))

(defn save
  [[movie countries] raw iwriter]
  (if movie 
    (index/add iwriter {:type   "Country"
                        :countries countries 
                        :movies movie})))

(defn run
  []
  (let [iwriter (index/writer)]
    (doseq [item (move-to-start-point data-seq)]
      (-> item parse-item (save item iwriter)))
    (.commit iwriter)
    (.close iwriter)))

(defn c
  []
  (.numDocs (index/reader)))

(defn r
  []
  (index/reader))

(defn s-run
  []
  (try (run)
       (catch Exception e (str "Caught exception: " (clojure.stacktrace/print-stack-trace e)))))

