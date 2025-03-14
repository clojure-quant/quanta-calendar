(ns quanta.calendar.interval.session
  (:require
   [tick.core :as t]
   [quanta.calendar.interval :as i]
   [quanta.calendar.interval.business :as b]))

(defrecord session [timezone open close bday]
  i/interval
  (current [this]
    (let [day (i/current bday)
          dt-open (if (t/>= open close)
                    ; special case for 
                    ; - 24h markets (crypto) 
                    ; - and close-before-open (forex):
                    ; =>> open is one day prior to open.
                    (-> day
                        (b/prior-day)
                        (t/at open)
                        (t/in timezone)
                        (t/instant))
                    (-> day
                        (t/at open)
                        (t/in timezone)
                        (t/instant)))
          dt-close (-> day
                       (t/at close)
                       (t/in timezone)
                       (t/instant))]
      {:open dt-open :close dt-close}))
  (move-next [this]
    (assoc this :bday (i/move-next bday)))
  (move-prior [this]
    (assoc this :bday (i/move-prior bday))))

(defn next-upcoming-close-session [{:keys [timezone open close week] :as market}
                                   dt]
  (let [session-dt (t/in dt timezone)
        bd (b/next-upcoming-close-business-day week session-dt)
        s (session. timezone open close bd)
        session-close-dt (-> s i/current :close)]
    ;(println "session-close: " session-close-dt)
    ;(println "session-time: " session-dt)
    (if (t/<= session-close-dt session-dt)
      (i/move-next s)
      s)))
