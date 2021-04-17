(ns short.db
  (:require
   [clojure.java.jdbc :as j]
   [taoensso.carmine :as car :refer [wcar]]
   [short.hasher :refer [decode encode]]))

;; Redis magic
(def redis-conn {:pool {} :spec {:host "127.0.0.1" :port 6379}})
(defmacro wcar* [& body] `(car/wcar redis-conn ~@body))

(def KEY-TTL 3600)

(def db
  {:classname   "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname     "db/database.db"
   })

(defn init-db []
  ;; create tbale if soes not exist
  (j/db-do-commands db (j/create-table-ddl
                        :urls
                        [[:timestamp :datetime :default :current_timestamp ]
                         [:long :text]
                         [:short :text :primary :key]]
                        {:conditional? true
                         :table-spec "WITHOUT ROWID"}))
  ;; set index to last value by acquiring most recent short and storing counter value on index cache key
  (if-let [last-id (:short (first (j/query db ["select short from urls order by timestamp desc limit 1"])))]
    (wcar* (car/set "index" (first (decode last-id))))))

(defn create-entry-in-db [long-url short-url]
  (println (str "Saving " short-url " in db")
  (j/insert! db :urls {:long long-url :short short-url})))

(defn create-entry-in-cache [long-url short-url]
  (println (str "Storing " short-url " in cache"))
  (wcar* (car/set short-url {:long long-url} "EX" KEY-TTL)))

(defn create-entry
  "Create a cache and db entry for the long-url, returns short-url."
  [long-url]
  (println (str "Received " long-url))
  (let [index (wcar* (car/incr "index"))
        short-url (encode index)]
    (println (str "Index and short are " index " / " short-url))
    (create-entry-in-cache long-url short-url)
    (create-entry-in-db long-url short-url)
    short-url))

(defn get-entry-from-cache
  "If exists gets an entry from cache and renews TTL."
  [short-url]
  (let [long-url (wcar* (car/get short-url))]
    (if long-url
      (wcar* (car/expire short-url KEY-TTL)))
    long-url))

(defn get-entry
  "Gets an entry from cache or db, returns long-url or nil."
  [short-url]
  (if-let [long-url (get-entry-from-cache short-url)]
    long-url
    (j/get-by-id db :urls short-url :short)))
