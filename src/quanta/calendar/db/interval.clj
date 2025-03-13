(ns quanta.calendar.db.interval
  (:require
   [quanta.calendar.db.calendars :as caldb]
   [quanta.calendar.interval.session :as s]
   [quanta.calendar.interval.day-fraction :as df]
   [quanta.calendar.interval.intraday :as id]))

(def intraday-intervals
  (->> df/interval-dict keys (into #{})))

(defn current-or-next [[market-kw interval-kw] dt]
  (if-let [market (caldb/get-calendar market-kw)]
    (cond
      (= :d interval-kw)
      (s/current-or-next-session market dt)

      (contains? intraday-intervals interval-kw)
      (id/current-or-next-intraday market interval-kw dt)

      :else
      (throw (ex-info (str "unknown interval: " interval-kw) {:market market
                                                              :interval interval-kw
                                                              :dt dt})))
    (throw (ex-info (str "unknown market: " market-kw) {:market market-kw
                                                        :interval interval-kw
                                                        :dt dt}))))

(comment

  intraday-intervals

  ;
  )