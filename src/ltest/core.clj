(ns ltest.core
  (:require
    [ltest.reporter :as reporter]
    [ltest.runner :as runner :refer [*style*]]
    [ltest.styles :as styles]
    [ltest.util :as util]))

(defn run-test
  [a-test]
  (dorun
    (->> a-test
         (runner/run-test)
         (conj [])
         (reporter/do-reports)))
  :ok)

(defn run-tests
  [& tests]
  (dorun
    (->> tests
         (apply runner/run-tests)
         (conj [])
         (reporter/do-reports)))
  :ok)

(defn run-suite
  [suite]
  (dorun
    (->> suite
         (runner/run-suite)
         (reporter/do-reports)))
  :ok)

(defn run-suites
  "Run a collection of suites of tests."
  [suites]
  (dorun
    (->> suites
         (runner/run-suites)
         (reporter/do-reports)))
  :ok)
