(defproject com.unpro/markov-text "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [korma "0.4.0"]
                 [org.clojure/java.jdbc "0.3.6"]
                 [org.postgresql/postgresql "9.3-1103-jdbc4"]
                 [com.h2database/h2 "1.3.170"]
                 [org.clojure/tools.cli "0.3.1"]]
  :main ^:skip-aot markov-text.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
