(ns quanta.calendar.nil-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [quanta.calendar.ds.nil :as n]))

(deftest fill-forward-test
  (testing "fill-forward"
    (is (= [1 2 3 3 5] (n/fill-forward [1 2 3 nil 5])))
    (is (= [1 2 3 3 3 6] (n/fill-forward [1 2 3 nil nil 6])))
    (is (= [nil 2 3 3] (n/fill-forward [nil 2 3 nil])))))

(deftest fill-start-backward-test
  (testing "fill-start-backward"
    (is (= [2 2 3 nil 5] (n/fill-start-backward [nil 2 3 nil 5])))
    (is (= [3 3 3 nil nil 6] (n/fill-start-backward [nil nil 3 nil nil 6])))))

(deftest fill-forward-start-backward-test
  (testing "fill-forward-start-backward"
    (is (= [2 2 3 3 5] (n/fill-forward-start-backward [nil 2 3 nil 5])))
    (is (= [nil nil nil] (n/fill-forward-start-backward [nil nil nil])))))