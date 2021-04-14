(ns short.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.util.response :refer [redirect]]
            [short.cache :as cache]
            [short.views :as views])
  (:import org.apache.commons.validator.routines.UrlValidator))


(defn build-url [token]
  (str "http://localhost:3000/" token))

(defn is-valid-url? [url]
  (.isValid (UrlValidator. (into-array ["http" "https"])) url))

(defn futurismo [url]
  (Thread/sleep 2000)
  (println (str "Saving URL: " url)))

(defn display [url req]
  (future (futurismo url))
  (let [addr (:remote-addr req)
        ua  (get (:headers req) "user-agent")]
    (str url " - " addr " - " ua)))

(defn create [url]
  (if (is-valid-url? url)
    (build-url (cache/create-entry url))
    (str "Url is NOT valid my friend")))

(defn get-short-url [short]
  (let [url (cache/get-entry short)]
    (if url
      (redirect (:url url) 302)
      (route/not-found "Not Found my friend"))))

(defn get-short-url-info [short]
  (println (str "Processing " short))
  (let [url (cache/get-entry short)]
    (if url
      (:url url)
      (str short " Is not found on database"))))


(defroutes app-routes
  (GET "/" [] (views/main))
  (POST "/" [url] (create url))
  (GET "/:short" [short] (get-short-url short))
  (GET "/:short/+" [short] (get-short-url-info short))
  (GET "/c/:url" [url :as req] (display url req))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
    (wrap-defaults app-routes (assoc-in site-defaults [:security :anti-forgery] false)))
