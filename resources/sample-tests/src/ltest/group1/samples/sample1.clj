(ns ltest.group1.samples.sample1
  (:require
    [clojure.test :refer [are deftest is testing]]
    [ltest.core :refer [run-test run-tests run-suite run-suites]]))

(deftest single-pass-test
  (is true))

(deftest single-fail-test
  (is false))

(deftest single-error-test
  (throw (new Exception "oops")))
