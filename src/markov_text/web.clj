(ns markov-text.web
  (:use compojure.core
        ring.adapter.jetty)
  (:require [markov-text.core :as core]
            [markov-text.util :as util]
            [markov-text.db :as db]))

(def config (ref {}))

(def db-spec (ref {:name "jdbc/MarkovDS"}))

(defn dev-init []
  "lein-ring dev setup"
  (dosync
    (ref-set db-spec "sqlite::memory:")
    (alter config merge (util/read-config)))
  (db/init-db @db-spec (:ngram-size @config) {:postgres? false}))

(defroutes markov
           (POST "/line" [line]
             (core/add-line @db-spec (:ngram-size @config) line)))