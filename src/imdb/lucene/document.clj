(ns imdb.lucene.document
  (use imdb.utils)
  (:require [clojure.string :as s])
  (:require digest)
  (:import clojure.lang.PersistentVector)
  (:import [org.apache.lucene.document Document IntField DoubleField Field TextField Field$TermVector Field$Index Field$Store]))

(defn id-field
  [f-value]
  (Field. "id" (digest/md5 (clean-value f-value)) Field$Store/YES Field$Index/NOT_ANALYZED))

(defn type-field
  [f-value]
  (Field. "type" f-value Field$Store/YES Field$Index/NOT_ANALYZED))

(defmacro type-case [e & clauses]
    `(condp = (type ~e)
        ~@clauses))

(defn original-field
  [f-name f-value]
  (Field. f-name f-value Field$Store/YES Field$Index/NOT_ANALYZED))

(defn not-indexed-field
  [f-name f-value]
  (Field. f-name f-value Field$Store/YES Field$Index/NOT_ANALYZED))


(defn default-field
  [f-name f-value]
  (type-case f-value
    Double  (DoubleField. f-name f-value Field$Store/YES)
    Integer (IntField. f-name f-value Field$Store/YES)
            (Field. f-name (str f-value) Field$Store/YES Field$Index/ANALYZED)))

(defn field
  [f-name f-value]
  (case f-name
    :id   (id-field f-value)
    :type (type-field f-value)
    :default (not-indexed-field (name f-name) f-value)
    (default-field (name f-name) f-value)))

(defn build
  [data]
  (let [doc (Document.)]
    (doseq [[k v] data]
      (.add doc (field k v)))
      doc))
