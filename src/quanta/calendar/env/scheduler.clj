(ns quanta.calendar.env.scheduler
  (:require
   [tick.core :as t]
   [missionary.core :as m]
   [quanta.calendar.interval :as i]
   [quanta.calendar.db.calendars :refer [get-calendar-list]]
   [quanta.calendar.db.interval :refer [next-upcoming-close last-finished-close all-intervals]])
  (:import [missionary Cancelled]))

(defn scheduler
  "returns a missionary flow
   fires current and all upcoming timestamps for a calendar"
  [calendar upcoming?]
   (m/ap
    (let [current (if upcoming? 
                    (next-upcoming-close calendar (t/instant))
                    (last-finished-close calendar (t/instant)))]
      (println "live-calendar " calendar " starting with: " (i/current current))
      (when-not upcoming?
        (m/amb current))
      (try
        (loop [c current]
          (let [current-dt-long (-> (t/instant) t/long)
                next-dt-instant  (-> c i/current :close )
                next-dt-long  (t/long next-dt-instant)
                diff-ms (* 1000 (- next-dt-long current-dt-long))]
            (when (> diff-ms 0)
              (println "live-calendar " calendar " sleeping for ms: " diff-ms " until: " next-dt-instant)
              (m/? (m/sleep diff-ms c))))
          (m/amb
           c
           (recur (i/move-next c))))
        (catch Cancelled cancel
          (println "live-calendar " calendar " stopped."))))))



  ;(m/stream
  ;(m/signal
          )




(defn all-calendars []
  (->> (for [c (get-calendar-list)
             i all-intervals]
         (let [cal [c i]]
           [cal (scheduler cal)]))
       (into {})))

(def calendar-dict (all-calendars))

(defn get-calendar-flow [calendar]
  (get calendar-dict calendar))

(comment

  (m/? (->> (scheduler [:us :d] false)
            (m/eduction (take 1) (map i/current))
            (m/reduce conj)))

  (do (println "start time: " (t/instant))
      (println "Result: "
               (m/? (->> ;(scheduler [:forex :m])
                     (scheduler [:us :m] false)
                     (m/eduction (take 2) (map i/current))
                     (m/reduce conj))))
      (println "end time: " (t/instant)))
  

  (m/? (->> (scheduler [:crypto :m])
            (m/eduction (take 2))
            (m/reduce conj)))

  (get-calendar-flow [:forex :m])
  (get-calendar-flow [:forex :m333])

 ;
  )
