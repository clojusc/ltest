(ns ltest.core
  (:require
    [ltest.reporter :as reporter]
    [ltest.runner :as runner]
    [ltest.styles :as styles]
    [ltest.util :as util]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Namespace utility functions   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn unit-ns
  [re]
  (util/filtered-tagged-ns re :unit))

(defn integration-ns
  [re]
  (util/filtered-tagged-ns re :integration))

(defn system-ns
  [re]
  (util/filtered-tagged-ns re :system))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Test-running functions   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn run-test
  [a-test]
  (dorun
    (->> {:report runner/report}
         (runner/run-test a-test)
         vector
         (reporter/do-reports)))
  @reporter/*status*)

(defn run-tests
  [tests]
  (dorun
    (->> {:report runner/report}
         (runner/run-tests tests)
         vector
         (reporter/do-reports)))
  @reporter/*status*)

(defn run-all-tests
  ([]
    (dorun
      (->> {:report runner/report}
           (runner/run-all-tests)
           vector
           (reporter/do-reports)))
    @reporter/*status*)
  ([re]
    (dorun
      (->> {:report runner/report}
           (runner/run-all-tests re)
           vector
           (reporter/do-reports)))
    @reporter/*status*))

(defn run-suite
  [suite]
  (dorun
    (->> {:report runner/report}
         (runner/run-suite suite)
         (reporter/do-reports)))
  @reporter/*status*)

(defn run-suites
  "Run a collection of suites of tests."
  [suites]
  (dorun
    (->> {:report runner/report}
         (runner/run-suites suites)
         (reporter/do-reports)))
  @reporter/*status*)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Tagged-test-running functions   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn run-unit-tests
  [re]
  (run-tests (unit-ns re)))

(defn run-integration-tests
  [re]
  (run-tests (integration-ns re)))

(defn run-system-tests
  [re]
  (run-tests (unit-ns re)))
