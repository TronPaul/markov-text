(ns markov-text.core-test
  (:require [clojure.test :refer :all]
            [markov-text.core :refer :all]))

(defn call-storer
  [calls-ref]
  (partial alter calls-ref (fn [rv & args]
                             (conj rv (vec args)))))

(def token-seq
  (seq ["The"
        "quick"
        "brown"
        "fox"
        "jumped"
        "over"
        "the"
        "lazy"
        "dog."]))

(def line
  "The quick brown fox jumped over the lazy dog.")

(deftest tokenize-test
  (testing "Line tokenization"
    (is (= token-seq (tokenize line)))))

(deftest santize-tokens-test
  (testing "Token sanitization"
    (is (= (seq ["the"
                 "quick"
                 "brown"
                 "fox"
                 "jumped"
                 "over"
                 "the"
                 "lazy"
                 "dog"])
           (sanitize token-seq)))))

(deftest add-line-test
  (testing "Add line makes the correct add-ngram calls"
    (let [calls (ref [])]
      (with-redefs [inc-or-add-ngram (call-storer calls)]
        (dosync
          (add-line ::db-uri 3 line))
        (is (= [[::db-uri (seq ["the" "quick" "brown"]) {:prev nil :next "fox"}]
                [::db-uri (seq ["quick" "brown" "fox"]) {:prev "the" :next "jumped"}]
                [::db-uri (seq ["brown" "fox" "jumped"]) {:prev "quick" :next "over"}]
                [::db-uri (seq ["fox" "jumped" "over"]) {:prev "brown" :next "the"}]
                [::db-uri (seq ["jumped" "over" "the"]) {:prev "fox" :next "lazy"}]
                [::db-uri (seq ["over" "the" "lazy"]) {:prev "jumped" :next "dog"}]
                [::db-uri (seq ["the" "lazy" "dog"]) {:prev "over" :next nil}]]
               @calls))))))

(deftest add-ngram-test
  (testing "Add ngram makes the correct db calls"
    (let [])))
