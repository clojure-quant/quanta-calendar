(ns quanta.calendar.interval.day-fraction
  (:require
   [tick.core :as t]
   [quanta.calendar.interval :as i]
   ;[quanta.calendar.util :as u]
   ))

; all divider of 1440 (minutes of day)
;1, 2, 3, 4, 5, 6, 8, 9, 10, 12, 15, 16, 18, 20, 24, 30, 32, 36, 40, 48, 60, 72, 80, 90, 96, 120, 144, 160, 180, 240, 288, 360, 480, 720, 1440

(def interval-dict
  {; hour
   :h   [1 :hours]
   :h2  [2 :hours]
   :h3  [3 :hours]
   :h4  [4 :hours]
   :h6  [6 :hours]
   :h8  [8 :hours]
   :h12 [12 :hours]

   ; minute
   :m   [1 :minutes]
   :m2  [2 :minutes]
   :m3  [3 :minutes]
   :m4  [4 :minutes]
   :m5  [5 :minutes]
   :m6  [6 :minutes]
   :m8  [8 :minutes]
   :m9  [9 :minutes]
   :m10 [10 :minutes]
   :m12 [12 :minutes]
   :m15 [15 :minutes]
   :m16 [16 :minutes]
   :m18 [18 :minutes]
   :m20 [20 :minutes]
   :m24 [24 :minutes]
   :m30 [30 :minutes]
   :m32 [32 :minutes]
   :m36 [36 :minutes]
   :m40 [40 :minutes]
   :m48 [48 :minutes]

   :m72 [72 :minutes]
   :m80 [80 :minutes]
   :m90 [90 :minutes]
   :m96 [96 :minutes]
   :m144 [144 :minutes]
   :m160 [160 :minutes]
   :m288 [288 :minutes]})

(def midnight
  (t/time "00:00:00"))

(defn minutes-since-midnight [t]
  (+ (* (t/hour t) 60)
     (t/minute t)))

(defn hours-since-midnight [t]
  (t/hour t))

(defn next-block-id [u n t]
  (let [i (case u
            :hours (hours-since-midnight t)
            :minutes (minutes-since-midnight t))
        full-blocks (quot i n)
        rem (mod i n)
        next-upcoming-id (if (= rem 0)
                           full-blocks
                           (inc full-blocks))]
    next-upcoming-id))

(defn block-time [u n i]
  (let [total (* n i)
        d (t/new-duration total u)]
    (t/>> midnight d)))

(defrecord intraday-block [duration dt]
  i/interval
  (current [this]
    {:open (t/<< dt duration)
     :close dt})
  (move-next [this]
    (assoc this :dt  (t/>> dt duration)))
  (move-prior [this]
    (assoc this :dt  (t/<< dt duration))))

(defn current-or-next-intraday-block [interval dt]
  (let [[n u] (get interval-dict interval)
        clock-instant (t/instant dt) ; 
        clock-d (t/date clock-instant)
        clock-t (t/time clock-instant)
        i (next-block-id u n clock-t)
        block-t (block-time u n i)
        dt (t/at clock-d block-t)
        dt (t/instant dt)
        duration (t/new-duration n u)]
    (intraday-block. duration dt)))

(comment

  (t/truncate (t/instant) :minutes)
  (def duration (t/new-duration 1.5 :hours))
  (def total-minutes (t/in duration :minutes)) ;; Convert the duration to minutes
  ;(u/extract-field (t/instant) :hours)
  ;(u/extract-field (t/instant) :minutes)

  (block-time :minutes 30 0)
  (block-time :minutes 30 1)
  (block-time :minutes 30 5)
  (block-time :minutes 30 20)
  (block-time :minutes 30 40)
  (block-time :minutes 30 48)

  (block-time :hours 2 1)
  (block-time :hours 2 3)

  (def s (current-or-next-intraday-block :m5 (t/instant)))

  (take 2 (i/next-seq s))
  (take 10 (i/next-seq s))

;
  )



