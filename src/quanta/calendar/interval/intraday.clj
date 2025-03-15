(ns quanta.calendar.interval.intraday
  (:require
   [tick.core :as t]
   [quanta.calendar.interval :as i]
   [quanta.calendar.interval.session :as s]
   [quanta.calendar.interval.day-fraction :as df]))

(defn position [small big]
  (cond
    (t/<= (:close (i/current small)) (:open (i/current big)))
    :outside-open

    (t/>= (:open (i/current small)) (:close (i/current big)))
    :outside-close

    (t/< (:open (i/current small)) (:open (i/current big)))
    :overlap-open

    (t/> (:close (i/current small)) (:close (i/current big)))
    :overlap-close

    :else
    :inside))

(defrecord intraday-session [session intraday]
  i/interval
  (current [this]
    (let [p (position intraday session)]
      (case p
        :overlap-open
        {:open (t/instant (:open (i/current session)))
         :close (:close (i/current intraday))
         :short :open}
        :overlap-close
        {:open (:open (i/current intraday))
         :close (t/instant (:close (i/current session)))
         :short :close}
      ; default
        (i/current intraday))))
  (move-next [this]
    (loop [s session
           i (i/move-next intraday)]
               ;(println "session: " (i/current s) " intraday: " (i/current i))
      (let [p (position i s)]
        (case p
          :outside-open ; intraday is prior to session -> next intraday
          (recur s (i/move-next i))

          :outside-close ; intraday is after session -> next session
          (recur (i/move-next s) i)

          ; otherwise we have an overlap 
          (assoc this :session s :intraday i)))))
  (move-prior [this]
    (loop [s session
           i (i/move-prior intraday)]
      (let [p (position i s)]
        (case p
          :outside-open ; intraday is prior to session  -> prior session
          (recur (i/move-prior s) i)

          :outside-close ; intraday is after session S-> prior intraday
          (recur s (i/move-prior i))

          ; otherwise we have an overlap 
          (assoc this :session s :intraday i))))))

(defn next-upcoming-close-intraday [market interval dt]
  (let [session (s/next-upcoming-close-session market dt)
        intraday (df/next-upcoming-close-dayfraction interval dt)
        is (intraday-session. session intraday)
        id-close-dt (-> is i/current :close)]
    (if (t/<= id-close-dt dt)
      (i/move-next is)
      is)))

