(ns short.hasher
  (:require [hashids.core :as h]))

(def hashids-opts
  {:salt "This is the project salt"
   :min-length 7})

(defn encode [x]
  (h/encode hashids-opts x))

(defn decode [x]
  (h/decode hashids-opts x))
