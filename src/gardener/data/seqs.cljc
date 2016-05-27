(ns ^{:doc "Functions that operate on Clojure sequences."}
  gardener.data.seqs
  (:require
    [clojure.walk :as walk]))

(set! *warn-on-reflection* true)

(defn any?
  "Returns true if some element in coll matches the pred"
  [pred coll]
  (boolean (some pred coll)))

(defn butlastv
  "Like (vec (butlast v))' but efficient for vectors"
  [v]
  (let [cnt (count v)]
    (if (< cnt 2)
      []
      (subvec v 0 (dec cnt)))))

(defn count-occurences [matching-fn strings-to-match search-terms]
  (->> strings-to-match
       (map (fn [string-to-match]
              (count (filter #(matching-fn % string-to-match) search-terms))))
       (apply +)))

(def count-exact-occurences    (partial count-occurences =))

(defn distinct-by
  "Like distinct, but calls keyfn to determine distinctness, much like sort-by.
   Takes an optional max-n, indicating how many duplicates are acceptible."
  ([keyfn coll]
     (distinct-by keyfn 1 coll))
  ([keyfn max-n coll]
     (let [step (fn step [xs seen]
                  (lazy-seq
                   ((fn [[f :as xs] seen]
                      (when-let [s (seq xs)]
                        (if (>= (get seen (keyfn f) 0) max-n)
                          (recur (rest s) seen)
                          (cons f (step (rest s) (update-in seen [(keyfn f)] (fnil inc 0)))))))
                    xs seen)))]
       (step coll {}))))

(defmacro doseq-indexed [index-sym [item-sym coll] & body]
  `(doseq [[~item-sym ~index-sym] (map vector ~coll (range))]
     ~@body))

(defn ensure-sequential
  "Returns x as [x] if x is not sequential, otherwise return x untouched."
  [x]
  (if (or (nil? x) (sequential? x))
    x
    [x]))

(defn indexed
  "Returns a lazy sequence of [index, item] pairs, where items come
  from 's' and indexes count up from zero.

  (indexed '(a b c d))  =>  ([0 a] [1 b] [2 c] [3 d])"
  [s]
  (map vector (iterate inc 0) s))

(defn map-nth [f n coll]
  (map-indexed (fn [idx x]
                 (if (zero? (rem (inc idx) n))
                   (f x)
                   x))
               coll))

(defn max-by
  "Like max, but the comparator is customizable through sort-by-fn"
  [sort-by-fn xs]
  (last (sort-by sort-by-fn xs)))

(defn min-by
  "Like min, but the comparator is customizable through sort-by-fn"
  [sort-by-fn xs]
  (first (sort-by sort-by-fn xs)))

(defn only
  "Gives the sole element of a sequence"
  [coll]
  (if (seq (rest coll))
    (throw (RuntimeException. "should have precisely one item, but had at least 2"))
    (if (seq coll)
      (first coll)
      (throw (RuntimeException. "should have precisely one item, but had 0")))))

(defn rand-take
  "Randomly takes at most n elements from coll"
  [coll n]
  (cond (nil? coll)
        []

        (> n (count coll))
        (shuffle coll)

        :else
        (take n (shuffle coll))))

(defn realize
  "Fully realize the specified collection(could be nested)"
  [x]
  (walk/postwalk identity x))

(defn realized-lazy-seq? [x]
  (and (instance? clojure.lang.LazySeq x)
       (realized? x)))

(defn segregate
  "Splits the collection into two collections of the same type. The first
   collection contains all elements that pass the predicate and the second
   collection all the items that fail the predicate."
  [pred coll]
  (reduce (fn [[passes fails] elem]
            (if (pred elem)
              [(conj passes elem) fails]
              [passes (conj fails elem)]))
          [(empty coll) (empty coll)]
          coll))

(defn seq-to-map
  "Transforms a seq of ([key1 value1] [key2 value2]) pairs to a map
   {key1 value1 key2 value2}. For empty and nil values, returns nil."
  [coll]
  (when (seq coll)
    (into {} coll)))

(defn zip
  "[[:a 1] [:b 2] [:c 3]] ;=> [[:a :b :c] [1 2 3]]"
  [seqs]
  (if (empty? seqs)
    []
    (apply map list seqs)))

(defn map-all [f & colls]
  (lazy-seq
  (when (some seq colls)
    (cons (apply f (map first colls))
          (apply map-all f (map rest colls))))))

(defn flatten-seqs
  "Given a vector, return a vector with seqs
  and seqs-of-seqs flattened recursively.
  (seqs in sub-vectors will not be flattened.)"
  [xs]
  (vec
   (loop [xs xs]
     (if (some seq? xs)
       (recur (mapcat #(if (seq? %) % (list %))
                      xs))
       xs))))
