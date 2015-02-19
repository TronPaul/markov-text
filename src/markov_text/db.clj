(ns markov-text.db
  (:require [clojure.java.jdbc :as sql]
            [clojure.string :refer [join]]
            [clojure.string :as string]))

(def token-table-ddl
  (sql/create-table-ddl :tokens
                        [:id :serial "PRIMARY KEY"]
                        [:text :text "NOT NULL" "UNIQUE"]
                        [:count :integer "DEFAULT 1"]))

(def ngram-token-column-names
  (map #(str "token_" (inc %)) (range)))

(defn- ngram-table-ddl
  [ngram-size]
  (let [token-columns (take ngram-size ngram-token-column-names)]
    (apply sql/create-table-ddl
           :ngrams
           (concat [[:id :serial "PRIMARY KEY"]]
                   (map (fn [name]
                          [name :serial "references tokens (id)"]) token-columns)
                   [["UNIQUE" (str "(" (join "," token-columns) ")")]]))))

(def direction-enum-ddl
  "CREATE TYPE direction AS ENUM ('next', 'prev')")

(def connections-table-ddl
  (sql/create-table-ddl :connections
                        [:id :serial "PRIMARY KEY"]
                        [:direction :direction "NOT NULL"]
                        [:ngram :serial "NOT NULL" "references ngrams (id)"]
                        [:token :serial "NOT NULL" "references tokens (id)"]
                        [:count :integer "NOT NULL" "DEFAULT 1"]
                        ["UNIQUE" "(direction, ngram, token)"]))

(defn init-db
  [db-uri ngram-size]
  (sql/db-do-commands db-uri
                      token-table-ddl
                      (ngram-table-ddl ngram-size)
                      direction-enum-ddl
                      connections-table-ddl))

(defn add-ngram
  [db-spec token-records]
  (let [token-columns (take (count token-records) ngram-token-column-names)]
    (sql/insert! db-spec :ngrams (zipmap token-columns token-records))))

(defn inc-ngram
  [db-spec ngram-record]
  (sql/update! db-spec
               :ngrams
               {:count (inc (:count ngram-record))}
               ["id = ?" (:id ngram-record)]))

(defn add-token
  [db-spec text]
  (sql/insert! db-spec :tokens {:text text}))

(defn inc-token
  [db-spec token-record]
  (sql/update! db-spec
               :tokens
               {:count (inc (:count token-record))}
               ["id = ?" (:id token-record)]))

(defn add-connection
  [db-spec ngram-record token-record direction]
  (sql/insert! db-spec
               :connections
               {:ngram (:id ngram-record)
                :token (:id token-record)
                :direction direction}))

(defn inc-connection
  [db-spec connection-record]
  (sql/update! db-spec
               :connections
               {:count (inc (:count connection-record))}
               ["id = ?" (:id connection-record)]))

(defn get-token-by-text [db-spec token]
  (sql/query db-spec ["SELECT id FROM tokens WHERE text = ?" token]))

(defn get-ngram-by-tokens [db-spec token-records]
  (let [token-columns (take (count token-records) ngram-token-column-names)
        columns (concat ["id"] token-columns ["count"])
        select-clause (string/join ", " columns)
        where-clause (string/join " and " (map #(str % " = ?") token-columns))]
    (apply sql/query db-spec (string/join " " ["SELECT" select-clause "FROM ngrams WHERE" where-clause]) (map #(get % :id) token-records))))

(defn get-connection-by-ngram-token-dir [db-spec ngram-record token-record direction]
  (sql/query db-spec "SELECT id FROM connections WHERE ngram = ? and token = ? and direction = ?" (:id ngram-record) (:id token-record) direction))