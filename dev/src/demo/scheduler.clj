(ns demo.scheduler
  (:require
   [missionary.core :as m]
   [quanta.calendar.interval :as i]
   [quanta.calendar.env.scheduler :refer [get-calendar-flow get-calendar-flow-close-date]]))

(def cal-f (get-calendar-flow [:crypto :m]))

; returns immediately with the last closed bar date

(m/? (m/reduce conj
               (m/eduction (take 1)
                           (map i/current)
                           cal-f)))

; waits until it gets the next upcoming bar date,
; and returns the prior and the next.
; it can take up to a minute to run, because it will only
; fire events as the time passes.

(m/? (m/reduce conj (m/eduction (take 2)
                                (map i/current)
                                cal-f)))

;; delayed calendar test

(def cal-d-f (get-calendar-flow [:crypto :m] 5000))

(m/? (m/reduce conj
               (m/eduction (take 1)
                           (map i/current)
                           cal-d-f)))


; date test

(def cal-date-f (get-calendar-flow-close-date [:crypto :m]))

(m/? (m/reduce conj
               (m/eduction (take 1)
                           cal-date-f)))

(def cal-date-delayed-f (get-calendar-flow-close-date [:crypto :m] 2000))

(m/? (m/reduce conj
               (m/eduction (take 1)
                           cal-date-delayed-f)))