(ns ltest.styles
  "The intended use for the styles defined in this namespace is for one to be
  used by default (overridable by a user) and set as an atom in the reporter
  namespace, then used by reporting functions that display output to the
  terminal."
  (:require
    [clansi :as ansi]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Styles   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def dark-style
  "The following style is intended for use with terminals with a dark
  background color."
  {:pass #(ansi/style % :green)
   :fail #(ansi/style % :red :bright)
   :error #(ansi/style % :magenta)
   :elipsis #(ansi/style % :green)
   :suite #(ansi/style % :blue :bright)
   :divider #(ansi/style % :blue)
   :subdivider #(ansi/style % :blue)
   :ns #(ansi/style % :yellow :bright)
   :test #(ansi/style % :yellow)})

;;; XXX Add more styles for different terminal types (e.g., light background).
;;;     PRs welcome!

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Utility functions   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn style
  "Convenience function for extracting style information from the style atom."
  [style-atom style-key text]
  ((style-key @style-atom) text))
