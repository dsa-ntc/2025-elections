(ns app.components.status-badge)

(defn status-badge [status]
  (let [status-class (case status
                       "Win" "status-win"
                       "Loss" "status-loss"
                       "Running" "status-running"
                       "Too Close to Call" "status-too-close"
                       "status-running")
        icon (case status
               "Win" "fa-solid fa-trophy"
               "Loss" "fa-solid fa-circle-xmark"
               "Running" "fa-solid fa-clock"
               "Too Close to Call" "fa-solid fa-circle-question"
               "fa-solid fa-clock")
        display-text (case status
                       "Running" "In Progress"
                       status)]
    [:div {:class (str "status-badge " status-class)}
     [:i {:class icon}]
     [:span display-text]]))
