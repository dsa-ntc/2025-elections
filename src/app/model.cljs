(ns app.model
  (:require [re-frame.core :as rf]))

;; Initial database state
(def default-db
  {:races []
   :loading? true
   :error nil
   :last-updated nil
   :expanded-states #{}})

;; Subscriptions
(rf/reg-sub
  ::races
  (fn [db _]
    (sort-by :display-order (:races db))))

(rf/reg-sub
  ::loading?
  (fn [db _]
    (:loading? db)))

(rf/reg-sub
  ::error
  (fn [db _]
    (:error db)))

(rf/reg-sub
  ::last-updated
  (fn [db _]
    (:last-updated db)))

(rf/reg-sub
  ::expanded-states
  (fn [db _]
    (:expanded-states db)))

;; Derived subscriptions
(rf/reg-sub
  ::races-by-state
  :<- [::races]
  (fn [races _]
    (group-by :state races)))

(rf/reg-sub
  ::stats
  :<- [::races]
  (fn [races _]
    (let [total (count races)
          wins (count (filter #(= (:status %) "Win") races))
          losses (count (filter #(= (:status %) "Loss") races))
          running (count (filter #(= (:status %) "Running") races))]
      {:total total
       :wins wins
       :losses losses
       :running running})))

(rf/reg-sub
  ::state-expanded?
  :<- [::expanded-states]
  (fn [expanded-states [_ state]]
    (contains? expanded-states state)))
