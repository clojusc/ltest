(ns ltest.core
  (:require
    [ltest.reporter :as reporter]
    [ltest.runner :as runner]
    [ltest.styles :as styles]
    [ltest.util :as util]))

(defn run-test
  [a-test]
  (dorun
    (->> {:report runner/report}
         (runner/run-test a-test)
         vector
         (reporter/do-reports)))
  :ok)

(defn run-tests
  [tests]
  (dorun
    (->> {:report runner/report}
         (runner/run-tests tests)
         vector
         (reporter/do-reports)))
  :ok)

(defn run-all-tests
  ([]
    (dorun
      (->> {:report runner/report}
           (runner/run-all-tests)
           vector
           (reporter/do-reports)))
    :ok)
  ([re]
    (dorun
      (->> {:report runner/report}
           (runner/run-all-tests re)
           vector
           (reporter/do-reports)))
    :ok))

(defn run-suite
  [suite]
  (dorun
    (->> {:report runner/report}
         (runner/run-suite suite)
         (reporter/do-reports)))
  :ok)

(defn run-suites
  "Run a collection of suites of tests."
  [suites]
  (dorun
    (->> {:report runner/report}
         (runner/run-suites suites)
         (reporter/do-reports)))
  :ok)
