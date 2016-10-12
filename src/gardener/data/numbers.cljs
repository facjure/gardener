(ns gardener.data.numbers
  (:refer-clojure :exclude [+ - * / < > <= >= = -compare zero? neg? pos?])
  (:require [goog.math.Integer :as int]
            [cljs.core :as cljs]))

(defn bigint?
  [x]
  (instance? goog.math.Integer x))

(defn bigint
  [x]
  (if (bigint? x)
    x
    (int/fromString (str x))))

(defprotocol Add
  (-add [x y]))
(defprotocol AddWithInteger
  (-add-with-integer [x y]))
(defprotocol AddWithRatio
  (-add-with-ratio [x y]))

(defprotocol Multiply
  (-multiply [x y]))
(defprotocol MultiplyWithInteger
  (-multiply-with-integer [x y]))
(defprotocol MultiplyWithRatio
  (-multiply-with-ratio [x y]))

(defprotocol Invert
  (-invert [x]))

(defprotocol Negate
  (-negate [x]))

(defprotocol Ordered
  (-compare [x y]))
(defprotocol CompareToInteger
  (-compare-to-integer [x y]))
(defprotocol CompareToRatio
  (-compare-to-ratio [x y]))

(extend-type number
  Add
  (-add [x y] (-add (bigint x) y))
  ;; I have a hard time reasoning about whether or not this is necessary
  AddWithInteger
  (-add-with-integer [x y]
    (-add-with-integer (bigint x) y))
  AddWithRatio
  (-add-with-ratio [x y]
    (-add-with-ratio (bigint x) y))
  Multiply
  (-multiply [x y] (-multiply (bigint x) y))
  MultiplyWithInteger
  (-multiply-with-integer [x y]
    (-multiply-with-integer (bigint x) y))
  MultiplyWithRatio
  (-multiply-with-ratio [x y]
    (-multiply-with-ratio (bigint x) y))
  Negate
  (-negate [x] (-negate (bigint x)))
  Ordered
  (-compare [x y] (-compare (bigint x) y))
  CompareToInteger
  (-compare-to-integer
    [x y]
    (-compare-to-integer (bigint x) y))
  CompareToRatio
  (-compare-to-ratio
    [x y]
    (-compare-to-ratio (bigint x) y)))

(declare ratio)

(extend-type goog.math.Integer
  Add
  (-add [x y] (-add-with-integer y x))
  AddWithInteger
  (-add-with-integer [x y]
    (.add x y))
  AddWithRatio
  (-add-with-ratio [x y]
    (-add-with-ratio (ratio x) y))
  Multiply
  (-multiply [x y]
    (-multiply-with-integer y x))
  MultiplyWithInteger
  (-multiply-with-integer [x y]
    (.multiply x y))
  MultiplyWithRatio
  (-multiply-with-ratio [x y]
    (-multiply-with-ratio (ratio x) y))
  Negate
  (-negate [x] (.negate x))
  Invert
  (-invert [x] (ratio 1 x))
  Ordered
  (-compare [x y] (cljs/- (-compare-to-integer y x)))
  CompareToInteger
  (-compare-to-integer
    [x y]
    (.compare x y))
  CompareToRatio
  (-compare-to-ratio
    [x y]
    (-compare-to-ratio (ratio x) y)))

(defn gcd
  [x y]
  (if (.isZero y)
    x
    (recur y (.modulo x y))))

(deftype Ratio
  ;; "Ratios should not be constructed directly by user code; we assume n and d are
  ;;  canonical; i.e., they are coprime and at most n is negative."
  [n d]
  Add
  (-add [x y] (-add-with-ratio y x))
  AddWithInteger
  (-add-with-integer [x y]
    (-add-with-ratio x (ratio y)))
  AddWithRatio
  (-add-with-ratio [x y]
    (let [+ -add-with-integer
          * -multiply-with-integer
          n' (+ (* (.-n x) (.-d y))
                (* (.-d x) (.-n y)))
          d' (* (.-d x) (.-d y))]
      (ratio n' d')))
  Multiply
  (-multiply [x y] (-multiply-with-ratio y x))
  MultiplyWithInteger
  (-multiply-with-integer [x y]
    (-multiply x (ratio y)))
  MultiplyWithRatio
  (-multiply-with-ratio [x y]
    (let [* -multiply-with-integer
          n' (* (.-n x) (.-n y))
          d' (* (.-d x) (.-d y))]
      (ratio n' d')))
  Negate
  (-negate [x]
    (Ratio. (-negate n) d))
  Invert
  (-invert [x]
    (if (.isNegative n)
      (Ratio. (.negate d) (.negate n))
      (Ratio. d n)))
  Ordered
  (-compare [x y]
    (cljs/- (-compare-to-ratio y x)))
  CompareToInteger
  (-compare-to-integer [x y]
    (-compare-to-ratio x (ratio y)))
  CompareToRatio
  (-compare-to-ratio [x y]
    (let [* -multiply-with-integer]
      (-compare-to-integer (* (.-n x) (.-d y))
                           (* (.-n y) (.-d x))))))

(def ZERO (bigint 0))
(def ONE (bigint 1))

(defn ratio
  ([x] (ratio x ONE))
  ([x y]
     (let [x (bigint x),
           y (bigint y),
           d (gcd x y)
           x' (.divide x d)
           y' (.divide y d)]
       (if (.isNegative y')
         (Ratio. (.negate x') (.negate y'))
         (Ratio. x' y')))))

(defn ratio?
  [x]
  (instance? Ratio x))

(defn double?
  [x]
  (number? x))

;;
;; Convenience functions
;;

(defn- vararg
  [f x y z more]
  (every? (partial apply f) (partition 2 1 (list* x y z more))))

(defn =
  ([x] true)
  ([x y] (cljs/= 0 (-compare x y)))
  ([x y z & more]
     (vararg = x y z more)))

(defn <
  ([x] true)
  ([x y] (cljs/neg? (-compare x y)))
  ([x y z & more]
     (vararg < x y z more)))

(defn >
  ([x] true)
  ([x y] (cljs/pos? (-compare x y)))
  ([x y z & more]
     (vararg > x y z more)))

(defn <=
  ([x] true)
  ([x y] (not (cljs/pos? (-compare x y))))
  ([x y z & more]
     (vararg <= x y z more)))

(defn >=
  ([x] true)
  ([x y] (not (cljs/neg? (-compare x y))))
  ([x y z & more]
     (vararg >= x y z more)))

(defn +
  ([] 0)
  ([x] x)
  ([x y] (-add x y))
  ([x y z & more] (reduce -add (list* x y z more))))

(defn *
  ([] 0)
  ([x] x)
  ([x y] (-multiply x y))
  ([x y z & more] (reduce -multiply (list* x y z more))))

(defn -
  ([x] (-negate x))
  ([x y & more]
     (+ x (-negate (apply + y more)))))

(defn /
  ([x] (-invert x))
  ([x y & more]
     (* x (-invert (apply * y more)))))

(defn zero? [x] (= x ZERO))
(defn neg? [x] (< x ZERO))
(defn pos? [x] (< ZERO x))
