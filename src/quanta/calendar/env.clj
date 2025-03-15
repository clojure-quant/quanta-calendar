(ns quanta.calendar.env
  (:require
   [missionary.core :as m]
   [quanta.calendar.interval :as i]
   [quanta.calendar.db.interval :refer [last-finished-close]]
   [quanta.calendar.env.scheduler :refer [get-calendar-flow]]
   ;[quanta.calendar.env.combined :refer [combined-event-seq]]
   )
  (:import [missionary Cancelled]))

(defn create-calendar-env
  ([]
   {:calendar {:dt (atom nil)
               :delay-ms 0}})
  ([delay-ms]
   {:calendar {:dt (atom nil)
               :delay-ms delay-ms}})
  ([delay-ms initial-dt]
   {:calendar {:dt (atom initial-dt)
               :delay-ms delay-ms}}))

(defn get-calendar
  "returns a calendar-flow for the calendar.
    depending on the env (and set-dt) the calendar is
    either live or historic."
  [env {:keys [calendar]}]
  (assert (:calendar env) "get-calendar env does not contain :calendar")
  (let [{:keys [delay-ms dt]} (:calendar env)
        calendar-f (get-calendar-flow calendar delay-ms)]
    (println "calendar-env: " env)
    (m/ap
     (try (let [dt-manual (m/?< (m/watch dt))]
            (println "dt-manual: " dt-manual)
            (if dt-manual
              (last-finished-close calendar dt-manual)
              (m/?> calendar-f)))
          (catch Cancelled _ (m/amb))))))

(defn get-calendar-close-date
  "returns a flow of instant for the calendar.
   depending on the env (and set-dt) the calendar is
   either live or historic."
  [env calendar]
  (->> (get-calendar env calendar)
       (m/eduction
        (map i/current)
        (map :close))
       (m/stream)))

(defn set-dt
  "env needs to have :calendar (created via create-calendar-env)
   if manual-dt is nil, then get-calendar will return live times,
   if it is set to an instant, then it is historic times."
  [env manual-dt]
  (let [{:keys [dt]} (:calendar env)]
    (if manual-dt
      (println "setting manual-calendar-time to: " manual-dt)
      (println "setting calendar to live-mode"))
    (reset! dt manual-dt)))

#_(defn fire-backtest-events [calendars window]
; firing old events that need to be syncronized with
; all calendars needs a little refactoring,
; before adding the model, so when it is in cell-spec stage
; we need to calculate the calendars, then we
; can can seed the combined event seq, 
; and from that filter the individual calendar events  
    (combined-event-seq calendars window))
