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
(def server1-conn {:pool {} :spec {:host "127.0.0.1" :port 6379}})
(defmacro wcar* [& body] `(car/wcar server1-conn ~@body))

(def hashids-opts
  {:salt "This is the project salt"
   :min-length 7})

(defn is-valid-url? [url]
  (.isValid (UrlValidator. (into-array ["http" "https"])) url))


(def DEFAULT_ALPHABET "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890")
(def DEFAULT_SEPS "cfhistuCFHISTU")
(def TOKEN_LEN 8)
(def BASE_PATH "db")
(def TREE_LEN 4)

(defn random-token []
  (apply str (take TOKEN_LEN (shuffle (seq DEFAULT_ALPHABET)))))

(defn file-path
  "Returns a path made of basepath and a tree of dirs.
   abcdefghij -> db/a/b/c/d/abcdefghij"
  [file-token]
  (str BASE_PATH "/" (apply str (interleave file-token (take TREE_LEN (repeat \/)))) file-token))

(defn create-file-record [file-token url]
  (io/make-parents file-token)
  (spit file-token (prn-str {:url url})))


(defn futurismo [url]
  (Thread/sleep 10000)
  (println (str "Saving URL: " url))
  )

(defn display [url req]
  (future (futurismo url))
  (let [addr (:remote-addr req)
        ua  (get (:headers req) "user-agent")]
    (str url " - " addr " - " ua)))

(defn create-old [url]
  (let [id (h/encode hashids-opts 1)
        short-url (str "https://short.io/" id)]
    (str "Original URL is:" url "\nShort is " short-url)))

(defn create-item-in-db  [item]
  (.exists (clojure.java.io/file item)))


(defn file-free? [file-path]
  (not (.exists (clojure.java.io/file file-path))))

(defn create-instance [url]
  (loop [token (random-token)]
    (let [file-token (file-path token)]
      (if (file-free? file-token)
        (do
          (create-file-record file-token url)
          token)
        (recur (random-token))))))

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


(defn find-long-in-db [long]
  (let [db (clojure.edn/read-string (slurp "db.edn"))]
    (first (filter #(= (:url (val %)) long) (seq db)))))

(defn find-short-in-db [short]
  (let [db (clojure.edn/read-string (slurp "db.edn"))]
    (get db (first (h/decode hashids-opts short)))))

(defn get-short-old [short]
  (let [long (find-short-in-db short)]
    (if long
      (redirect (:url long) 301)
      (route/not-found "Not Found"))))

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
