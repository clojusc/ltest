(ns ltest.core
  (:require
    [ltest.runner :as runner :refer [*style*]]
    [ltest.styles :as styles]
    [ltest.util :as util]
    [potemkin :refer [import-vars]]))

(import-vars
  [ltest.runner
    run-test
    run-tests])

(defn run-suite
  "Run a suite of tests."
  ([suite]
    (apply run-suite (util/extract-suite suite)))
  ([suite-name nss action-fn]
    (run-suite suite-name nss action-fn util/default-group-formatter))
  ([suite-name nss action-fn format-fn]
    (run-suite suite-name nss action-fn format-fn util/default-grouper))
  ([suite-name nss action-fn format-fn grouper-fn]
    (println (styles/style *style* :divider (str "\n" util/divider)))
    (println (styles/style *style* :suite suite-name))
    (println (styles/style *style* :divider util/divider))
    (as-> nss data
          (util/do-grouped-nss data
                           (or action-fn run-tests)
                           format-fn
                           grouper-fn
                           {:style *style*})
          (reduce conj [] data))))

(defn run-suites
  "Run a collection of suites of tests."
  [suites]
  (->> suites
       (map run-suite)
       (reduce conj [])
       (flatten)
       (runner/show-summary)
       (runner/show-failures)
       (runner/show-errors))
  :ok)
