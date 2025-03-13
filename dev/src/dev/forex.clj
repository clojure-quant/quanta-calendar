(ns dev.forex
  (:require
   [tick.core :as t]
   [tablecloth.api :as tc]
   [ta.calendar.calendars :refer [calendars]]
   [ta.calendar.interval :refer [intervals]]
   [ta.calendar.helper :refer [trading-open-time]]
   [quanta.calendar.core :refer [next-close current-close]]
   [quanta.calendar.compress :refer [compress-to-calendar add-date-group-calendar]]
   ))


(def ds-min 
(tc/dataset {:date [(t/instant "2025-03-06T09:05:00Z")
                    (t/instant "2025-03-07T09:05:00Z")
                    (t/instant "2025-03-08T09:05:00Z")
                    (t/instant "2025-03-09T09:05:00Z")
                    (t/instant "2025-03-10T09:05:00Z")
                    (t/instant "2025-03-11T09:05:00Z")
                    (t/instant "2025-03-12T09:05:00Z")
                    (t/instant "2025-03-13T09:05:00Z")
                    (t/instant "2025-03-14T09:05:00Z")
                    (t/instant "2025-03-15T09:05:00Z")
                    (t/instant "2025-03-16T09:05:00Z")
                    (t/instant "2025-03-17T09:05:00Z")
                    (t/instant "2025-03-18T09:05:00Z")
                    (t/instant "2025-03-19T09:05:00Z")
                    (t/instant "2025-03-20T09:05:00Z")
                    ]
             :open [1 2 3 4 5 6 7 8 9 10 11 12 13 14 15]
             :high [1 2 3 4 5 6 7 8 9 10 11 12 13 14 15]
             :low [1 2 3 4 5 6 7 8 9 10 11 12 13 14 15]
             :close [1 2 3 4 5 6 7 8 9 10 11 12 13 14 15]
             :volume [1 1 1 1 1 1 1 1 1 1 1 1 1 1 1]
             }))

ds-min

(t/zoned-date-time)
(t/in (t/instant "2024-01-02T00:00:00Z") "UTC")
(t/in (t/instant) "America/New_York")


(tc/map-columns ds-min :date #(t/in % "America/New_York"))

(current-close [:crypto :h] (t/instant "2025-03-06T09:05:00Z"))

(def dt 
  (current-close [:crypto :h] (t/instant "2025-03-06T09:05:00Z"))
  )
dt
(def dt2 (next-close [:crypto :h]  dt))

(def dt3 (next-close [:crypto :h]  dt2))
dt3


(add-date-group-calendar ds-min [:forex :h])
(add-date-group-calendar ds-min [:forex :m])

(add-date-group-calendar ds-min [:crypto :h])

(compress-to-calendar ds-min [:forex :h])



(current-close [:forex :m]
               (t/in (t/date-time "2025-03-12T09:00:00") "UTC"))
#time/zoned-date-time "2025-03-12T05:00-04:00[America/New_York]"

;=> #time/zoned-date-time"2024-02-08T23:59Z[UTC]"

(current-close [:crypto :m]
               (t/in (t/date-time "2024-02-08T23:59:59") "UTC"))
;=> #time/zoned-date-time"2024-02-08T23:59Z[UTC]"

(current-close [:crypto :m]
               (t/in (t/date-time "2024-02-09T00:00:00") "UTC"))