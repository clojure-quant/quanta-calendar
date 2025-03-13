(ns quanta.calendar.interval)

(defprotocol interval
  (current [this])
  (move-next [this])
  (move-prior [this]))

(defn next-seq [this]
  (->> (iterate move-next this)
       (map current)))

(defn prior-seq [this]
  (->> (iterate move-prior this)
       (map current)))

(defn trailing-window
  "returns a calendar-seq for a calendar of n rows
   if end-dt specified then last date equals end-date,
   otherwise end-dt is equal to the most-recent close of the calendar"
  [n last-interval]
   (take n (prior-seq last-interval)))

(defn trailing-summary
  "returns a calendar-range for a calendar of n rows
   if end-dt specified then last date equals end-date,
   otherwise end-dt is equal to the most-recent close of the calendar"
  ([trailing-window-seq]
   {:end (current (first trailing-window-seq))
    :start (current (last trailing-window-seq))}))
  

;(defn fixed-window
  ;[[calendar-kw interval-kw] {:keys [start end] :as window}]
  ;(let [seq (calendar-seq-prior [calendar-kw interval-kw] end)
        ;after-start? (fn [dt] (t/>= dt start))]
    ;(take-while after-start? seq)))
