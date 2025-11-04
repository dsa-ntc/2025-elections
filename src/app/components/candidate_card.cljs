(ns app.components.candidate-card)

(defn format-poll-close-time [iso-datetime]
  "Formats ISO datetime string to a human-readable format with timezone"
  (when iso-datetime
    (let [date (js/Date. iso-datetime)
          options #js {:hour "numeric"
                       :minute "2-digit"
                       :timeZoneName "short"}]
      (.toLocaleString date "en-US" options))))

(defn vote-display [{:keys [vote-percentage status ballots-counted poll-close-time]}]
  (if vote-percentage
    [:div.vote-results
     [:div.vote-percentage
      (if vote-percentage
        (str (.toFixed (js/Number vote-percentage) 1) "%")
        "—")]
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

(defn candidate-card [race]
  (let [status (:status race)
        photo-url (:candidate-photo-url race)
        card-class (cond
                     (= status "Win") "candidate-card winner"
                     (= status "Loss") "candidate-card loss"
                     :else "candidate-card")]
    [:div {:class card-class}
     ;; State badge (top right)
     [:div.state-badge (:state race)]

     ;; Candidate photo
     (if photo-url
       [:div.candidate-photo
        [:img {:src photo-url
               :alt (str (:candidate-name race) " photo")}]]
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
