(ns ltest.util
  (:require
    [clojure.java.io :as io]
    [clojure.string :as string]
    [clojure.tools.namespace.find :as find]
    [ltest.styles :as styles])
  (:import
   (clojure.lang Keyword)
   (java.util.concurrent TimeUnit)))

(defn bar
  "Create a string of a given length composed of the given character."
  ([chr]
    (bar chr 80))
  ([chr len]
    (string/join (repeat len chr))))

(def divider
  "Section divider string."
  (bar "="))

(def subdivider
  "Section subdivider string."
  (bar "-"))

(defmulti get-ns
  "Given something representing a namespace, convert it to an actual namespace
  object."
  type)

(defmethod get-ns nil
  [an-ns]
  *ns*)

(defmethod get-ns java.lang.String
  [an-ns]
  (get-ns (symbol an-ns)))

(defmethod get-ns clojure.lang.Symbol
  [an-ns]
  (find-ns an-ns))

(defmethod get-ns clojure.lang.Keyword
  [an-ns]
  (get-ns (name an-ns)))

(defmethod get-ns :default
  [an-ns]
  an-ns)

(defn- require-namespaces-in-dir [dir]
  (map (fn [ns] (require ns) (find-ns ns)) (find/find-namespaces-in-dir dir)))

(defn- find-tests-in-namespace [ns]
  (->> ns ns-interns vals (filter (comp :test meta))))

(defn- find-tests-in-dir [dir]
  (mapcat find-tests-in-namespace (require-namespaces-in-dir dir)))

(defmulti find-tests
  "Find test vars specified by a source. The source may be a var, symbol
  namespace or directory path, or a collection of any of the previous types."
  {:arglists '([source])}
  type)

(defmethod find-tests clojure.lang.IPersistentCollection [coll]
  (mapcat find-tests coll))

(defmethod find-tests clojure.lang.Namespace [ns]
  (find-tests-in-namespace ns))

(defmethod find-tests clojure.lang.Symbol [sym]
  (if (namespace sym) (find-tests (find-var sym)) (find-tests-in-namespace sym)))

(defmethod find-tests clojure.lang.Var [var]
  (if (-> var meta :test) (list var)))

(defmethod find-tests java.io.File [dir]
  (find-tests-in-dir dir))

(defmethod find-tests java.lang.String [dir]
  (find-tests-in-dir (io/file dir)))

(defmulti find-test-nss
  "Find test namespaces specified by a source. The source may be a var, symbol
  namespace or directory path, or a collection of any of the previous types."
  {:arglists '([source])}
  type)

(defmethod find-test-nss clojure.lang.IPersistentCollection [coll]
  (mapcat find-tests coll))

(defmethod find-test-nss clojure.lang.Namespace [ns]
  ns)

(defmethod find-test-nss clojure.lang.Symbol [sym]
  (create-ns sym))

(defmethod find-test-nss clojure.lang.Var [var]
  (if (-> var meta :test) (list var)))

(defmethod find-test-nss java.io.File [dir]
  (map (comp :ns meta) (find-tests-in-dir dir)))

(defmethod find-test-nss java.lang.String [dir]
  (map (comp :ns meta) (find-tests-in-dir (io/file dir))))

(defn default-group-formatter
  ""
  [group]
  (println (format "\nTesting %s:\n" group)))

(defmulti ns->app
  "Given a namespace string, return the top-level namespace for the namespace's
  parent application."
  type)

(defmethod ns->app clojure.lang.Namespace
  [an-ns]
  (ns->app (ns-name an-ns)))

(defmethod ns->app clojure.lang.Symbol
  [an-ns]
  (ns->app (str an-ns)))

(defmethod ns->app clojure.lang.Keyword
  [an-ns]
  (ns->app (name an-ns)))

(defmethod ns->app java.lang.String
  [an-ns]
  (->> (string/split an-ns #"\.")
       (take 2)
       (string/join ".")))

(defn default-grouper
  "Given a list of namespaces, group them by sorted application."
  [nss]
  (sort (group-by ns->app nss)))

(defn process-group
  [[group-name nss] action-fn format-fn opts]
  (format-fn group-name)
  (let [results (action-fn nss opts)]
    (println \newline
             (styles/style (:style opts) :subdivider subdivider))
    results))

(defn do-grouped-nss
  "Given a list of namespaces, group them and run the given function against
  each namespace. Optionally provide a group formatting function."
  ([nss action-fn]
    (do-grouped-nss nss action-fn {}))
  ([nss action-fn opts]
    (do-grouped-nss nss action-fn default-group-formatter opts))
  ([nss action-fn format-fn opts]
    (do-grouped-nss nss action-fn format-fn default-grouper opts))
  ([nss action-fn format-fn grouper-fn opts]
    (->> nss
         (grouper-fn)
         (map #(process-group % action-fn format-fn opts)))))

(defn extract-suite
  [suite]
  [(:name suite) (:nss suite) (:runner suite)])

(defn sort-namespaces
  [nss]
  (->> nss
       (map (fn [x] [(str x) x]))
       (sort)
       (map second)))

(defn all-ns-sorted
  []
  (sort-namespaces (all-ns)))

(defn filtered-ns
  [re]
  (filter #(re-matches re (name (ns-name %))) (all-ns-sorted)))

(defn tagged-ns
  ([^Keyword tag]
    (tagged-ns (all-ns-sorted) tag))
  ([nss ^Keyword tag]
    (filter #(tag (meta %)) nss)))

(defn filtered-tagged-ns
  ([tag]
    (tagged-ns tag))
  ([re tag]
    (tagged-ns (filtered-ns re) tag)))

(defn find-nss-sorted
  [arg]
  (sort-namespaces (find-test-nss arg)))

(defn nano->seconds
  [nano]
  (.toSeconds TimeUnit/NANOSECONDS nano))

(defn nano->millis
  [nano]
  (.toMillis TimeUnit/NANOSECONDS nano))

(defn nano->micros
  [nano]
  (.toMicros TimeUnit/NANOSECONDS nano))

(defn nano->human-readable
  [nano]
  (cond (>= (/ nano 1e9) 1) (str (nano->seconds nano) "s")
        (>= (/ nano 1e6) 1) (str (nano->millis  nano) "ms")
        (>= (/ nano 1e3) 1) (str (nano->micros  nano) "μs")
        :else               (str                nano  "ns")))
