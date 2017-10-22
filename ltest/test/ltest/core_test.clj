(ns ltest.core-test
  (:require
    [clojure.test :refer [are deftest is testing]]
    [ltest.core :refer [run-test run-tests run-suite run-suites]]))

(deftest placeholder
  (testing "Placeholder"
    (is (= 1 1))))
