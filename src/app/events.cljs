(ns app.events
  (:require [re-frame.core :as rf]
            [ajax.core :as ajax]
            [app.model :refer [default-db]]))

;; Initialize database
(rf/reg-event-db
  :initialize-db
  (fn [_ _]
    default-db))

;; Toggle state section expanded/collapsed
(rf/reg-event-db
  :toggle-state
  (fn [db [_ state]]
    (update db :expanded-states
            (fn [expanded]
              (if (contains? expanded state)
                (disj expanded state)
                (conj expanded state))))))

;; Set races data
(rf/reg-event-db
  :set-races
  (fn [db [_ races]]
    (assoc db
           :races races
           :loading? false
           :last-updated (js/Date.))))

;; Set error
(rf/reg-event-db
  :set-error
  (fn [db [_ error]]
    (assoc db
           :error error
           :loading? false)))

;; Fetch races (tries Airtable first, falls back to initial data)
(rf/reg-event-fx
  :fetch-races
  (fn [{:keys [db]} _]
    {:airtable/fetch-races
     {:on-success [:process-airtable-response]
      :on-failure [:airtable-request-failed]}
     :db (assoc db :loading? true)}))

;; Clear error message
(rf/reg-event-db
  :clear-error
  (fn [db _]
    (assoc db :error nil)))

;; Process Airtable response
(rf/reg-event-fx
  :process-airtable-response
  (fn [{:keys [db]} [_ races]]
    {:dispatch [:set-races races]}))

;; Handle Airtable request failure - fall back to empty data
(rf/reg-event-fx
  :airtable-request-failed
  (fn [{:keys [db]} [_ error]]
    (js/console.warn "Airtable API call failed, using fallback data:" error)
    {:dispatch [:set-races []]}))

;; Start auto-refresh timer (refreshes every 30 seconds)
(rf/reg-event-fx
  :start-auto-refresh
  (fn [_ _]
    {:dispatch-later [{:ms 30000 :dispatch [:auto-refresh-tick]}]}))

;; Auto-refresh tick - fetch new data and schedule next refresh
(rf/reg-event-fx
  :auto-refresh-tick
  (fn [{:keys [db]} _]
    {:airtable/fetch-races
     {:on-success [:process-airtable-response]
      :on-failure [:airtable-silent-failure]}
     :dispatch-later [{:ms 30000 :dispatch [:auto-refresh-tick]}]
     :db (assoc db :loading? false)}))

;; Silent failure for auto-refresh (don't show error, just keep existing data)
(rf/reg-event-db
  :airtable-silent-failure
  (fn [db [_ error]]
    (js/console.warn "Auto-refresh failed:" error)
    db))
