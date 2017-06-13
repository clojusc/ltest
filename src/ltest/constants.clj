(ns ltest.constants
  (:require
    [clojure.string :as string]))

(def ns-indent (string/join (repeat 2 " ")))
(def test-indent (string/join (repeat 4 " ")))
(def assertion-indent (string/join (repeat 6 " ")))

(def max-len 80)
(def min-elide 3)
(def elide-space 2)
(def status-brackets 2)

(def pass "OK")
(def fail "FAIL")
(def error "ERROR")
