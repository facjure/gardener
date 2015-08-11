(ns gardener.core
  (:refer-clojure :exclude [+ - * / rem])
  (:require [garden.core :refer [css]]
            [garden.units :as units :refer (px pt em rem dpi)]
            [garden.color :as color :refer [hsl rgb]]
            [garden.arithmetic :refer [+ - * /]]
            [garden.stylesheet :refer [at-media]]))

(def breakpoints
  {:mobile (px 320)
   :tablet (px 768)
   :laptop (px 1024)
   :desktop (px 1440)})
