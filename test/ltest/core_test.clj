(ns ltest.core-test
  (:require
    [clojure.test :refer :all]
    [ltest.core :refer :all]))

(deftest placeholder
  (testing "Placeholder"
    (is (= 1 1))))
