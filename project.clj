(defproject clojusc/ltest "0.1.0-SNAPSHOT"
  :description "A custom test runner for clojure.test with detailed, coloured output and summaries"
  :url "https://github.com/clojusc/ltest"
  :license {
    :name "Apache License, Version 2.0"
    :url "http://www.apache.org/licenses/LICENSE-2.0"}
  :dependencies [
    [clansi "1.0.0" :exclusions [org.clojure/clojure]]
    [io.aviso/pretty "0.1.33"]
    [org.clojure/clojure "1.8.0"]
    [potemkin "0.4.3"]]
  :profiles {
    :dev {
      :dependencies [
        [org.clojure/tools.namespace "0.2.11"]]
      :source-paths [
        "dev-resources/src"
        "resources/sample-tests/src"]
      :repl-options {
        :init-ns ltest.dev}}})
