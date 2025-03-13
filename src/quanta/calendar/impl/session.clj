(ns quanta.calendar.impl.session
  (:require
   [tick.core :as t]
   [quanta.calendar.impl.business :refer [current-or-next-business-day] :as b]
  ))

(defrecord session [timezone open close bday]
  b/interval
  (current [this]
    (let [day (b/current bday)
          dt-open (if (t/>= open close)
                                       ; special case for 
                                       ; - 24h markets (crypto) 
                                       ; - and close-before-open (forex):
                                       ; =>> open is one day prior to open.
                    (-> day 
                        (b/prior-day)
                        (t/at open)
                        (t/in timezone))
                    (-> day
                        (t/at open)
                        (t/in timezone)))
          dt-close (-> day
                       (t/at close)
                       (t/in timezone))]
      {:open dt-open :close dt-close}))
  (move-next [this]
    (assoc this :bday (b/move-next bday)))
  (move-prior [this]
    (assoc this :bday (b/move-prior bday)))
  (next-seq [this]
    (->> (iterate b/move-next this)
         (map b/current)))
  (prior-seq [this]
    (->> (iterate b/move-prior this)
         (map b/current))))


(defn current-or-next-session [{:keys [timezone open close week] :as market}
                               dt]
  (let [dt-session (t/in dt timezone)
        bd (current-or-next-business-day week dt-session)
        s (session. timezone open close bd)
        close-s (-> s b/current :close)]
    (if (t/< dt-session close-s)
      (b/move-next s)
      s)))

(comment 
  (require '[ta.calendar.calendars :refer [get-calendar]]) 

(def forex (get-calendar :forex))
forex

(def s (current-or-next-session forex (t/instant)))

(def s (current-or-next-session (get-calendar :eu) (t/instant)))
s

(b/current s)
  
(-> (b/current s)
    :open 
    type
    )  


(take 2 (b/next-seq s))
(take 10 (b/next-seq s))

  (t/> (-> (take 1 (b/next-seq s))
           first
           :close)
       (t/instant))

  (t/< (-> (take 1 (b/next-seq s))
         first
         :close)
     (t/instant))

;
       )

