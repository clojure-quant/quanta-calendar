(ns quanta.calendar.ds-align-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [tablecloth.api :as tc]
   [tick.core :as t]
   [quanta.calendar.ds.window :as w]))

(defn instant-day [d]
  (t/instant (format "2020-01-%02dT00:00:00Z" d)))

(def calendar-ds
  (tc/dataset {:date (mapv instant-day (range 1 11))}))

(def bar-ds
  (tc/dataset {:date [;2 missing calendar dates at start
                      (instant-day 3)
                      (instant-day 4)
                      (instant-day 5)
                      ; 1 gap
                      (instant-day 7)
                      (instant-day 8)
                      ; 2 missing at end
                      ]
               :close [100 101 102 103 104]}))

(def asset-name "EUR")

(deftest create-idx-links-sparse-pattern-test
  (testing "10 calendar dates, 5 bars with gaps at start, middle, and end"
    (is (= 10 (tc/row-count calendar-ds)))
    (is (= 5 (tc/row-count bar-ds)))
    (is (= [nil nil 0 1 2 nil 3 4 nil nil]
           (w/create-idx-links calendar-ds bar-ds)))))

(deftest align-bars-fill-forward-start-backward-test
  (testing "nil links filled from first bar forward, then gaps forward-filled"
    (let [aligned (w/align-bars calendar-ds {asset-name bar-ds} :fill-forward-start-backward)]
      (is (= [0 0 0 1 2 2 3 4 4 4]
             (vec (get aligned asset-name)))))))

(deftest aligned-ds-map-fill-forward-start-backward-test
  (testing "aligned bar rows repeat per fill strategy; :date comes from calendar"
    (let [result (w/aligned-ds-map calendar-ds {asset-name bar-ds} :fill-forward-start-backward)
          aligned (get result asset-name)]
      (is (= 10 (tc/row-count aligned)))
      (is (= (vec (:date calendar-ds))
             (vec (:date aligned))))
      (is (= [100 100 100 101 102 102 103 104 104 104]
             (vec (:close aligned)))))))
