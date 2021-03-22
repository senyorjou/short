(ns short.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [hashids.core :as h]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.util.response :refer [redirect]]))


;; (def db (clojure.edn/read-string (slurp "db.edn")))

(def hashids-opts
  {:salt "This is the project salt"
   :min-length 8})

(defn display [url req]
  (let [addr (:remote-addr req)
        ua  (get (:headers req) "user-agent")]
    (str url " - " addr " - " ua)))

(defn create [url]
  (let [id (h/encode hashids-opts 1)
        short-url (str "https://short.io/" id)]
    (str "Original URL is:" url "\nShort is " short-url)))

(defn find-long-in-db [long]
  (let [db (clojure.edn/read-string (slurp "db.edn"))]
    (first (filter #(= (:url (val %)) long) (seq db)))))

(defn find-short-in-db [short]
  (let [db (clojure.edn/read-string (slurp "db.edn"))]
    (get db (first (h/decode hashids-opts short)))))

(defn get-short [short]
  (let [long (find-short-in-db short)]
    (if long
      (redirect (:url long) 301)
      (route/not-found "Not Found"))))

(defroutes app-routes
  (GET "/" [] "Hello World")
  (POST "/" [url] (create url))
  (GET "/:short" [short] (get-short short))
  (GET "/c/:url" [url :as req] (display url req))
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes (assoc-in site-defaults [:security :anti-forgery] false)))
