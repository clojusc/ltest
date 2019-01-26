(defproject lein-ltest "0.3.0"
  :description "The lein plugin for ltest"
  :url "https://github.com/clojusc/ltest"
  :license {
    :name "Eclipse Public License"
    :url "http://www.eclipse.org/legal/epl-v10.html"}
  :eval-in-leiningen true
  :aliases {
    "build" ["do"
      ["check"]
      ["compile"]
      ["clean"]
      ["jar"]]})
