(ns gardener.async
(:require-macros
 [cljs.core.async.macros :as m :refer [go go-loop alt!]])
(:require
 [cljs.core.async :as async :refer [<! >!]]))

;; Utilities -----

(defn- throw-err [x]
  (if (instance? js/Error x)
    (throw x)
    x))

(defmacro dochan
  "Executes the body in a go-loop with a do block with given binding value from channel.
   Usage: (dochan [value in]
            (foo value)
            ...)"
  [[val chan] & body]
  `(let [chan# ~chan]
     (cljs.core.async.macros/go-loop []
       (if-let [~val (cljs.core.async/<! chan#)]
         (do
           ~@body
           (recur))
         :done))))

(defn drain!
  "Closes and drains a channel. Synchronously returns a collection of
  drained messages."
  [chan]
  (async/close! chan)
  (loop [items []]
    (if-let [item (<!! chan)]
      (recur (conj items item))
      items)))

(defn run-async-task
  "Run a one-off async task in a channel and return a value or error
  on the channel."
  [f & args]
  (let [out (async/chan)
        cb  (fn [err & results]
              (go (if err
                    (>! out err)
                    (>! out results))
                  (async/close! out)))]
    (apply f (concat args [cb]))
    out))

(defmacro <?
  "Convert async errors into exceptions when used in a try-catch
  block. Use in conjunction with run-async-task."
  [expr]
  `(throw-err (cljs.core.async/<! ~expr)))
