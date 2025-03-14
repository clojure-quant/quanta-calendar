(ns demo.info
  (:require
   [tick.core :as t]
   [quanta.calendar.calendar-info :refer [market-info gather-calendar]]))

(def dt (t/instant))

(market-info :crypto dt)

;; => {:market :crypto, 
;;     :open? true, 
;;     :business true, :
;;      as-of-dt #time/zoned-date-time "2024-10-11T21:10:02.760020057Z[UTC]"}

(gather-calendar [:crypto :h] dt)
;{:calendar [:crypto :h],
; :business true,
; :open true,
; :zone "UTC",
; :calendar-time #time/date-time "2025-03-14T17:17:18.293494791",
; :current #time/instant "2025-03-14T17:00:00Z",
; :next #time/instant "2025-03-14T18:00:00Z"}