(defproject com.unpro/markov-text "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/tools.cli "0.3.1"]
                 [clj-yaml "0.4.0"]
                 [compojure "1.3.2"]
                 [ring "1.3.2"]
                 [sqlingvo "0.7.8"]
                 [org.clojure/java.jdbc "0.3.6"]
                 [org.postgresql/postgresql "9.3-1103-jdbc4"]]
  :main ^:skip-aot markov-text.cli
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev {:dependencies [[org.xerial/sqlite-jdbc "3.8.7"]]}}
  :plugins [[lein-ring "0.9.1"]]
  :ring {:handler markov-text.web/markov
         :init markov-text.web/dev-init})
