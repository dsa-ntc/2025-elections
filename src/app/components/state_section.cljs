(ns app.components.state-section
  (:require [re-frame.core :as rf]
            [app.model :as model]
            [app.components.candidate-card :refer [candidate-card]]))

(defn state-header [state race-count expanded?]
  [:div.state-header
   {:on-click #(rf/dispatch [:toggle-state state])}
   [:div
    [:h2 state]
    [:span.has-text-grey
     " (" race-count (if (= race-count 1) " race" " races") ")"]]
   [:div.state-toggle
    [:i {:class (if expanded?
                  "fa-solid fa-chevron-up"
                  "fa-solid fa-chevron-down")}]]])

(defn state-section [state races]
  (let [expanded? @(rf/subscribe [::model/state-expanded? state])
        sorted-races (sort-by :candidate-name races)]
    [:div.state-section
     [state-header state (count races) expanded?]
     (when expanded?
       [:div.state-content
        (for [race sorted-races]
          ^{:key (:id race)}
          [candidate-card race])])]))
