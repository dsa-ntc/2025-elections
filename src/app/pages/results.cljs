(ns app.pages.results
  (:require [re-frame.core :as rf]
            [app.model :as model]
            [app.components.candidate-card :refer [candidate-card]]))

(defn format-time [date]
  (when date
    (let [hours (.getHours date)
          minutes (.getMinutes date)
          ampm (if (>= hours 12) "PM" "AM")
          display-hours (if (> hours 12) (- hours 12) (if (= hours 0) 12 hours))
          display-minutes (if (< minutes 10) (str "0" minutes) (str minutes))]
      (str display-hours ":" display-minutes " " ampm))))

(defn summary-stat [number label]
  [:div.stat-box
   [:span.stat-number number]
   [:span.stat-label label]])

(defn election-header []
  (let [stats @(rf/subscribe [::model/stats])
        last-updated @(rf/subscribe [::model/last-updated])]
    [:div.election-header
     [:a.header-logo-link {:href "https://electoral.dsausa.org/"
                           :target "_blank"
                           :rel "noopener noreferrer"}
      [:img {:src "./img/nec-logo_white-transparent.png"
             :alt "National Electoral Commission"}]]
     [:h1 "DSA Election Night 2025"]
     [:p.subtitle "Real-time results for DSA’s nationally-endorsed candidates and high profile locally-endorsed candidates."]
     [:div.header-buttons
      [:a.join-dsa-link {:href "https://act.dsausa.org/donate/membership/"
                         :target "_blank"
                         :rel "noopener noreferrer"}
       "build a better world with us habibi"]
      [:a.donate-link {:href "https://electoral.dsausa.org/socialist-cash-takes-out-capitalist-trash/"
                       :target "_blank"
                       :rel "noopener noreferrer"}
       "💸💸💸"]]
     [:div.summary-stats
      [summary-stat (:total stats) "Total Races"]
      [summary-stat (:wins stats) "Wins"]
      [summary-stat (:running stats) "Running"]
      [summary-stat (:losses stats) "Losses"]]

     (when last-updated
       [:div.last-updated
        [:i.fa-solid.fa-clock]
        " Last updated: " (format-time last-updated)])]))

(defn loading-view []
  [:div.container.has-text-centered {:style {:padding "3rem 0"}}
   [:div.lds-ring
    [:div]
    [:div]
    [:div]
    [:div]]
   [:p.mt-3 "Loading election results..."]])

(defn error-view [error]
  [:div.container {:style {:padding "2rem 0"}}
   [:div.notification.is-warning
    [:i.fa-solid.fa-triangle-exclamation]
    " " error]])

(defn results-content []
  (let [races @(rf/subscribe [::model/races])]
    [:div.results-container
     [:div.candidates-grid
      (for [race races]
        ^{:key (:id race)}
        [candidate-card race])]]))

(defn election-footer []
  [:div.election-footer
   [:a.footer-logo-link {:href "https://electoral.dsausa.org/"
                         :target "_blank"
                         :rel "noopener noreferrer"}
    [:img.footer-logo {:src "./img/nec-logo_white-transparent.png"
                       :alt "National Electoral Commission"}]]
   [:p "Data updates every 30 seconds"]
   [:p
    [:i.fa-solid.fa-rose]]])

(defn results-page []
  (let [loading? @(rf/subscribe [::model/loading?])
        error @(rf/subscribe [::model/error])]
    [:div
     [election-header]

     (cond
       loading? [loading-view]
       error [error-view error]
       :else [results-content])

     [election-footer]]))
