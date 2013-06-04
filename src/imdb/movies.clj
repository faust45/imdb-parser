(ns imdb.movies
  (use imdb.utils)
  (require [imdb.ratings :as r])
  (require [clojure.string :as s])
  (require [imdb.lucene.index :as index])
  (require digest))

(def file-path "/Users/faust45/Desktop/imdb/movies.list")
;(def file-path "./movies_short.list")

(def title  #".+?(?=\()")
(def full-title  #".+?(?=\t)")
(def year   #"\(\d+\)")
(def series #"\{.+\}")

(defn add-rating
  [[title year _ _ :as item]]
  (-> (r/by-title-year title year) (cons item)))

(defn to-record 
  [[[votes rating] title year full-title series]]
  {:id [title year] 
   :type   "Movie"
   :votes   votes 
   :rating  rating
   :series  series
   :movies  full-title
   :year    (s-to-int year)})

(def parse-item
  (juxt (s-reg title) (s-reg year) (s-reg full-title) (s-reg series)))

(defn clean-item
  [[title year full-title series]]
  [(s-clean title) (s-remove year #"[()]") (s-clean full-title) (s-remove series #"\{\{SUSPENDED\}\}")])

(def data-seq
  (line-seq (clojure.java.io/reader file-path)))

(def to-start-point
  (partial drop-while (partial not= "MOVIES LIST")))

(def parsed-items 
  (partial map parse-item))

(def clean-items
  (partial map clean-item))

(def drop-series  
  (partial filter (comp blank? last)))

(def with-rating
  (partial map add-rating))

(def only-with-rating
  (partial filter (comp not blank? first)))

(def as-records
  (partial map to-record))

(def as-chunks (partial partition 1000))

(def stream (-> data-seq to-start-point parsed-items drop-series clean-items with-rating))

(defn save
  [record iwriter]
  (index/add iwriter record))

(defn rt
  []
  @r/data)

(defn run
  []
  (r/build-index)
  (let [iwriter (index/writer)]
    (println (count @r/data))
    (doseq [docs (-> stream only-with-rating as-records as-chunks)]
      (doseq [doc docs] (save doc iwriter))
      (println "commit - " (count docs))
      (.commit iwriter))
    (.close iwriter)))

(defn s-run
  []
  (try (run)
       (catch Exception e (str "Caught exception: " (clojure.stacktrace/print-stack-trace e)))))

(defn wrap-with-quotes 
  [s]
  (str "\"" s "\""))

(defn q-sort-by
  [order-type]
  (case order-type
    "votes"  (index/q-sort-by "votes"  :int)
    "rating" (index/q-sort-by "rating" :double)))

(defn search
  [q order-type]
  (let [q-str (str "type:Movie AND movies:" (-> q s/lower-case wrap-with-quotes))]
    (index/search q-str (q-sort-by (or order-type "rating")))))

(defn data 
  [q]
  (let [q-str (str "type:Movie AND movies:" (-> q s/lower-case wrap-with-quotes))]
    (index/search q-str)))

(defn c
  []
  (.numDocs (index/reader)))

(defn r
  []
  (index/reader))
