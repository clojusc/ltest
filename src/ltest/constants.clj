(ns ltest.constants)

(def ns-prefix (apply str (repeat 2 " ")))
(def test-prefix (apply str (repeat 4 " ")))
(def assertion-prefix (apply str (repeat 6 " ")))

(def max-len 80)
(def min-elide 3)
(def elide-space 2)
(def status-brackets 2)

(def pass "OK")
(def fail "FAIL")
(def error "ERROR")
