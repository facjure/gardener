(ns gardener.query.selectors
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :as io]))

(defprotocol Selector
  (-select [s m]))

(defn select [m selectors-coll]
  (reduce conj {} (map #(-select % m) selectors-coll)))

(extend-protocol Selector
  clojure.lang.Keyword
  (-select [k m]
    (find m k))
  clojure.lang.APersistentMap
  (-select [sm m]
    (into {}
          (for [[k s] sm]
            [k (select (get m k) s)]))))

(defn load-data [f]
  (edn/read-string (slurp (io/resource f))))
