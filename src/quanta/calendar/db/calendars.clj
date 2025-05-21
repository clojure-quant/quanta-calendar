(ns quanta.calendar.db.calendars
  (:require
   [tick.core :as t]))

(def week-5
  (sorted-set
   t/MONDAY t/TUESDAY t/WEDNESDAY
   t/THURSDAY t/FRIDAY))

(def week-6
  (sorted-set
   t/MONDAY t/TUESDAY t/WEDNESDAY
   t/THURSDAY t/FRIDAY t/SATURDAY))

(def week-6-sunday
  (sorted-set
   t/SUNDAY t/MONDAY t/TUESDAY
   t/WEDNESDAY t/THURSDAY t/FRIDAY))

(def week-7
  (sorted-set
   t/MONDAY t/TUESDAY t/WEDNESDAY
   t/THURSDAY t/FRIDAY t/SATURDAY t/SUNDAY))

(def calendars
  {:forex {:open (t/new-time 17 0 0)
           :close (t/new-time 17 0 0)
           :week week-5 ; old: week-6-sunday
           :timezone "America/New_York"}
   :forex-no-asia {:open (t/new-time 3 0 0)
                   :close (t/new-time 16 0 0)
                   :week week-5
                   :timezone "America/New_York"}
   :crypto {:open (t/new-time 0 0 0)
            :close (t/new-time 0 0 0)
            :week week-7
            :timezone "UTC"}
   :us {:open (t/new-time 9 0 0)
        :close (t/new-time 17 0 0)
        :week week-5
        :timezone "America/New_York"}
   :us24 {:open (t/new-time 0 0 0)
          :close (t/new-time 0 0 0)
          :week week-5
          :timezone "America/New_York"}
   :eu {:open (t/new-time 9 0 0)
        :close (t/new-time 17 0 0)
        :week week-5
        :timezone "Europe/Berlin"}

   :jp {:open (t/new-time 9 0 0)
        :close (t/new-time 17 0 0)
        :week week-5
        :timezone "Asia/Tokyo"}

   :test-short {:open (t/new-time 9 0 0)
                :close (t/new-time 11 0 0)
                :week week-5
                :timezone "Asia/Tokyo"}

   :test-equal {:open (t/new-time 0 0 0)
                :close (t/new-time 0 0 0)
                :week week-5
                :timezone "Asia/Tokyo"}})

(defn calendar-exists? [calendar-kw]
  (contains? calendars calendar-kw))

(defn get-calendar [calendar-kw]
  (get calendars calendar-kw))

(defn get-calendar-list []
  (keys calendars))

(defn get-calendar-timezone [calendar-kw]
  (-> calendar-kw
      get-calendar
      :timezone))

(defn day-open? [{:keys [week] :as _calendar} dt]
  (let [day (t/day-of-week dt)]
    (contains? week day)))

(defn day-closed? [calendar dt]
  (not (day-open? calendar dt)))

(defn midnight-close? [close]
  (t/= close (t/new-time 0 0 0)))

(defn intraday? [{:keys [open close] :as _calendar}]
  (or (t/< open close)
      (and (t/= open close)
           (midnight-close? close))))

(defn overnight? [{:keys [open close] :as _calendar}]
  (and (t/>= open close)
       (not (midnight-close? close))))

(defn time-open?
  "expecting a zoned dt in the same timezone as the calendar timezone"
  [{:keys [open close] :as calendar} dt]
  (let [time (t/time dt)]
    (cond
      (day-closed? calendar dt) false
      (intraday? calendar) (and (t/>= time open) (or (t/<= time close)
                                                     (midnight-close? close)))
      (overnight? calendar) (let [day-before (t/<< dt (t/new-duration 1 :days))
                                  day-after (t/>> dt (t/new-duration 1 :days))]
                              (or (and (t/<= time close) (day-open? calendar day-before))
                                  (and (t/>= time open) (day-open? calendar day-after)))))))

(comment
  (contains? week-5 t/MONDAY)
  (contains? week-5 t/SUNDAY)
  (contains? week-7 t/SUNDAY)

  (calendar-exists? :us)
  (calendar-exists? :us555)

  (get-calendar-list)

  (get-calendar :jp)
  (get-calendar-timezone :jp)
  (get-calendar-timezone :forex)
  (get-calendar-timezone :crypto)

;
  )