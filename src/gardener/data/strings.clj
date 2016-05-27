(ns ^{:doc "Functions that operate on Strings or Keywords."}
    gardener.data.strings
  (:require
    [clojure.string :as str])
  (:import
   (java.net URLDecoder)
   (java.nio ByteBuffer CharBuffer)
   (java.nio.charset Charset CharsetDecoder CodingErrorAction)))

;;; String Functions

(declare nil-str re-matches? split)

(defn downcase [^String s]
  (when s
    (.toLowerCase s)))

(defn all-whitespace? [s]
  (re-matches? #"^\s*$" s))

(defn as-str
  ([] "")
  ([x] (if (instance? clojure.lang.Named x)
         (name x)
         (str x))))

(defn decamelize [s]
  (when s
    (letfn [(hump? [^Character a ^Character b]
              (and a b (Character/isLowerCase a) (Character/isUpperCase b)))
            (smooth-hump [result ch]
              (if (hump? (last result) ch)
                (str result \- ch)
                (str result ch)))]
      (str/lower-case (reduce smooth-hump "" s)))))

(defn decode-string [s]
  (when s
    (let [ds (.replace (URLDecoder/decode (str s) "UTF-8") " " "_")]
      (.replaceAll (re-matcher #"[\W]" ds) "_"))))

(defn domain-name [host-name]
  (let [hns (split host-name #"\.")]
    (if (= 1 (count hns))
      (first hns)
      (nth hns (- (count hns) 2)))))

(defn empty-str
  "Returns nil if x is empty or nil, otherwise x as a string"
  [x]
  (if (or (string? x)
          (sequential? x))
    (when-not (empty? x)
      (str x))
    (nil-str x)))

(defn equals-ignore-case? [^String s1 ^String s2]
  (.equalsIgnoreCase s1 s2))

(defn extract-number [s]
  (when s
    (if (number? s)
      s
      (read-string (last (re-seq #"[\-]?[0-9]+[.,]?[0-9]*" s))))))

(def escape-regex-except-*
  (memoize (fn
             ;;"Escapes special Regex symbols except *."
             [^String s]
             (.. s
                 (replace "\\" "\\\\")
                 (replace "("  "\\(")
                 (replace ")"  "\\)")
                 (replace "."  "\\.")
                 (replace "["  "\\[")
                 (replace "]"  "\\]")
                 (replace "^"  "\\^")
                 (replace "$"  "\\$")
                 (replace "|"  "\\|")
                 (replace "?"  "\\?")
                 (replace "*"  ".*") ;; Only WildChar * will work
                 (replace "+"  "\\+")))))

(defn git-sha? [s]
  (re-matches? #"([a-f]|\d){40}" s))

(defn hyphenize [s]
  (when s
    (str/replace s #"_" "-")))

(defn length [^String s]
  (.length s))

(defn mapify [s sep]
  (when s
    (let [kv (split s sep)]
      (hash-map (first kv) (or (second kv) "")))))

(defn nil-or-empty-string? [s]
  (or (nil? s) (= "" s)))

(defn nil-str [x]
  (when x (str x)))

(defn parse-query-param
  [q]
  (if q
    (reduce (fn [a s] (merge a (mapify s "="))) {} (split q "&"))
    {}))

(defn parse-url [url-str]
  (when-not (empty? url-str)
    (try
      (let [url (java.net.URL. url-str)]
        {:host (.getHost url)
         :query (.getQuery url)})
      (catch java.net.MalformedURLException e
        nil))))

(defn query-string [url-str]
  (when-let [url-m (parse-url url-str)]
    (:query url-m)))

(defn query-url [url param]
  (when-let [query (query-string url)]
    ((parse-query-param query) param)))

(defn remove-dashes [guid]
  (str/replace guid #"-" ""))

(defn repeat-str [n x]
  (apply str (repeat n x)))

(defn re-matches? [re s]
  (if s
    (boolean (re-matches re s))
    false))

(defn safe-string-pattern-re-find [pattern string-to-match]
  (try
    (re-find (re-pattern (str "(?i)" pattern))
             string-to-match)
    (catch java.util.regex.PatternSyntaxException e
      nil)))

(defn split [s delim]
  (cond
   (nil? s)        nil
   (string? delim) (str/split s (re-pattern delim))
   :else           (str/split s delim)))

(definline starts-with? [^String s ^String sub-string]
  `(.startsWith ~s ~sub-string))

(defn stringify-map-values [the-map]
  (let [stringify-entry (fn [[k v]] {k (str v)})]
    (apply merge (map stringify-entry the-map))))

(defn strip-whitespace [s]
  (str/replace s #"[\s\t\r]" ""))

(defn truncate [^String s n]
  (.substring s 0 (min (.length s) n)))

(defn urldecode [s]
  (when s
    (URLDecoder/decode s "UTF-8")))

(defn uuid? [s]
  (re-matches? #"[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}" s))

(defn uuid+timestamp? [s]
  (re-matches? #"[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}-\d{13}" s))


;; Keyword Functions

(defn underscores->hyphens [k]
  (when k
    (-> k name (.replace "_" "-") keyword)))

(defn hyphens->underscores [k]
  (when k
    (-> k name (.replace "-" "_") keyword)))

(defn keyword->hyphenated-string [k]
  (-> k name (.replace "_" "-")))

(defn keyword->hyphenated-keyword [k]
  (-> k keyword->hyphenated-string keyword))

(defn keyword->underscored-string [k]
  (-> k name (.replace "-" "_")))

(defn keyword->underscored-keyword [k]
  (-> k keyword->underscored-string keyword))

;; Performance optimization
(def ^Charset utf8-charset (Charset/forName "UTF-8"))

(defn utf8-bytes [^String s]
  (.getBytes s utf8-charset))

(defn num-utf8-bytes [^String s]
  (alength ^bytes (utf8-bytes s)))

(defn truncate-utf8-bytes [s n]
  (let [bytes (utf8-bytes s)
        decoder (.newDecoder utf8-charset)
        byte-buffer (ByteBuffer/wrap bytes 0 n)
        char-buffer (CharBuffer/allocate n)]
    (.onMalformedInput decoder CodingErrorAction/IGNORE)
    (.decode decoder byte-buffer char-buffer true)
    (.flush decoder char-buffer)
    (String. (.array char-buffer) 0 (.position char-buffer))))

(defn camel-case->dash-sep [^String s]
  (when s
    (let [with-dashes (.replaceAll s
                        "([a-z])([A-Z])" "$1-$2")]
      (.toLowerCase with-dashes))))

(defn ->nice-keyword [^String s]
  (when s
    (-> (.trim s)
      (.replaceAll "_" "-")
      (.replaceAll "\\s+" "-")
      camel-case->dash-sep
      keyword)))

(defn ->nice-keywords [scol]
  (map ->nice-keyword scol))
