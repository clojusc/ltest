(ns ltest.util-test
  (:require
    [clojure.test :refer [are deftest is testing]]
    [ltest.core :refer [run-test run-tests run-suite run-suites]]
    [ltest.util :as util]))

(deftest nano->human-readable
  (testing "Nanosecond checks"
    (is (= "1ns" (util/nano->human-readable 1)))
    (is (= "10ns" (util/nano->human-readable 10)))
    (is (= "100ns" (util/nano->human-readable 100))))
  (testing "Microsecond checks"
    (is (= "1μs" (util/nano->human-readable 1000)))
    (is (= "10μs" (util/nano->human-readable 10000)))
    (is (= "100μs" (util/nano->human-readable 100000))))
  (testing "Millisecond checks"
    (is (= "1ms" (util/nano->human-readable 1000000)))
    (is (= "10ms" (util/nano->human-readable 10000000)))
    (is (= "100ms" (util/nano->human-readable 100000000))))
  (is (= "1s" (util/nano->human-readable 1000000000))))
