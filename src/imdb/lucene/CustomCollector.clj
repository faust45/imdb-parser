(ns imdb.lucene.CustomCollector
  (gen-class
    :implements [org.apache.lucene.search.Collector]
    :name imdb.lucene.CustomCollector
    :prefix "-"
    :main false
    :state state
    :constructors {[] []}
    :init init
    :methods [[totalHits [] int]
             [get-docs [] java.util.Collection]])

  (:import [org.apache.lucene.search Collector FieldCache IndexSearcher TermQuery]))

(defn -field-to-kv
  [field]
  {(keyword (.name field)) (.stringValue field)})

(defn -init []
  [[] (atom {:hits 0 :docs ()})])
 
(defn -reader [this]
  (:reader (.state this)))

(defn -setNextReader [this docBase] 
  (reset! (.state this) (assoc 'reader (.reader docBase))))

(defn -collect [this docID]
  (println "hit collect: " docID)
  (let [doc (map #(-field-to-kv %) (.getFields (.document (.reader this) docID)))]))

(defn -acceptsDocsOutOfOrder [] true)

(defn -setScorer [scorer])

(defn -totalHits [this] 
  (:hits (.state this)))

(defn -docs [this] 
  (:docs (.state this)))


