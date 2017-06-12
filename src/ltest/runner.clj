(ns ltest.runner
  (:require
    [clansi :as ansi]
    [clojure.stacktrace :as stack]
    [clojure.string :as string]
    [clojure.test :as test]
    [ltest.constants :as const]
    [ltest.styles :as styles]
    [ltest.util :as util]))

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
                          (apply str (repeat elide-count ".")))
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

(defn test-ns
  "Extract the test's namespace."
  ([]
    (test-ns (meta (first test/*testing-vars*))))
  ([m]
    (ns-name (:ns m))))

(defn test-name
  "Extract the test's name."
  ([]
    (test-name {:var (first test/*testing-vars*)}))
  ([m]
    (:name (meta (:var m)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Reporter implementation/overrides   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmethod test/report :default
  [m]
  (test/with-test-out
    (prn m)))

(defmethod test/report :pass
  [m]
  (test/with-test-out
    (test/inc-report-counter :pass)
    (swap! *passed* inc)
    (swap! *assertion-count* inc)
    (println
      (line-format const/assertion-indent
                   @*assertion-count*
                   const/pass))))

(defmethod test/report :fail
  [m]
  (swap! *failures*
         conj
         (assoc m
                :ns (test-ns)
                :test (test-name)))
  (test/with-test-out
    (test/inc-report-counter :fail)
    (swap! *assertion-count* inc)
    (println
      (line-format const/assertion-indent
                   @*assertion-count*
                   (:file m)
                   (:line m)
                   const/fail))))

(defmethod test/report :error
  [m]
  (swap! *errors*
         conj
         (assoc m
                :ns (test-ns)
                :test (test-name)))
  (test/with-test-out
    (test/inc-report-counter :error)
    (swap! *assertion-count* inc)
    (println
      (line-format const/assertion-indent
                   @*assertion-count*
                   (:file m)
                   (:line m)
                   const/error))))

(defmethod test/report :begin-test-ns
  [m]
  (test/with-test-out
    (println const/ns-indent
             (styles/style *style*
                           :ns
                           (test-ns m)))))

(defmethod test/report :end-test-ns
  [m])

(defmethod test/report :begin-test-var
  [m]
  (swap! *tests* inc)
  (test/with-test-out
    (println const/test-indent
             (styles/style *style*
                           :test
                           (test-name m)))))

(defmethod test/report :end-test-var
  [m]
  (reset! *assertion-count* 0))

(defmethod test/report :summary [m]
  (test/with-test-out
   (println "\nRan" (:test m) "tests containing"
            (+ (:pass m) (:fail m) (:error m)) "assertions.")
   (println (:fail m) "failures," (:error m) "errors.")))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Test runner implementation/overrides   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn run-tests
  "Runs all tests in the given namespaces; prints results. Defaults to current
  namespace if none given. Returns a map summarizing test results. Note that
  given namespaces may be a vector of strings, symbols, or vars."
  ([]
    (run-tests [*ns*]))
  ([& nss]
    (let [namespaces (map util/get-ns nss)
          summary (assoc (apply merge-with + (map test/test-ns namespaces))
                    :type :summary)]
      (-> summary
          (assoc :errors @*errors*
                 :failures @*failures*)
          (reset-counters!)))))

(defn run-test
  "Run a single test given in the form of a var, e.g.:

    #'my.lib.tests.ns/my-test."
  [ns-test-var]
  (test/test-vars [ns-test-var])
  (let [result {:tests @*tests*
                :pass @*passed*
                :failures @*failures*
                :errors @*errors*}]
    (reset-counters!)
    result))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Custom summary reporter   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn show-summary
  [results]
  (println)
  (println util/divider)
  (println "Results")
  (println util/divider)
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
  (dorun
    (map show-failure result)))

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
  (println (:actual result))
  (println util/subdivider))

(defn show-error-set
  [result]
  (dorun
    (map show-error result)))

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
