(ns ltest.util
  (:require
    [clojure.string :as string]
    [ltest.styles :as styles]))

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
