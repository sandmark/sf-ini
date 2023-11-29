(ns sf-ini.core-test
  (:require
   [clojure.test :as t :refer [deftest is testing]]
   [matcher-combinators.clj-test]
   [matcher-combinators.matchers :as m]
   [sf-ini.core :as sut]
   [orchestra.spec.test :as st]))

(defn unstrument [f]
  (with-out-str
    (st/unstrument)
    (f)
    (st/instrument)))

(t/use-fixtures :once unstrument)

(deftest merge-test
  (testing "Fold"
    (is (match? {:a (m/in-any-order [{:map :a} {:map :b}])
                 :b (m/in-any-order [{:map :c}])}
                (sut/fold [[:a {:map :a}]
                           [:a {:map :b}]
                           [:b {:map :c}]])))))

(deftest concat-test
  (testing "Concatanate INI"
    (is (match? {"Archive" {"bInvalidateOlderFiles"  {:source #{"base.ini" "custom.ini"} :value #{"1"}}
                            "sResourceDataDirsFinal" {:source "custom.ini" :value ""}}
                 "Wwise"   {"iDefaultExternalCodecID" {:source "custom.ini" :value "4"}}}
                (sut/deep-merge-with (comp set vector)
                                     {"Archive" {"bInvalidateOlderFiles" {:source "base.ini" :value "1"}}}
                                     {"Archive" {"bInvalidateOlderFiles"  {:source "custom.ini" :value "1"}
                                                 "sResourceDataDirsFinal" {:source "custom.ini" :value ""}}
                                      "Wwise"   {"iDefaultExternalCodecID" {:source "custom.ini" :value "4"}}})))))
