(ns dev.interval
  (:require
   [tick.core :as t]
   [quanta.calendar.db.calendars :as caldb]
   [quanta.calendar.interval :as i]
   [quanta.calendar.interval.business :as bd]
   [quanta.calendar.interval.day-fraction :as df]
   [quanta.calendar.interval.session :as s]
   [quanta.calendar.interval.intraday :as id]))

;; business day
(def day (bd/next-upcoming-close-business-day caldb/week-5 (t/instant)))

day

(i/current day)
(-> day i/move-next i/current)
(-> day i/move-prior i/current)

(take 10 (i/next-seq day))
(take 10 (i/prior-seq day))

(def day6 (bd/next-upcoming-close-business-day caldb/week-6-sunday
                                               (t/instant)))

(take 10 (i/next-seq day6))

 ;; day fraction

(def m30 (df/next-upcoming-close-dayfraction :m30 (t/instant)))
(take 10 (i/next-seq m30))
(take 10 (i/prior-seq m30))

(def h1 (df/next-upcoming-close-dayfraction :h (t/instant)))
(take 10 (i/next-seq h1))

(def h4 (df/next-upcoming-close-dayfraction :h4 (t/instant)))
(take 10 (i/next-seq h4))

(def h12 (df/next-upcoming-close-dayfraction :h12 (t/instant)))
(take 10 (i/next-seq h12))
(take 20 (i/prior-seq h12))

;; session

(def forex (caldb/get-calendar :forex))
forex

(def s-fx (s/next-upcoming-close-session forex (t/instant)))

(def s-eu (s/next-upcoming-close-session (caldb/get-calendar :eu) (t/instant)))
s-fx

(i/current s-fx)

(take 2 (i/next-seq s-fx))
(take 10 (i/next-seq s-fx))
(take 10 (i/prior-seq s-fx))

;; intraday

(def s (id/next-upcoming-close-intraday forex :h (t/instant)))

(i/current s)

(take 2 (i/next-seq s))
(take 100 (i/next-seq s))
(take 100 (i/prior-seq s))

(def m30-id (id/next-upcoming-close-intraday forex :m30 (t/instant)))

(-> (take 500 (i/next-seq m30-id))
    println)

(def h4-id (id/next-upcoming-close-intraday forex :h4 (t/instant)))
(take 100 (i/next-seq h4-id))