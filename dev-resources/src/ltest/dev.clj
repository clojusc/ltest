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
  [{:name "Arbitrary Group 1"
    :nss ['sample0 'sample1]}
   {:name "Arbitrary Group 2"
    :nss [:sample2 "sample3"]}])

;; Now run the following:
;;
;;   (ltest/run-suites suites)
