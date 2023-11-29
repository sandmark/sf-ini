(ns sf-ini.core
  (:gen-class)
  (:require
   [clojure-ini.core :as ini]
   [clojure.java.io :as io]
   [clojure.string :as str]
   [sf-ini.config :as config]
   [taoensso.timbre :as log]))

(set! *warn-on-reflection* true)

(defrecord Property [source value])

(def ini-dir (-> config/config :ini-directory))

(defn find-ini-files [dir]
  (let [dir? #(.isDirectory ^java.io.File %)
        ini? #(str/ends-with? % "ini")]
    (->> (tree-seq dir? #(.listFiles ^java.io.File %) (io/file dir))
         (filter (comp not dir?))
         (filter ini?))))

(defn with-target-source [^java.io.File file]
  {:target   (-> file .getParentFile .getName (str ".ini"))
   :source   (-> file .getName)
   :ini-file file})

(defn reduce-ini-prop [f coll]
  (letfn [(inner [m k v] (assoc m k (f v)))
          (outer [ini section prop-map]
            (assoc ini section (reduce-kv inner {} prop-map)))]
    (reduce-kv outer {} coll)))

(defn read-ini [{:keys [target source ini-file]}]
  (let [ini  (ini/read-ini ini-file)
        join #(->Property source %)]
    [target (reduce-ini-prop join ini)]))

(defn fold [coll]
  (reduce (fn [m [k v]] (update m k conj v)) {} coll))

(defn deep-merge-with
  "Like merge-with, but merges maps recursively, applying the given fn
  only when there's a non-map at a particular level.
  (deep-merge-with + {:a {:b {:c 1 :d {:x 1 :y 2}} :e 3} :f 4}
                     {:a {:b {:c 2 :d {:z 9} :z 3} :e 100}})
  -> {:a {:b {:z 3, :c 3, :d {:z 9, :x 1, :y 2}}, :e 103}, :f 4}"
  [f & maps]
  (apply
   (fn m [& maps]
     (if (every? map? maps)
       (apply merge-with m maps)
       (apply f maps)))
   maps))

(defn -main [& _]
  (let [m (->> ini-dir find-ini-files (map with-target-source) (map read-ini) fold)]
    (doseq [ini-file (keys m)]
      (with-open [f (io/writer ini-file)]
        (let [section-map (apply (partial deep-merge-with (comp set vector)) (get m ini-file))]
          (doseq [section (keys section-map)]
            (.write f (format "[%s]\n" section))
            (doseq [[k {:keys [:source :value]}] (get section-map section)]
              (when (< 1 (count value))
                (log/warnf "Conflict: '[%s]->%s' defined with different values on %s"
                           section k source))
              (.write f (str k "="))
              (if (set? value)
                (.write f (str (first (seq value)) "\n"))
                (.write f (str value "\n"))))))))))
