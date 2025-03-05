(ns dev.calendar
  (:require
   [ta.calendar.core :as cal]
   [tick.core :as t]))

(cal/current-close :forex :m)
;; => #time/zoned-date-time "2024-10-01T19:27-04:00[America/New_York]"

(cal/current-close :forex :d)
;; => #time/zoned-date-time "2024-10-01T16:30-04:00[America/New_York]"

(->> (cal/current-close :forex :m)
     (cal/next-close :forex :m))
;; => #time/zoned-date-time "2024-10-02T17:01-04:00[America/New_York]"

(->> (cal/calendar-seq :forex :m)
     (take 2))
;; => (#time/zoned-date-time "2024-10-01T19:51-04:00[America/New_York]"
;;     #time/zoned-date-time "2024-10-02T17:01-04:00[America/New_York]")

;; CRYPTO

(cal/current-close :crypto :m)
;; => #time/zoned-date-time "2024-10-01T23:49Z[UTC]"

(->> (cal/current-close :crypto :m)
     (cal/next-close :crypto :m))
;; => #time/zoned-date-time "2024-10-01T23:50Z[UTC]"

(->> (cal/calendar-seq :crypto :m)
     (take 2))
;; => (#time/zoned-date-time "2024-10-01T23:51Z[UTC]" 
;;     #time/zoned-date-time "2024-10-01T23:52Z[UTC]")

;; 
(cal/current-close :forex :d)
;; => #time/zoned-date-time "2024-10-01T16:30-04:00[America/New_York]"

(t/instant)
;; => #time/instant "2024-10-02T15:58:55.337424726Z"

(t/zoned-date-time)
;; => #time/zoned-date-time "2024-10-02T10:59:06.569410757-05:00[America/Panama]"

(cal/current-close :crypto :d)
;; => #time/zoned-date-time "2024-10-01T23:59:59Z[UTC]"

(cal/current-close :crypto :d (t/zoned-date-time))
;; => #time/zoned-date-time "2024-10-01T23:59:59Z[UTC]"

(cal/current-close :crypto :d (t/instant))
;; => #time/zoned-date-time "2024-10-01T23:59:59Z[UTC]"


