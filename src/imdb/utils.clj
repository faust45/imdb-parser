(ns imdb.utils
  (require [ring.util.response :as resp]
            [clojure.data.json :as json])
  (require [clojure.string :as s])
  (use [clojure.string :only (join split)]))


(def blank? (comp empty? str))

(defn clean [value] (-> value str (s/replace #"[\t\n]" " ") s/trim))

(defn seqable? 
  [x]
  (instance? clojure.lang.Seqable x))

(defn s-clean
  [value]
  (if (seqable? value)
      (map clean value)
      (clean value)))

(defn s-join
  [value]
  (if (seqable? value)
      (s/join " " value)
      value))

(def clean-value (comp str s-clean))

(defn s-reg
  [reg]
  #(re-find reg %))

(defn s-remove
  [s reg]
  (if s
    (s/replace s reg "")))

(defn clean-movies-title 
  [title]
  (s-remove title #"[\"\(\)]"))

(defn json-resp
  [data]
  (-> data json/write-str resp/response (resp/header "Content-Type" "application/json")))
      

(defn send-file [path] 
  (resp/file-response path {:root "public/"}))

(defn wrap-reload
  [handler]
  (fn [req]
    (require 'imdb.core :reload)
    (require 'imdb.lucene.index :reload)
    (handler req)))

(defn is-json?
  [req]
  (= (:content-type req) "application/json; charset=UTF-8"))

(defn try-parse-json
  [req]
  (if (is-json? req) 
      (assoc req :data (json/read-str (slurp (:body req))))
      req))

(defn wrap-parse-json
  [handler]
  (fn [req]
    (-> req try-parse-json handler)))

(defn is-chess-move?
  [req]
  (= (:content-type req) "application/chess; charset=UTF-8"))

(defn parse-move
  [req]
    (split (slurp (:body req)) #" "))

(defn try-parse-chess-proto
  [req]
  (if (is-chess-move? req) 
      (assoc req :move (parse-move req))
      req))

(defn wrap-parse-chess-protocol
  [handler]
  (fn [req]
    (-> req
        try-parse-chess-proto
        handler)))

(defn s-to-int
  [s]
  (if s
    (try (Integer. s)
      (catch NumberFormatException e nil))))

(defn s-to-double
  [value]
  (if (re-find #"\d+\.?\d?+" (str value))
    (try (Double. value)
         (catch Exception e nil))))


