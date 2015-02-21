(ns markov-text.util
  (:require [clojure.java.io :as io]
            [clj-yaml.core :as yaml])
  (:import (java.io File)))

(defn- parse-yaml
  [resource-like]
  (with-open [is (io/input-stream resource-like)]
    (yaml/parse-string is)))

(defn read-defaults []
  (if-let [url (io/resource "markov-text.yml")]
    (parse-yaml url)))

(defn- read-config* []
  (let [file (io/as-file "/etc/markov-text/markov-text.yml")]
    (if (.exists ^File file)
      (parse-yaml file))))

(defn read-config []
  (merge (read-defaults) (read-config*)))