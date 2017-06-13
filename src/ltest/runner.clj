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

(defn run-test
  "Run a single test given in the form of a var, e.g.:

    #'my.lib.tests.ns/my-test."
  [ns-test-var]
  (test/test-vars [ns-test-var])
  (let [results {:test @*tests*
                 :pass @*passed*
                 :failures @*failures*
                 :errors @*errors*}]
    (-> results
        (reset-counters!)
        (assoc :fail (count (:failures results))
               :error (count (:errors results))))))

(defn run-tests
  "Runs all tests in the given namespaces; prints results. Defaults to current
  namespace if none given. Returns a map summarizing test results. Note that
  given namespaces may be a vector of strings, symbols, or vars."
  ([]
    (run-tests [*ns*]))
  ([& nss]
    (let [namespaces (map util/get-ns nss)
          results (apply merge-with + (map test/test-ns namespaces))]
      (-> results
          (assoc :errors @*errors*
                 :failures @*failures*)
          (reset-counters!)))))

(defn run-suite
  "Run a suite of tests."
  ([suite]
    (apply run-suite (util/extract-suite suite)))
  ([suite-name nss]
    (run-suite suite-name nss nil))
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
       (flatten)))
