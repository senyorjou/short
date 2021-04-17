(defproject short "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/java.jdbc "0.7.12"]
                 [com.taoensso/carmine "3.1.0"]
                 [commons-validator/commons-validator "1.7"]
                 [compojure "1.6.1"]
                 [hiccup "1.0.5"]
                 [jstrutz/hashids "1.0.1"]
                 [org.xerial/sqlite-jdbc "3.34.0"]
                 [ring/ring-defaults "0.3.2"]]
  :plugins [[lein-ring "0.12.5"]]
  :ring {:handler short.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.2"]]}})
