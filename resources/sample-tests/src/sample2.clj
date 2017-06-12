(ns sample2
  (:require
    [clojure.test :refer [are deftest is testing]]
    [ltest.core :refer [run-test run-tests run-suite run-suites]]))

(deftest multiple-pass-test
  (is true)
  (is true)
  (is true)
  (is true)
  (is true)
  (is true)
  (is true)
  (is true)
  (is true)
  (is true)
  (is true)
  (is true)
  (is true)
  (is true))

(deftest some-fail-test
  (is false)
  (is true)
  (is false)
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
    (is false)
    (is false)
    (is true)
    (is false)
    (is true)
    (is false)
    (is true)
    (is true)
    (is true)
    (is false)
    (throw (new Exception "oops 1"))))
