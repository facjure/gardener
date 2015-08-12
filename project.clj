(defproject facjure/gardener "0.1.0"
  :description "A utility belt for Garden: CSS in Clojure/Clojurescript."
  :url "https://github.com/priyatam/gardener"
  :license {:name "Eclipse Public License" :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.5.0"
  :global-vars {*warn-on-reflection* true}

  :dependencies                  [[org.clojure/clojure "1.7.0" :scope "provided"]
                                  [org.clojure/clojurescript "1.7.28" :scope "provided"]
                                  [garden "1.3.0"]]

  :npm
  {:dependencies                 [[source-map-support "0.3.1"]]}

  :clean-targets
  ^{:protect false}              ["target"]

  :profiles
  {:dev {:source-paths           ["src" "test" "dev"]
         :dependencies           [[criterium "0.4.1"]]
         :plugins                 [[cider/cider-nrepl "0.9.1"]
                                   [codox "0.8.13"]
                                   [lein-npm "0.6.1"]
                                   [com.jakemccrary/lein-test-refresh "0.10.0"]]}}

  :aliases
  {"build-cljs"                  ["run" "-m" "user/build"]
   "node-repl"                   ["run" "-m" "user/node-repl"]
   "test-clj"                    ["do" "clean," "run" "-m" "user/build," "test-refresh"]
   "test-cljs"                   ["run" "-m" "gardener.tests/test-all"]})
