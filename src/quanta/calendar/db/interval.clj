(ns quanta.calendar.db.interval
  (:require
   [quanta.calendar.db.calendars :as caldb]
   [quanta.calendar.interval :as i]
   [quanta.calendar.interval.session :as s]
   [quanta.calendar.interval.day-fraction :as df]
   [quanta.calendar.interval.intraday :as id]))

(def intraday-intervals
  (->> df/interval-dict keys (into #{})))

(def all-intervals 
  (conj intraday-intervals :d))

(defn next-upcoming-close [[market-kw interval-kw] dt]
  (if-let [market (caldb/get-calendar market-kw)]
    (cond
      (= :d interval-kw)
      (s/next-upcoming-close-session market dt)

      (contains? intraday-intervals interval-kw)
      (id/next-upcoming-close-intraday market interval-kw dt)

      :else
      (throw (ex-info (str "unknown interval: " interval-kw) {:market market
                                                              :interval interval-kw
                                                              :dt dt})))
    (throw (ex-info (str "unknown market: " market-kw) {:market market-kw
                                                        :interval interval-kw
                                                        :dt dt}))))


(defn next-upcoming-close-instant [[market-kw interval-kw] dt]
  (->> dt
      (next-upcoming-close [market-kw interval-kw])
      (i/current) 
      :close))

(defn last-finished-close [[market-kw interval-kw] dt]
  (->> dt
       (next-upcoming-close [market-kw interval-kw])
       (i/move-prior)))

(defn last-finished-close-instant [[market-kw interval-kw] dt]
  (->> dt
       (last-finished-close [market-kw interval-kw])
       (i/current)
       :close))


(comment

  intraday-intervals
  all-intervals

  ;
  )