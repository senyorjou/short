(ns short.cache
  (:require [taoensso.carmine :as car :refer (wcar)]
            [short.hasher :refer [encode]]))

;; Redis magic
(def redis-conn {:pool {} :spec {:host "127.0.0.1" :port 6379}})
(defmacro wcar* [& body] `(car/wcar redis-conn ~@body))

(defn create-entry [url]
  (let [index (wcar* (car/incr "index"))
        short-token (encode index)]
    (wcar* (car/set short-token {:url url}))
    short-token))

(defn get-entry [short]
  (wcar* (car/get short)))
