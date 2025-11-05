(ns app.components.candidate-card
  (:require [clojure.string]))

(defn format-poll-close-time [iso-datetime]
  "Formats ISO datetime string to a human-readable format with timezone"
  (when iso-datetime
    (let [date (js/Date. iso-datetime)
          options #js {:hour "numeric"
                       :minute "2-digit"
                       :timeZoneName "short"}]
      (.toLocaleString date "en-US" options))))

(defn vote-display [{:keys [vote-percentage results-override status ballots-counted poll-close-time]}]
  (if vote-percentage
    [:div.vote-results
     [:div.vote-percentage
      (if results-override (str results-override)
          (if vote-percentage
            (str (.toFixed (* 100 vote-percentage) 2) "%")
            "—"))]
     (when (or ballots-counted poll-close-time)
       [:div.ballots-counted
        (str
         (when poll-close-time
           (str "Polls close: " (format-poll-close-time poll-close-time)))
         (when (and ballots-counted poll-close-time) " • ")
         (when ballots-counted
           (str (.toLocaleString (* 100 (js/Number ballots-counted))) "% reporting")))])]
    [:div.vote-results.no-results
     [:div.status-label status]]))

(defn get-webp-url [png-url]
  "Convert PNG URL to WebP URL"
  (when png-url
    (-> png-url
        clojure.string/trim
        (clojure.string/replace #"\.png$" ".webp"))))

(defn candidate-card [race]
  (let [status (:status race)
        photo-url-raw (:candidate-photo-url race)
        photo-url (when photo-url-raw (clojure.string/trim photo-url-raw))
        webp-url (get-webp-url photo-url)
        alt-text (or (:candidate-photo-alt race)
                     (str (:candidate-name race) " photo"))
        card-class (cond
                     (= status "Win") "candidate-card winner"
                     (= status "Loss") "candidate-card loss"
                     (= status "Run Off") "candidate-card runoff"
                     :else "candidate-card")]
    [:div {:class card-class}
     ;; State badge (top right)
     [:div.state-badge (:state race)]

     ;; Candidate photo
     (if photo-url
       [:div.candidate-photo
        [:picture
         [:source {:srcset photo-url
                   :type "image/png"}]
         [:img {:src webp-url
                :alt alt-text}]]]
       [:div.candidate-photo.no-photo
        [:i.fa-solid.fa-user]])

     ;; Candidate info
     [:div.candidate-info
      [:div.name-and-chapter
       [:span.candidate-name (:candidate-name race)]]
      (if-let [chapter-url (:chapter-url race)]
        [:a.candidate-chapter {:href chapter-url
                               :target "_blank"
                               :rel "noopener noreferrer"}
         (:chapter race)]
        [:div.candidate-chapter (:chapter race)])
      [:div.candidate-office (:office race)]]

     ;; Vote results (always show)
     [vote-display race]]))
