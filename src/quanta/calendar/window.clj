(ns quanta.calendar.window
  (:require
   [tick.core :as t]
   [quanta.calendar.interval :as i]
   [quanta.calendar.db.interval :refer [next-upcoming-close]]))

(defn- next-seq [this]
  (iterate i/move-next this))

(defn- prior-seq [this]
  (iterate i/move-prior this))

;; CREATE A WINDOW

(defn date->window
  "creates a window with the last finished close for a date"
  [calendar dt]
  (let [last-interval (next-upcoming-close calendar dt)
        last-interval (if (t/> (:close (i/current last-interval) dt))
                        (i/move-prior last-interval)
                        last-interval)]
    {:calendar calendar
     :window [last-interval]}))

(defn- window-trailing [{:keys [calendar window]} trailing-n]
  (let [first-interval (last window)]
    {:calendar calendar
     :window (take trailing-n (prior-seq first-interval))}))

(defn create-trailing-window
  "creates a trailing window for calendar/dt for trailing-n bars."
  [calendar trailing-n dt]
  (let [w1 (date->window calendar dt)]
    (window-trailing w1 trailing-n)))

(defn date-range->window
  "returns a window for a date-range (start-end)"
  [calendar {:keys [start end] :as _date-range}]
  (let [end-interval (next-upcoming-close calendar end)
        end-interval (i/move-prior end-interval)
        interval-seq (prior-seq end-interval)
        after-start? (fn [current-interval]
                       (t/>= (:close (i/current current-interval)) start))]
    {:calendar calendar
     :window (take-while after-start? interval-seq)}))

(defn create-event-window
  "crates a window prior-n before dt and post-n after date"
  [calendar prior-n post-n dt]
  (let [w1 (date->window calendar dt)
        first-interval (last (:window w1))]
    {:calendar calendar
     :window (concat
              (reverse (take post-n (next-seq (i/move-next first-interval))))
              (take prior-n (prior-seq first-interval)))}))

;; MODIFY 

(defn window-extend-left
  "extends a window to the left by n more bars"
  [{:keys [calendar window] :as w} n]
  {:calendar calendar
   :window (concat window (rest (:window (window-trailing w (inc n)))))})

;; EXTRACTION

(defn window->close-range
  "extracts start (close-dt) and end (close dt) from a window 
   (useful for database queries)"
  [w]
  (let [r (:window w)]
    {:calendar (:calendar w)
     :start (-> r last i/current :close)
     :end (-> r first i/current :close)
     :count (-> r count)}))

(defn window->open-range
  "extracts start (open-dt) and end (open dt) from a window 
   (useful for bar import providers)"
  [w]
  (let [r (:window w)]
    {:calendar (:calendar w)
     :start (-> r last i/current :open)
     :end (-> r first i/current :open)
     :count (-> r count)}))

(defn window->intervals
  "extracts for all bars :open and :close dates"
  [{:keys [calendar window]}]
  {:calendar calendar
   :window (map i/current window)})

(comment

  (def w1 (date->window [:forex :h] (t/instant)))
  w1
  (window->close-range w1)
  (window->open-range w1)
  (window->intervals w1)

  (-> (window-trailing  w1 3)
      window->close-range)

  (-> (create-trailing-window [:forex :h] 3 (t/instant))
      window->close-range)

  (-> (create-trailing-window [:forex :h] 3 (t/instant))
      (window-extend-left 2)
      window->open-range)

  (-> (create-event-window [:forex :h] 3 10 (t/instant))
      window->open-range)

  (-> (date-range->window [:forex :h] {:start  (t/instant "2024-07-04T14:59:00Z")
                                       :end  (t/instant "2024-07-05T14:59:00Z")})
      window->close-range)

; 
  )




