(ns imdb.core
  (:require [ring.util.response :as resp]
            [clojure.java.io :as io]
            [compojure.handler :as handler]
            [clojure.data.json :as json]
            [imdb.movies :as movies])
  (:import java.util.UUID)
  (:use ring.adapter.jetty
        [clojure.string :only (join split)]
        [compojure.core]
        imdb.utils
        ring.middleware.multipart-params
        ring.middleware.params))


(defn search
  [q order]
  (-> (movies/search q order) json-resp))

(defn get-movie-data
  [q]
  (-> (movies/data q) json-resp))

(def my-routes
  (routes (GET "/search/movies" [q order]     (search q order))
          (GET "/search/movie-data"  [q] (get-movie-data q))
          (GET "/public/*" {{resource-path :*} :route-params} (send-file resource-path))))

(def app
  (-> my-routes
      wrap-params
      wrap-reload))
    
(defn main [& args]
  (run-jetty #'app {:port 8080}))


