(ns ltest.core
  (:require
    [ltest.runner :refer [*style*]]
    [ltest.styles :as styles]
    [ltest.util :as util]
    [potemkin :refer [import-vars]]))

(import-vars
  [ltest.runner
    run-test
    run-tests])

(defn run-suite
  ""
  ([suite-name nss action-fn]
    (run-suite suite-name nss action-fn util/default-group-formatter))
  ([suite-name nss action-fn format-fn]
    (run-suite suite-name nss action-fn format-fn util/default-grouper))
  ([suite-name nss action-fn format-fn grouper-fn]
    (println (styles/style *style* :divider (str "\n" util/divider)))
    (println (styles/style *style* :suite suite-name))
    (println (styles/style *style* :divider util/divider))
    (doall
      (util/do-grouped-nss nss
                           (or action-fn run-tests)
                           format-fn
                           grouper-fn
                           {:style *style*}))))

(defn run-suites
  ""
  [suites]
  (map
    #(run-suite (:name %) (:nss %) (:runner %))
    suites))
