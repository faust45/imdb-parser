(defproject imdb "1.0.0-SNAPSHOT"
  :description "Indexing IMDB data"
  :jvm-opts ["-Xms1g"  "-Xmx1g"]
  :dependencies [
                 [org.apache.lucene/lucene-analyzers-common "4.2.0"]
                 [org.apache.lucene/lucene-core "4.2.0"]
                 [org.apache.lucene/lucene-queries "4.2.0"]
                 [org.apache.lucene/lucene-demo "4.2.0"]
                 [org.apache.lucene/lucene-wordnet "3.3.0"]
                 [org.apache.commons/commons-math "2.2" :classifier "sources"]
                 [org.clojure/data.json "0.2.1"]
                 [compojure "1.0.4"]
                 [ring/ring-core "1.1.0-beta3"]
                 [ring/ring-jetty-adapter "1.1.0-beta3"]
                 [digest "1.3.0"]
                 [org.clojure/clojure "1.3.0"]])
