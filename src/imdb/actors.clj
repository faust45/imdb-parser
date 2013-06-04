(ns imdb.actors
  (import java.util.Scanner)
  (require [clojure.string :as s])
  (require [imdb.lucene.index :as index])
  (require digest))


(def file-path "/Users/faust45/Desktop/imdb/actors.list")
;(def file-path "./actors_short.list")

(defn move-to-start-point
  [coll]
  (drop-while #(not= "THE ACTORS LIST\n===============" %) coll))

(defn data-source 
  []
  (.useDelimiter (Scanner. (clojure.java.io/reader file-path)) "\n\n"))

(defn data-seq
  ([] (data-seq (data-source)))
  ([source]
    (cons (.next source)
          (if (.hasNext source)
            (lazy-seq (data-seq source))))))

(defn parse-actor-name
  [s]
  (let [i (.indexOf s "\t")]
    (if (< -1 i)
      (subs s 1 i))))

(defn parse-item
  [s]
  [(parse-actor-name s) s])

(defn save
  [[actor-name movies] iwriter]
  (index/add iwriter {:id [actor-name] 
                      :type "Actor" 
                      :name actor-name  
                      :movies movies}))

(defn run
  []
  (let [iwriter (index/writer)]
    (doseq [item (move-to-start-point (data-seq))]
           (-> item parse-item (save iwriter)))
           (.commit iwriter)
           (.close iwriter)))

(defn c
  []
  (.numDocs (index/reader)))

(defn r
  []
  (index/reader))


