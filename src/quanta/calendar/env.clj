(ns quanta.calendar.env
  (:require
   [missionary.core :as m]
   [ta.calendar.core :refer [current-close]]
   [quanta.calendar.scheduler :as live]
   [quanta.calendar.combined :refer [combined-event-seq]]))

(defn get-historic-calendar-flow [cal dt]
  (let [[market-kw interval-kw] cal
        current-close (current-close market-kw interval-kw dt)]
    (m/seed [current-close])))

; firing old events that need to be syncronized with
; all calendars needs a little refactoring,
; before adding the model, so when it is in cell-spec stage
; we need to calculate the calendars, then we
; can can seed the combined event seq, 
; and from that filter the individual calendar events

(defn fire-backtest-events [calendars window]
  (combined-event-seq calendars window))

(defn create-live-calendar [opts]
  (let [live-delay-ms (or (:live-delay-ms opts) 0)]
    (if
     (= 0 live-delay-ms)
      live/get-calendar-flow
      (fn [cal]
        (m/stream (m/ap (let [live-f (live/get-calendar-flow cal)
                              dt (m/?> live-f)]
                          (m/? (m/sleep live-delay-ms))
                          dt)))))))

(defn get-calendar
  "returns a calendar-flow for the calendar.
   reads :dt from dag opts. if dt is set, then
   the flow will be a historic calculation, otherwise
   it will be the live flow. 
   live-flow needs to set dag env :dt-live, for testing
   you could use quanta.calendar.scheduler/get-calendar-flow"
  [env {:keys [calendar]}]
  (let [opts @(get-in env [:dag :opts])
        dt (:dt opts)]
    (if dt
      (get-historic-calendar-flow calendar dt)
      (let [get-live-flow (or (:dt-live opts)
                              (create-live-calendar opts))]
        (get-live-flow calendar)))))
