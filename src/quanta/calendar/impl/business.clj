(ns quanta.calendar.impl.business
  (:require
   [tick.core :as t]))

(defprotocol interval
  (current [this])
  (move-next [this])
  (move-prior [this])
  (next-seq [this])
  (prior-seq [this]))

(defn day-open? [week dt]
  (let [day (t/day-of-week dt)]
    (contains? week day)))

(def day1 (t/of-days 1))

(defn next-day [day]
  (t/>> day day1))

(defn prior-day [day]
  (t/<< day day1))


(defrecord businessday [week-days day]
  interval
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
               (recur (prior-day dt))))))
  (next-seq [this]
    (->> (iterate move-next this)
         (map :day)))
  (prior-seq [this]
    (->> (iterate move-prior this)
         (map :day))))


(def week-5
  (sorted-set
   t/MONDAY t/TUESDAY t/WEDNESDAY
   t/THURSDAY t/FRIDAY))

(def week-6
  (sorted-set
   t/MONDAY t/TUESDAY t/WEDNESDAY
   t/THURSDAY t/FRIDAY t/SATURDAY))

(def week-6-sunday
  (sorted-set
   t/SUNDAY t/MONDAY t/TUESDAY
   t/WEDNESDAY t/THURSDAY t/FRIDAY))

(def week-7
  (sorted-set
   t/MONDAY t/TUESDAY t/WEDNESDAY
   t/THURSDAY t/FRIDAY t/SATURDAY t/SUNDAY))

(defn current-or-next-business-day [week dt]
  (let [bd (businessday. week (t/date dt))]
    (if (day-open? week (current bd))
      bd
      (move-next bd))))

(comment
  (def day (current-or-next-business-day week-5 (t/instant))) 
  
  (current day)
  (move-next day)
  (move-prior day)

  (take 10 (next-seq day))
  (take 10 (prior-seq day))

  (def day (current-or-next-business-day week-6-sunday
                                         (t/instant)))

  (take 10 (next-seq day))
  
  
;
  )