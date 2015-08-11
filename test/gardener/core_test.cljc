(ns gardener.core-test
  (:refer-clojure :exclude [complement])
  (:require
   #?(:cljs [cljs.test :as t :refer-macros [is are deftest testing]]
      :clj  [clojure.test :as t :refer [is are deftest testing]])
   [garden.color :as color])
  #?(:clj
     (:import clojure.lang.ExceptionInfo)))

(def hex-black "#000000")
(def rgb-black (color/rgb 0 0 0))

(deftest color-conversion-test
  (testing "hex->rgb"
    (are [x y] (= x y)
      (color/hex->rgb hex-black) rgb-black)))
