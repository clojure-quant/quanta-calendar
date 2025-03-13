(ns quanta.calendar.interval.session
  (:require
   [tick.core :as t]
   [quanta.calendar.interval :as i]
   [quanta.calendar.interval.business :refer [current-or-next-business-day] :as b]))

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

(defn current-or-next-session [{:keys [timezone open close week] :as market}
                               dt]
  (let [dt-session (t/in dt timezone)
        bd (current-or-next-business-day week dt-session)
        s (session. timezone open close bd)
        close-s (-> s i/current :close)]
    (if (t/< dt-session close-s)
      (i/move-next s)
      s)))
