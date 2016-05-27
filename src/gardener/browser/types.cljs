(ns gardener.browser.types
  (:require
   [clojure.string :as string]))

;; Strings and numbers work as cursors and React doesn't know how to handle these
;; correctly. Extending type enables om/value to be used in conjunction with strings
;; and numbers

(extend-type string
  ICloneable
  (-clone [s] (js/String. s)))

(extend-type js/String
  om/IValue
  (-value [s] (str s))
  ICloneable
  (-clone [s] (js/String. s)))

(extend-type number
  ICloneable
  (-clone [n] (js/Number. n)))

(extend-type js/Number
  om/IValue
  (-value [n] (.valueOf n))
  ICloneable
  (-clone [n] (js/Number. n)))
