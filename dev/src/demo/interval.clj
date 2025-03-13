(ns demo.interval
  (:require
   [tick.core :as t]
   [quanta.calendar.interval :as i]
   [quanta.calendar.db.interval :refer [next-upcoming-close]]))

(def dt (t/instant))

dt

(def fx-d (next-upcoming-close [:forex :d] dt))
(def fx-h (next-upcoming-close [:forex :h] dt))

(take 10 (i/next-seq fx-d))
(take 10 (i/next-seq fx-h))

dt

(-> (t/zoned-date-time))

(defn to-est [dt-str]
  (t/in (t/date-time dt-str) "America/New_York"))

(def dt2
  (-> (to-est "2025-03-13T16:30:00")
      (t/instant)))

dt2

(def fx-d2 (next-upcoming-close [:forex :d] dt2))

(take 2 (i/next-seq fx-d2))

(t/instant)