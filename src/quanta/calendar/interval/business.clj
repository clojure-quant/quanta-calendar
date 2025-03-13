(ns quanta.calendar.interval.business
  (:require
   [tick.core :as t]
   [quanta.calendar.interval :as i]))

(defn day-open? [week dt]
  (let [day (t/day-of-week dt)]
    (contains? week day)))

(def day1 (t/of-days 1))

(defn next-day [day]
  (t/>> day day1))

(defn prior-day [day]
  (t/<< day day1))

(defrecord businessday [week-days day]
  i/interval
  (current [this]
    day)
  (move-next [this]
    (assoc this :day
           (loop [dt (next-day day)]
             (if (day-open? week-days dt)
               dt
               (recur (next-day dt))))))
  (move-prior [this]
    (assoc this :day
           (loop [dt (prior-day day)]
             (if (day-open? week-days dt)
               dt
               (recur (prior-day dt)))))))

(defn next-upcoming-close-business-day [week dt]
  (let [bd (businessday. week (t/date dt))]
    (if (day-open? week (i/current bd))
      bd
      (i/move-next bd))))
