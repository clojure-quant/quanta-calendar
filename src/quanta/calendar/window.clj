(ns quanta.calendar.window
  (:require
   [tick.core :as t]
   [quanta.calendar.interval :as i]
   [quanta.calendar.db.interval :refer [next-upcoming-close last-finished-close]]))

(defn trailing-window
  "returns a calendar-seq for a calendar of n rows
   if end-dt specified then last date equals end-date,
   otherwise end-dt is equal to the most-recent close of the calendar"
  ([n last-interval]
   (take n (i/prior-seq last-interval)))
  ([calendar n last-dt]
   (let [bar-range (next-upcoming-close calendar last-dt)
         bar-range (if (t/> (:close (i/current bar-range) last-dt))
                     (i/move-prior bar-range)
                     bar-range)]
     (trailing-window n bar-range))))

(defn fixed-window
  [calendar {:keys [start end] :as window}]
  (let [end-interval (next-upcoming-close calendar end)
        end-interval (i/move-prior end-interval)
        interval-seq (i/prior-seq end-interval)
        after-start? (fn [current-interval]
                       (t/>= (:close current-interval) start))]
    (take-while after-start? interval-seq)
      ;(take 2 interval-seq)
    ))

(defn window-summary
  "returns a calendar-range for a calendar of n rows
   if end-dt specified then last date equals end-date,
   otherwise end-dt is equal to the most-recent close of the calendar"
  ([trailing-window-seq]
   {:start  (last trailing-window-seq)
    :end  (first trailing-window-seq)}))