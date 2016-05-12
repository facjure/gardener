(ns gardener.query.matches
  "Supports the speed optimized 'matches?' macro and 'fmatches?' fn for
   checking if a string matches a sequence of strings or wildcard strings."
  (:require
   [gardener.data.seqs :as sq]
   [gardener.data.strings :as str]))

(set! *warn-on-reflection* true)

(defn- create-wildcard-regex [^String regex]
  (re-pattern
   (str "^" (str/escape-regex-except-* (.toLowerCase regex)) "$")))

(defn- wildcard-match? [^String m]
  (<= 0 (.indexOf m "*")))

(defn- segregate-and-compile-matches [matches]
  (let [[wildcards exacts] (sq/segregate wildcard-match? matches)]
    [(vec (map create-wildcard-regex wildcards)) (set exacts)]))

(defn regex-matches? [^String src matches]
  (cond
   (string? src) (let [src (.toLowerCase src)]
                   (sq/any? #(re-find % src) matches))
   (coll? src) (sq/any? #(regex-matches? % matches) src)
   :else false))

(defn exact-matches? [src match-set]
  (cond
   (string? src) (contains? match-set src)
   (coll? src) (sq/any? #(exact-matches? % match-set) src)
   :else false))

;;; matches? has been redefined as a macro in order to improve the
;;; performance at run-time, by splitting the match clauses into exact
;;; and regex cases and making a clojure set out of the exact cases
;;; and pre-compiling the regex cases. See `fmatches?` for a
;;; functional equivalent of `matches?`.

(defmacro matches? [src matches]
  (let [[wildcards exacts] (segregate-and-compile-matches matches)]
    `(or (exact-matches? ~src ~exacts)
         (regex-matches? ~src ~wildcards))))

(defmacro not-matches? [src matches]
  `(not (matches? ~src ~matches)))

(defn fmatches?
  "The arg `src` may be a string or a collection of strings. Returns
  true if the string `src` or any item in the collection `src` matches
  anything in `matches`, false otherwise."
  [src matches]
  (let [[wildcards exacts] (segregate-and-compile-matches matches)]
    (or (exact-matches? src exacts)
        (regex-matches? src wildcards))))

(defn not-fmatches? [src matches]
  (not (fmatches? src matches)))

(defn is-in? [^String item elements]
  (if item
    (not (nil? (some #{(.toUpperCase item)} (map #(.toUpperCase ^String %) elements))))
    false))

(def is-not-in? (complement is-in?))
