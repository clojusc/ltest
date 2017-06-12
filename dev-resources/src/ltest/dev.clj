(ns ltest.dev
  (:require
    [clojure.pprint :refer [pprint]]
    [clojure.tools.namespace.repl :as repl]
    [ltest.core :as ltest]
    [ltest.util :as util]))

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

(def suites
  [{:name "Arbitrary Division 1"
    :nss ['nogroup
          'ltest.group1.samples.sample0
          'ltest.group1.samples.sample1]}
   {:name "Arbitrary Division 2"
    :nss [:ltest.group1.samples.sample2
          "ltest.group1.samples.sample3"
          'ltest.group2.samples.sample4
          'ltest.group2.samples.sample5
          'ltest.group2.samples.sample6
          'ltest.group2.samples.sample7]}])

;; Now run the following:
;;
;;   (ltest/run-suites suites)
