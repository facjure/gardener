(defproject facjure/gardener "0.3.0"
  :description "A tiny clojurescript sdk"
  :url "https://github.com/facjure/gardener"
  :license {:name "Eclipse Public License" :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.8.1"
  :global-vars {*warn-on-reflection* true}
  :dependencies [[org.clojure/clojure "1.10.0" :scope "provided"]
                 [org.clojure/clojurescript "1.10.520"]
                 [org.clojure/core.async "0.4.500"]
                 [binaryage/devtools "0.9.10"]]
  :clean-targets ^{:protect false} ["target"]
  :profiles {:dev {:source-paths ["src" "test" "dev"]
                   :dependencies [[criterium "0.4.1"]]
                   :plugins [[lein-npm "0.6.2"]]}}
  :aliases {"build-cljs" ["run" "-m" "user/build"]
            "node-repl" ["run" "-m" "user/node-repl"]
            "test-clj" ["do" "clean," "run" "-m" "user/build," "test-refresh"]
            "test-cljs" ["run" "-m" "gardener.tests/test-all"]})
