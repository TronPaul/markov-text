(ns markov-text.web
  (:use compojure.core
        ring.adapter.jetty)
  (:require [markov-text.core :as core]))

(def config (ref {}))

(defn init []
  (dosync
    (alter config merge (core/read-config))))

(defroutes markov
           (POST "/line" [line]
             (core/add-line db-uri ngram-size line)))