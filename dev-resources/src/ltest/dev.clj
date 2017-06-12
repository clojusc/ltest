(ns ltest.dev
  (:require
    [clojure.pprint :refer [pprint]]
    [clojure.test :refer [*test-out*]]
    [clojure.tools.namespace.repl :as repl]
    [ltest.core :as ltest]
    [ltest.util :as util]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Sample data   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def suite-1
  {:name "Arbitrary Division 1"
   :nss ['nogroup
         'ltest.group1.samples.sample0
         'ltest.group1.samples.sample1]})

(def suite-2
  {:name "Arbitrary Division 2"
   :nss [:ltest.group1.samples.sample2
         "ltest.group1.samples.sample3"
         'ltest.group2.samples.sample4
         'ltest.group2.samples.sample5
         'ltest.group2.samples.sample6
         'ltest.group2.samples.sample7]})

(def suites
  [suite-1 suite-2])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Examples   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Run a single test:
;;
;;   (ltest/run-test #'ltest.group2.samples.sample7/multiple-pass-test)
;;
;; Run tests:
;;
;;   (ltest/run-tests 'ltest.group2.samples.sample1
;;                    'ltest.group2.samples.sample2)
;;
;; Run a suite:
;;
;;   (ltest/run-suite "My Suite" ['ltest.group1.samples.sample1
;;                                'ltest.group1.samples.sample2])
;;
;; or
;;
;;   (ltest/run-suite suite-1)
;;
;; Run several suites:
;;
;;   (ltest/run-suites suites)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Dev env   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn run
  []
  :ok)

(defn refresh
  ([]
    (repl/refresh))
  ([& args]
    (apply #'repl/refresh args)))

(defn reset []
  (refresh :after 'ltest.dev/run))
