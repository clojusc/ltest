(ns nogroup
  (:require
    [clojure.test :refer [are deftest is testing]]
    [ltest.core :refer [run-test run-tests run-suite run-suites]]))

(deftest some-fail-error-test
  (testing "not so super-great tests"
    (is (false? true))
    (is (= (inc 1) 3))
    (is true))
  (testing "more somewhat less than awesome tests"
    (is true)
    (is false))
  (testing "nasty test"
    (throw (new Exception "oops 1")))
  (testing "more nasty tests"
    (throw (new Exception "oops 2"))))
