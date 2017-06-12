(ns ltest.constants)

(def ns-indent (apply str (repeat 2 " ")))
(def test-indent (apply str (repeat 4 " ")))
(def assertion-indent (apply str (repeat 6 " ")))

(def max-len 80)
(def min-elide 3)
(def elide-space 2)
(def status-brackets 2)

(def pass "OK")
(def fail "FAIL")
(def error "ERROR")
