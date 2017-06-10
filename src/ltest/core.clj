(ns ltest.core
  (:require
    [ltest.styles :as styles]))

(def ^:dynamic *style* (atom styles/dark-style))

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))
