(ns short.handler
  (:require [clojure.java.io :as io]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [hashids.core :as h]
            [taoensso.carmine :as car :refer (wcar)]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.util.response :refer [redirect]])
   (:import
    org.apache.commons.validator.routines.UrlValidator))


;; (def db (clojure.edn/read-string (slurp "db.edn")))

;; Redis magic
(def redis-conn {:pool {} :spec {:host "127.0.0.1" :port 6379}})
(defmacro wcar* [& body] `(car/wcar redis-conn ~@body))

(def hashids-opts
  {:salt "This is the project salt"
   :min-length 7})

(defn is-valid-url? [url]
  (.isValid (UrlValidator. (into-array ["http" "https"])) url))

(defn futurismo [url]
  (Thread/sleep 10000)
  (println (str "Saving URL: " url))
  )

(defn display [url req]
  (future (futurismo url))
  (let [addr (:remote-addr req)
        ua  (get (:headers req) "user-agent")]
    (str url " - " addr " - " ua)))

(defn create-cache-entry [url]
  (let [index (wcar* (car/incr "index"))
        short-token (h/encode hashids-opts index)]
    (wcar* (car/set short-token {:url url}))
    short-token
    )
  )

(defn create [url]
  (if (is-valid-url? url)
    (let [short-url (str "https://short.io/" (create-cache-entry[url]))]
      (str "Original URL is: " url "\nShort is " short-url))
    (str "Url is NOT valid my friend")))


(defn get-short [short]
  (let [url (wcar* (car/get short))]
    (if url
      (redirect (first (:url url)) 302)
      (route/not-found "Not Found"))))

(defroutes app-routes
  (GET "/" [] "Hello World")
  (POST "/" [url] (create url))
  (GET "/:short" [short] (get-short short))
  (GET "/c/:url" [url :as req] (display url req))
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes (assoc-in site-defaults [:security :anti-forgery] false)))
