(ns ltest.group2.samples.sample7
  (:require
    [clojure.test :refer [are deftest is testing]]
    [ltest.core :refer [run-test run-tests run-suite run-suites]]))

(deftest multiple-pass-test
  (testing "super-great tests"
    (is true)
    (is true)
    (is true))
  (testing "more awesome tests"
    (is true)
    (is true))
  (testing "last, but not least:"
    (is true)))

(deftest some-fail-test
  (testing "not so super-great tests"
    (is false)
    (is true))
  (testing "more somewhat less than awesome tests"
    (is true)
    (is false)))

(deftest some-fail-error-test
  (testing "not so super-great tests"
    (is false)
    (is true))
  (testing "more somewhat less than awesome tests"
    (is true)
    (is false))
  (testing "nasty test"
    (throw (new Exception "oops 1")))
  (testing "more nasty tests"
    (throw (new Exception "oops 2"))))
