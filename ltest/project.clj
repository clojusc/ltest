(defproject clojusc/ltest "0.4.0-SNAPSHOT"
  :description "A custom test runner for clojure.test with detailed, coloured output and summaries"
  :url "https://github.com/clojusc/ltest"
  :license {
    :name "Apache License, Version 2.0"
    :url "http://www.apache.org/licenses/LICENSE-2.0"}
  :exclusions [
    [org.clojure/clojure]]
  :dependencies [
    [clansi "1.0.0"]
    [io.aviso/pretty "0.1.36"]
    [org.clojure/clojure "1.10.0"]
    [org.clojure/tools.namespace "0.2.11"]
    [potemkin "0.4.5"]]
  :profiles {
    :ubercompile {
      :aot :all}
    :dev {
      :plugins [
        [jonase/eastwood "0.3.4"]
        [lein-ancient "0.6.15"]
        [lein-kibit "0.1.6"]
        [lein-shell "0.5.0"]]
      :source-paths [
        "dev-resources/src"
        "resources/sample-tests/src"]
      :repl-options {
        :init-ns ltest.dev
        :welcome
          ~(do
              (println (slurp "resources/text/banner.txt"))
              (println (slurp "resources/text/loading.txt")))}}}
  :aliases {
    "ubercompile" ["with-profile" "+ubercompile" "compile"]
    "check-vers" ["with-profile" "+test" "ancient" "check" ":all"]
    "check-jars" ["with-profile" "+test" "do"
      ["deps" ":tree"]
      ["deps" ":plugin-tree"]]
    "check-deps" ["do"
      ["check-jars"]
      ["check-vers"]]
    "kibit" ["with-profile" "+lint" "kibit"]
    "eastwood" ["with-profile" "+lint" "eastwood" "{:namespaces [:source-paths]}"]
    "lint" ["do" ["check"] ["kibit"] ["eastwood"]]
    "build" ["with-profile" "+test" "do"
      ["check-deps"]
      ["lint"]
      ["ubercompile"]
      ["clean"]
      ["uberjar"]
      ["clean"]
      ["test"]]})
