(ns markov-text.cli
  (:require [markov-text.db :as db]
            [clojure.tools.cli :as cli])
  (:gen-class))

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
      (= :clear-db subcommand) (db/clear-db (first subargs))
      :else (println (name subcommand) "not recognized."))))
