(ns demo.window
  (:require
   [tick.core :as t]
   [quanta.calendar.window :as w]))

(->> {; note the summer-time change here
      :start (t/instant "2025-03-01T21:30:00Z")
      :end (t/instant "2025-03-10T20:30:00Z")}
     (w/fixed-window [:forex :d])
     (w/window-summary))

(->>  (t/instant "2025-02-05T21:30:00Z")
      (w/trailing-window [:forex :d] 4)
      (w/window-summary))

(->>  (t/instant "2025-02-05T23:30:00Z") ; 2 hours after close, 
      (w/trailing-window [:forex :d] 4) ; result will be the same as the prior example
      (w/window-summary))