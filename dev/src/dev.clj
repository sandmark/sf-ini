(ns dev
  (:require
   [clojure.java.io :as io]
   [clojure.repl :refer :all]
   [clojure.spec.alpha :as s]
   [clojure.tools.namespace.repl :refer [refresh]]
   [orchestra.spec.test :as st]))

(clojure.tools.namespace.repl/set-refresh-dirs "dev/src" "src" "test/src" "spec/src")

(defn suspend-unstrument []
  (with-out-str
    (st/unstrument)))

(defn resume-instrument []
  (with-out-str
    (st/instrument)))
