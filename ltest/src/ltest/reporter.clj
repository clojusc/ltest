(ns ltest.reporter
  (:require
    [io.aviso.exception :as pretty-ex]
    [ltest.runner :as runner :refer [*style*]]
    [ltest.styles :as styles]
    [ltest.util :as util]))

(defn show-summary
  [results]
  (println)
  (println util/divider)
  (println "Results")
  (println util/divider)
  (println)
  (println "Total tests:" (reduce + 0 (map :test results)))
  (println "Assertion passes:" (reduce + 0 (map :pass results)))
  (println "Assertion failures:" (reduce + 0 (map :fail results)))
  (println "Assertion errors:" (reduce + 0 (map :error results)))
  (println)
  results)

(defn show-failure
  [result]
  (println (format "Test: %s/%s"
                   (styles/style *style* :ns (:ns result))
                   (styles/style *style* :test (:test result))))
  (println "File:" (:file result))
  (println "Line number:" (:line result))
  (println (format "%s: %s"
                   (styles/style *style* :pass "Expected")
                   (:expected result)))
  (println (format "%s: %s"
                   (styles/style *style* :fail "Actual")
                    (:actual result)))
  (println util/subdivider))

(defn show-failure-set
  [result]
  (run! show-failure result))

(defn show-error
  [result]
  (println (format "Test: %s/%s"
                   (styles/style *style* :ns (:ns result))
                   (styles/style *style* :test (:test result))))
  (println "File:" (:file result))
  (println "Line number:" (:line result))
  (println (format "%s: %s"
                   (styles/style *style* :error-header "Message")
                   (:message result)))
  (println (format "%s: %s"
                   (styles/style *style* :error-header "Error")
                   (:actual result)))
  (pretty-ex/write-exception (:actual result))
  (println util/subdivider))

(defn show-error-set
  [result]
  (run! show-error result))

(defn show-failures
  [results]
  (when (pos? (reduce + 0 (map :fail results)))
    (println (styles/style *style* :fail-divider util/divider))
    (println (styles/style *style* :fail-header "Failures"))
    (println (styles/style *style* :fail-divider util/divider))
    (println)
    (dorun
      (->> results
           (map :failures)
           (remove empty?)
           (map show-failure-set)))
    (println))
  results)

(defn show-errors
  [results]
  (when (pos? (reduce + 0 (map :error results)))
    (println (styles/style *style* :error-divider util/divider))
    (println (styles/style *style* :error-header "Errors"))
    (println (styles/style *style* :error-divider util/divider))
    (println)
    (dorun
      (->> results
           (map :errors)
           (remove empty?)
           (map show-error-set)))
    (println))
  results)

(defn do-reports
  ""
  [results]
  (dorun
    (-> results
        (show-failures)
        (show-errors)
        (show-summary))))
