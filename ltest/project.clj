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
    [io.aviso/pretty "0.1.34"]
    [org.clojure/clojure "1.8.0"]
    [org.clojure/tools.namespace "0.2.11"]
    [potemkin "0.4.4"]]
  :profiles {
    :ubercompile {
      :aot :all}
    :dev {
      :plugins [
        [jonase/eastwood "0.2.5"]
        [lein-ancient "0.6.14"]
        [lein-bikeshed "0.5.0"]
        [lein-kibit "0.1.5"]
        [lein-shell "0.5.0"]
        [venantius/yagni "0.1.4"]]
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
    "check-deps" ["with-profile" "+test" "ancient" "check" ":all"]
    "kibit" ["do" ["shell" "echo" "== Kibit =="]
                  ["kibit"]]
    "outlaw" ["eastwood" "{:namespaces [:source-paths] :source-paths [\"src\"]}"]
    "lint" ["do" ["check"] ["kibit"] ["outlaw"]]
    "ubercompile" ["with-profile" "+ubercompile" "compile"]
    "build" ["with-profile" "+test" "do"
      ["check-deps"]
      ["lint"]
      ["ubercompile"]
      ["clean"]
      ["uberjar"]
      ["clean"]
      ["test"]]})
