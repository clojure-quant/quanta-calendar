(ns quanta.calendar.window-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [tablecloth.api :as tc]
   [tick.core :as t]
   [quanta.calendar.ds.window :as w]))

(def return-ds
  (tc/dataset {:date [(t/date "2020-01-01") (t/date "2020-01-02") (t/date "2020-01-03") (t/date "2020-01-04")]
               "A" [0.10 -0.10 0.15 0.20]
               "B" [0.00 0.20 0.10 0.15]}))

(deftest create-idx-links-dense-bar-ds-test
  (testing "each calendar date has a matching bar row — indices are bar-ds row positions"
    (let [allocation-ds (tc/dataset {:date [(t/date "2020-01-01") (t/date "2020-01-02") (t/date "2020-01-03") (t/date "2020-01-04")]
                                     "A" [0.5 1.0 0.0 0.0]
                                     "B" [0.5 0.0 1.0 0.0]})]
      (is (= [0 1 2 3] (w/create-idx-links return-ds allocation-ds))))))

(deftest create-idx-links-sparse-bar-ds-test
  (testing "missing bar dates yield nil; bar indices refer to bar-ds rows (not calendar rows)"
    (let [allocation2-ds (tc/dataset {:date [(t/date "2020-01-01") (t/date "2020-01-04")]
                                      "A" [0.5 0.0]
                                      "B" [0.5 1.0]})
          allocation3-ds (tc/dataset {:date [(t/date "2020-01-01") (t/date "2020-01-03")]
                                      "A" [0.5 0.0]
                                      "B" [0.5 1.0]})]
      (is (= [0 nil nil 1] (w/create-idx-links return-ds allocation2-ds)))
      (is (= [0 nil 1 nil] (w/create-idx-links return-ds allocation3-ds))))))

(deftest create-idx-links-after-last-bar-test
  (testing "calendar dates after the last bar date return nil without mixing date types"
    (let [bar-ds (tc/dataset {:date [(t/date "2020-01-01")]
                              "A" [1.0]
                              "B" [0.0]})]
      (is (= [0 nil nil nil] (w/create-idx-links return-ds bar-ds))))))
