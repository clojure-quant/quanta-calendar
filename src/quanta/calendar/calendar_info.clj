(ns quanta.calendar.calendar-info
  (:require
   [tick.core :as t]
   [quanta.calendar.db.calendars :as caldb]
   [quanta.calendar.db.interval :refer [next-upcoming-close-instant last-finished-close-instant]]
   ))

(defn market-info [market-kw dt]
  (let [cal (caldb/get-calendar market-kw)
        dt-cal (-> dt
                   (t/zoned-date-time)
                   (t/in (:timezone cal)))
        open? (caldb/time-open? cal dt-cal)
        business? (caldb/day-open? cal dt-cal)]
    {:calendar market-kw
     :business business?
     :open open?
     :zone (:timezone cal)
     :calendar-time (t/date-time dt-cal)}))

(defn gather-calendar [[calendar-kw interval-kw] dt]
  (assoc (market-info calendar-kw dt)
         :calendar [calendar-kw interval-kw]
         ;:prior (t/instant (cal/prior-close calendar-kw interval-kw dt))
         :current (last-finished-close-instant [calendar-kw interval-kw] dt)
         :next (next-upcoming-close-instant [calendar-kw interval-kw] dt)))

(comment
  (market-info :crypto (t/instant))
  (market-info :eu (t/instant))

  (gather-calendar [:crypto :m] (t/instant))

;
  )
