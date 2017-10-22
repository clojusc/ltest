(ns ltest.group2.samples.sample6
  (:require
    [clojure.test :refer [are deftest is testing]]
    [ltest.core :refer [run-test run-tests run-suite run-suites]]))

(deftest multiple-pass-test
  (is true)
  (is true))

(deftest some-fail-test
  (is true)
  (is false)
  (is true)
  (is true)
  (is false)
  (is false))

(deftest some-fail-error-test
  (testing "test"
    (throw (new Exception "oops 1")))
  (testing "more test"
    (throw (new Exception "oops 2"))))
