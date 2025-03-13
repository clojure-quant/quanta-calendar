(ns quanta.calendar.impl.intraday
  (:require
   [tick.core :as t]
   [quanta.calendar.impl.business :as b]
   [quanta.calendar.impl.session :as s]
   [quanta.calendar.impl.day-fraction :as df]))

(defrecord intraday-session [session intraday]
  b/interval
  (current [this]
    (cond 
      (t/< (:open (b/current intraday)) (:open (b/current session)))
      {:open (t/instant (:open (b/current session)))
       :close (:close (b/current intraday))
       :short :open}
      (t/> (:close (b/current intraday)) (:close (b/current session)))
      {:open (:open (b/current intraday))
       :close (t/instant (:close (b/current session)))
       :short :close}
      :else 
      (b/current intraday)))
  (move-next [this]
    (loop [n 0
           s session
           i (b/move-next intraday)]
               ;(println "session: " (b/current s) " intraday: " (b/current i))
      (cond
        ; intraday is prior to session -> next intraday
        (t/< (:close (b/current i)) (:open (b/current s)))
        (do ;(println "intraday not yet in current session")
            (if (< n 100)
              (recur (inc n) s (b/move-next i))
              this))
        
        ; intraday is after session -> next session
        (t/> (:open (b/current i)) (:close (b/current s)))
        (do ;(println "switch to next session")
          (if (< n 100)
            (recur (inc n) (b/move-next s) i)
            this))

        ; otherwise we have an overlap 
        :else
        (assoc this :session s :intraday i))))
  (move-prior [this]
    ;(assoc this :dt (t/<< dt duration))
    ;(assoc this :bday (b/move-prior bday))
    this)
  (next-seq [this]
    (->> (iterate b/move-next this)
         (map b/current)))
  (prior-seq [this]
    (->> (iterate b/move-prior this)
         (map b/current))))

(defn current-or-next-intraday [market interval dt]
  (let [session (s/current-or-next-session market dt)
        intraday (df/current-or-next-intraday-block interval dt)
        is (intraday-session. session intraday)]
    is))

(comment

  (require '[ta.calendar.calendars :refer [get-calendar]])

  (def forex (get-calendar :forex))
  forex

  (def s (current-or-next-intraday forex :h (t/instant)))

  (b/current s)

  (take 2 (b/next-seq s))
  (take 100 (b/next-seq s))

  (def m30 (current-or-next-intraday forex :m30 (t/instant)))
  
  (-> (take 500 (b/next-seq m30))
      println
   )
  


  (def h4 (current-or-next-intraday forex :h4 (t/instant)))
  (take 100 (b/next-seq h4))
;
  )
 