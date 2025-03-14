(ns quanta.calendar.upcoming-future-test
  (:require
   [clojure.test :refer :all]
   [tick.core :as t]
   [quanta.calendar.db.interval :refer [next-upcoming-close-instant
                                        last-finished-close-instant]]))

(defn upcoming-is-future? [market dt]
  (let [close (next-upcoming-close-instant market dt)]
    (t/> close dt)))

(defn finished-is-past? [market dt]
  (let [close (last-finished-close-instant market dt)]
    (t/<= close dt)))

(def dt (t/instant "2025-03-13T15:09:02Z"))

(defn to-est [dt-str]
  (t/in (t/date-time dt-str) "America/New_York"))

(def dt-forex-close
  (-> (to-est "2025-03-13T16:30:00") (t/instant)))

(deftest upcoming-close-is-in-the-future
  (testing "upcoming daily (is future)"
    (is (upcoming-is-future? [:forex :d] dt))
    (is (upcoming-is-future? [:crypto :d] dt))
    (is (upcoming-is-future? [:eu :d] dt))
    (is (upcoming-is-future? [:us :d] dt))
    (is (upcoming-is-future? [:forex :d] dt-forex-close))
    (is (upcoming-is-future? [:crypto :d] dt-forex-close))
    (is (upcoming-is-future? [:eu :d] dt-forex-close))
    (is (upcoming-is-future? [:us :d] dt-forex-close)))
  (testing "finished  daily (is past)"
    (is (finished-is-past? [:forex :d] dt))
    (is (finished-is-past? [:crypto :d] dt))
    (is (finished-is-past? [:eu :d] dt))
    (is (finished-is-past? [:us :d] dt))
    (is (finished-is-past? [:forex :d] dt-forex-close))
    (is (finished-is-past? [:crypto :d] dt-forex-close))
    (is (finished-is-past? [:eu :d] dt-forex-close))
    (is (finished-is-past? [:us :d] dt-forex-close)))

  (testing "upcoming hour (is future)"
    (is (upcoming-is-future? [:forex :d] dt))))


