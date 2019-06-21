(ns gardener.macros
  #?(:clj
     (:require
      [clojure.edn :as edn]
      [clojure.java.io :as io])))

#?(:clj
   (defmacro defdata
     "Read resource file into edn"
     [sym resource-name]
     (let [data (edn/read-string (slurp (io/resource resource-name)))]
       `(def ~sym ~data))))
