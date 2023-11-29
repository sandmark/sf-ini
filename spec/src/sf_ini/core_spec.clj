(ns sf-ini.core-spec
  (:require
   [clojure.spec.alpha :as s]
   [clojure.string :as str]
   [sf-ini.core :as sut])
  (:import
   (sf_ini.core Property)))

(s/def ::dirname string?)
(s/def ::ini-filename #(str/ends-with? % "ini"))
(s/def ::ini-file (s/and ::ini-filename #(instance? java.io.File %)))
(s/def ::target ::ini-filename)
(s/def ::source ::ini-filename)
(s/def ::target-source-map (s/keys :req-un [::target ::source ::ini-file]))

(s/def ::ini-key string?)
(s/def ::ini-value #(instance? Property %))
(s/def ::raw-value string?)
(s/def ::ini-section-name string?)
(s/def ::ini-section (s/map-of ::ini-key ::ini-value))
(s/def ::ini (s/map-of ::ini-section-name ::ini-section))

(s/def ::target-ini-pair (s/tuple ::target ::ini))

(s/fdef sut/find-ini-files
  :args (s/cat :dir ::dirname)
  :ret (s/coll-of ::ini-file))

(s/fdef sut/with-target-source
  :args (s/cat :file ::ini-file)
  :ret ::target-source-map)

(s/fdef sut/reduce-ini-prop
  :args (s/cat :f ifn? :coll map?)
  :ret map?)

(s/fdef sut/read-ini
  :args (s/cat :m ::target-source-map)
  :ret ::target-ini-pair)

(s/fdef sut/fold
  :args (s/cat :coll (s/coll-of ::target-ini-pair))
  :ret map?)

(s/fdef sut/merge!
  :args (s/cat :coll map?))
