(ns markov-text.core
  (:require [markov-text.db :as db]
            [clojure.tools.cli :as cli]
            [clojure.string :as string])
  (:gen-class))

(defn inc-or-add-token
  [db-spec token]
  (if-let [token-record (db/get-token-by-text token)]
    (db/inc-token db-spec token-record)
    (db/add-token db-spec token)))

(defn inc-or-add-ngram
  [db-spec token-records]
  (if-let [ngram-record (db/get-ngram-by-tokens token-records)]
    (db/inc-ngram db-spec ngram-record)
    (db/add-ngram db-spec token-records)))

(defn inc-or-add-connection
  [db-spec ngram-record token-record direction]
  (if-let [connection-record (db/get-connection-by-ngram-token-dir ngram-record token-record direction)]
    (db/inc-connection db-spec connection-record)
    (db/add-connection db-spec ngram-record token-record direction)))

(defn inc-or-add-ngram-data
  [db-spec ngram {:keys [prev next]}]
  (let [ngram-token-records (map (partial inc-or-add-token db-spec) ngram)
        prev-record (if prev (inc-or-add-token db-spec prev) nil)
        next-record (if next (inc-or-add-token db-spec next) nil)]
    (let [ngram-record (inc-or-add-ngram db-spec ngram-token-records)]
      (if prev-record
        (inc-or-add-connection db-spec ngram-record prev-record :prev))
      (if next-record
        (inc-or-add-connection db-spec ngram-record next-record :next)))))

(defn tokenize [line]
  (string/split line #" "))

(defn sanitize [tokens]
  (map (comp string/lower-case string/trim) (concat (drop-last tokens) [(string/replace (last tokens) #".$" "")])))

(defn add-line
  [db-uri ngram-size line]
  (let [tokens (sanitize (tokenize line))]
    (loop [prev nil
           ngram (take ngram-size tokens)
           next (nth tokens ngram-size)
           tokens (rest tokens)]
      (inc-or-add-ngram-data db-uri ngram {:prev prev :next next})
      (if next
        (recur (first ngram) (take ngram-size tokens) (nth tokens ngram-size nil) (rest tokens))))))

(def cli-opts
  {:init-db [["-n" "--ngram-size" "NGram size"
              :default 3]]})

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [{[subcommand-str & arguments] :arguments} (cli/parse-opts args nil :in-order true)
        subcommand (keyword subcommand-str)
        {{:keys [ngram-size]} :options subargs :arguments} (cli/parse-opts arguments (get cli-opts subcommand))]
    (cond
      (= :init-db subcommand) (db/init-db (first subargs) ngram-size)
      :else (println subcommand "not recognized."))))
