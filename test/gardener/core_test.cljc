(ns gardener.core-test
  (:refer-clojure :exclude [complement])
  (:require
   #?(:cljs [cljs.test :as t :refer-macros [is are deftest testing]]
      :clj  [clojure.test :as t :refer [is are deftest testing]]))
  #?(:clj
     (:import clojure.lang.ExceptionInfo)))
