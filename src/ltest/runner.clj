(ns ltest.runner
  "The ltest runner.

  Note that the default reporter used by ltest runner is actually the
  `clojure.test` reporter. This is done in order to allow easy inclusion of
  ltest in other projects without stomping on that project's default runner.

  To use the ltest runner, simply pass `:report runner/report` in the options
  when calling the test runner functions below. This is done for you
  automatically by the test runner wrapper functions in `ltest.core`."
  (:require
    [clansi :as ansi]
    [clojure.stacktrace :as stack]
    [clojure.string :as string]
    [clojure.template :as temp]
    [clojure.test :as test]
    [ltest.constants :as const]
    [ltest.styles :as styles]
    [ltest.util :as util]
    [potemkin :refer [import-vars]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Global vars   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ^:dynamic *style* (atom styles/dark-style))
(def ^:dynamic *tests* (atom 0))
(def ^:dynamic *passed* (atom 0))
(def ^:dynamic *errors* (atom []))
(def ^:dynamic *failures* (atom []))
(def ^:dynamic *assertion-count* (atom 0))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Support/utility functions   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn reset-counters!
  "Reset the global counter values."
  ([]
    (reset-counters! nil))
  ([data]
    (reset! *tests* 0)
    (reset! *passed* 0)
    (reset! *errors* [])
    (reset! *failures* [])
    data))

(defn color-status
  "Color the test status according to the configured style."
  [status]
  (condp = status
    const/pass (styles/style *style* :pass status)
    const/fail (styles/style *style* :fail status)
    const/error (styles/style *style* :error status)))

(defn make-line
  "Fornmat a reporting line."
  ([line postfix status]
    (make-line line postfix const/min-elide status))
  ([line postfix elide-count status]
    (format "%s%s %s [%s]"
            line
            (ansi/style postfix :blue)
            (styles/style *style*
                          :elipsis
                          (string/join (repeat elide-count ".")))
            (color-status status))))

(defn line-format
  "Sadly, padded string support in `format` isn't quite as flexible as we
  would like, thus this function. This might be possible to do more elgantly
  with Clojure's Common-Lisp-inspired `cl-format` ..."
  ([prefix idx status]
   (line-format prefix idx "" status))
  ([prefix idx postfix status]
   (let [line (format "%s assertion %s" prefix idx)
         len (+ (count line)
                (count postfix)
                const/elide-space
                const/status-brackets
                (count status))]
     (if (< (+ len const/min-elide) const/max-len)
       (make-line line postfix (- const/max-len len) status)
       (make-line line postfix status))))
  ([prefix idx file line status]
   (line-format prefix idx (format " (%s:%s)" file line) status)))

(defn get-test-ns
  "Extract the test's namespace."
  ([]
    (get-test-ns (meta (first test/*testing-vars*))))
  ([m]
    (ns-name (:ns m))))

(defn get-test-name
  "Extract the test's name."
  ([]
    (get-test-name {:var (first test/*testing-vars*)}))
  ([m]
    (:name (meta (:var m)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Reporter implementation/overrides   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmulti
  ^{:doc "Generic reporting function, may be overridden to plug in
   different report formats (e.g., TAP, JUnit).  Assertions such as
   'is' call 'report' to indicate results.  The argument given to
   'report' will be a map with a :type key.  See the documentation at
   the top of test_is.clj for more information on the types of
   arguments for 'report'."
     :dynamic true}
  report :type)

(defmethod report :default
  [m]
  ;; XXX With the following un-commented, reports of type
  ;;     `:clojure.test.check.clojure-test/trial` were being printed
  ; (test/with-test-out
  ;   (prn m))
  )

(defmethod report :pass
  [m]
  (test/with-test-out
    (test/inc-report-counter :pass)
    (swap! *passed* inc)
    (swap! *assertion-count* inc)
    (println
      (line-format const/assertion-indent
                   @*assertion-count*
                   const/pass))))

(defmethod report :fail
  [m]
  (swap! *failures*
         conj
         (assoc m
                :ns (get-test-ns)
                :test (get-test-name)))
  (test/with-test-out
    (test/inc-report-counter :fail)
    (swap! *assertion-count* inc)
    (println
      (line-format const/assertion-indent
                   @*assertion-count*
                   (:file m)
                   (:line m)
                   const/fail))))

(defmethod report :error
  [m]
  (swap! *errors*
         conj
         (assoc m
                :ns (get-test-ns)
                :test (get-test-name)))
  (test/with-test-out
    (test/inc-report-counter :error)
    (swap! *assertion-count* inc)
    (println
      (line-format const/assertion-indent
                   @*assertion-count*
                   (:file m)
                   (:line m)
                   const/error))))

(defmethod report :begin-test-ns
  [m]
  (test/with-test-out
    (println const/ns-indent
             (styles/style *style*
                           :ns
                           (get-test-ns m)))))

(defmethod report :end-test-ns
  [m])

(defmethod report :begin-test-var
  [m]
  (swap! *tests* inc)
  (test/with-test-out
    (println const/test-indent
             (styles/style *style*
                           :test
                           (get-test-name m)))))

(defmethod report :end-test-var
  [m]
  (reset! *assertion-count* 0))

(defmethod report :summary [m]
  (test/with-test-out
   (println "\nRan" (:test m) "tests containing"
            (+ (:pass m) (:fail m) (:error m)) "assertions.")
   (println (:fail m) "failures," (:error m) "errors.")))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Test runner implementation/overrides   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn run-test
  "Run a single test given in the form of a var, e.g.:

    #'my.lib.tests.ns/my-test."
  ([ns-test-var]
    (run-test ns-test-var {}))
  ([ns-test-var opts]
    (binding [test/report (:report opts test/report)]
      (test/test-vars [ns-test-var])
      (let [results {:test @*tests*
                     :pass @*passed*
                     :failures @*failures*
                     :errors @*errors*}]
        (-> results
            (reset-counters!)
            (assoc :fail (count (:failures results))
                   :error (count (:errors results))))))))

(defn run-tests
  "Runs all tests in the given namespaces; prints results. Defaults to current
  namespace if none given. Returns a map summarizing test results. Note that
  given namespaces may be a vector of strings, symbols, or vars."
  ([]
    (ltest.runner/run-tests [*ns*]))
  ([nss]
    (ltest.runner/run-tests nss {}))
  ([nss opts]
    (binding [test/report (:report opts test/report)]
      (let [namespaces (map util/get-ns nss)
            results (apply merge-with + (map test/test-ns namespaces))]
        (-> results
            (assoc :errors @*errors*
                   :failures @*failures*)
            (reset-counters!))))))

(defn run-all-tests
  "Runs all tests in all namespaces; prints results.
  Optional argument is a regular expression; only namespaces with
  names matching the regular expression (with re-matches) will be
  tested."
  ([]
    (ltest.runner/run-all-tests {}))
  ([opts]
    (ltest.runner/run-tests (util/all-ns-sorted) opts))
  ([re opts]
    (ltest.runner/run-tests
      (filter #(re-matches re (name (ns-name %))) (util/all-ns-sorted))
      opts)))

(defn run-suite
  "Run a suite of tests."
  ([suite]
    (apply run-suite (conj (util/extract-suite suite) {})))
  ([suite opts]
    (apply run-suite (conj (util/extract-suite suite) opts)))
  ([suite-name nss opts]
    (run-suite suite-name nss nil opts))
  ([suite-name nss action-fn opts]
    (run-suite suite-name nss action-fn util/default-group-formatter opts))
  ([suite-name nss action-fn format-fn opts]
    (run-suite suite-name nss action-fn format-fn util/default-grouper opts))
  ([suite-name nss action-fn format-fn grouper-fn opts]
    (println (styles/style *style* :divider (str "\n" util/divider)))
    (println (styles/style *style* :suite suite-name))
    (println (styles/style *style* :divider util/divider))
    (as-> nss data
          (util/do-grouped-nss data
                               (or action-fn ltest.runner/run-tests)
                               format-fn
                               grouper-fn
                               (merge {:style *style*} opts))
          (flatten data))))

(defn run-suites
  "Run a collection of suites of tests."
  ([suites]
    (run-suites suites {}))
  ([suites opts]
    (->> suites
         (map #(run-suite % opts))
         (flatten))))
