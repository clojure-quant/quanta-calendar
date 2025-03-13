(ns quanta.calendar.interval.intraday
  (:require
   [tick.core :as t]
   [quanta.calendar.interval :as i]
   [quanta.calendar.interval.session :as s]
   [quanta.calendar.interval.day-fraction :as df]))

(defrecord intraday-session [session intraday]
  i/interval
  (current [this]
    (cond
      (t/< (:open (i/current intraday)) (:open (i/current session)))
      {:open (t/instant (:open (i/current session)))
       :close (:close (i/current intraday))
       :short :open}
      (t/> (:close (i/current intraday)) (:close (i/current session)))
      {:open (:open (i/current intraday))
       :close (t/instant (:close (i/current session)))
       :short :close}
      :else
      (i/current intraday)))
  (move-next [this]
    (loop [n 0
           s session
           i (i/move-next intraday)]
               ;(println "session: " (i/current s) " intraday: " (i/current i))
      (cond
        ; intraday is prior to session -> next intraday
        (t/< (:close (i/current i)) (:open (i/current s)))
        (do ;(println "intraday not yet in current session")
          (if (< n 100)
            (recur (inc n) s (i/move-next i))
            this))

        ; intraday is after session -> next session
        (t/> (:open (i/current i)) (:close (i/current s)))
        (do ;(println "switch to next session")
          (if (< n 100)
            (recur (inc n) (i/move-next s) i)
            this))

        ; otherwise we have an overlap 
        :else
        (assoc this :session s :intraday i))))
  (move-prior [this]
    ;(assoc this :dt (t/<< dt duration))
    ;(assoc this :bday (i/move-prior bday))
    this))

(defn next-upcoming-close-intraday [market interval dt]
  (let [session (s/next-upcoming-close-session market dt)
        intraday (df/next-upcoming-close-dayfraction interval dt)
        is (intraday-session. session intraday)
        id-close-dt (-> is i/current :close)]
    (if (t/<= id-close-dt dt)
      (i/move-next is)
      is)))

