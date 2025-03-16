(ns quanta.calendar.env.scheduler
  (:require
   [tick.core :as t]
   [missionary.core :as m]
   [quanta.calendar.interval :as i]
   [quanta.calendar.db.calendars :refer [get-calendar-list]]
   [quanta.calendar.db.interval :refer [last-finished-close all-intervals]])
  (:import [missionary Cancelled]))

(defn scheduler
  "returns a missionary flow
   fires current and all upcoming timestamps for a calendar"
  [calendar]
  (let [current (last-finished-close calendar (t/instant))
        f (m/ap
           (println "live-calendar " calendar " starting with: " (i/current current))
           (try
             (loop [c (i/move-next current)]
               (let [current-dt-long (-> (t/instant) t/long)
                     next-dt-instant  (-> c i/current :close)
                     next-dt-long  (t/long next-dt-instant)
                     diff-ms (* 1000 (- next-dt-long current-dt-long))]
                 (when (> diff-ms 0)
                   (println "live-calendar " calendar " sleeping for ms: " diff-ms " until: " next-dt-instant)
                   (m/? (m/sleep diff-ms c))))
               (m/amb
                c
                (recur (i/move-next c))))
             (catch Cancelled _cancel
               (println "live-calendar " calendar " stopped.")
               (m/amb))))]
    (->> f
         (m/reductions {} current)
         ;(m/reductions (fn [r v] v) (i/current current))
         ;(m/relieve {})
         (m/signal))))

(defn delayed-scheduler [calendar delay-ms]
  (let [current (last-finished-close calendar (t/instant))
        f (scheduler calendar)
        delayed-f (m/ap
                   (let [cur (m/?> f)]
                     (m/? (m/sleep delay-ms cur))))]
    (->> delayed-f
         (m/reductions {} current)
             ;(m/reductions (fn [r v] v) (i/current current))
             ;(m/relieve {})
         (m/signal))))

(defn create-calendar-flow-dict []
  (->> (for [c (get-calendar-list)
             i all-intervals]
         (let [cal [c i]]
           [cal (scheduler cal)]))
       (into {})))

(def calendar-dict (create-calendar-flow-dict))

calendar-dict

(defn get-calendar-flow
  ([calendar]
   (get calendar-dict calendar))
  ([calendar delay-ms]
   (delayed-scheduler calendar delay-ms)))

(defn as-close-date-flow [cal-flow]
  (->> cal-flow
       (m/eduction
        (map i/current)
        (map :close))
       (m/signal)))

(comment

  (m/? (->> (get-calendar-flow [:forex :m])
            (m/eduction (take 1)
                        (map i/current)
                        (map :close))
            (m/reduce conj)))

  (m/? (->> (get-calendar-flow [:forex :m] 3000)
            (m/eduction (take 1)
                        (map i/current)
                        (map :close))
            (m/reduce conj)))

  (do (println "start time: " (t/instant))
      (println "Result: "
               (m/? (->> ;(scheduler [:forex :m])
                     (get-calendar-flow [:crypto :m])
                     (m/eduction (take 2)
                                 (map i/current)
                                 (map :close))
                     (m/reduce conj))))
      (println "end time: " (t/instant)))

  (m/? (->> (scheduler [:crypto :m])
            (m/eduction (take 2))
            (m/reduce conj)))

;
  )
