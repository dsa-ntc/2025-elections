(ns app.airtable
  (:require [ajax.core :as ajax]
            [re-frame.core :as rf]
            [app.config :as config]
            [clojure.string :as str]))

;; Get configuration from config namespace
(def airtable-config
  {:api-key (config/get-config :api-key)
   :base-id (config/get-config :base-id)
   :table-name (config/get-config :table-name)})


;; Transform Airtable record to our internal race format
(defn transform-record [record]
  (let [fields (:fields record)]
    {:id (:id record)
     :candidate-name (get fields (keyword "Candidate Name"))
     :state (get fields (keyword "State"))
     :chapter (get fields (keyword "Endorsing Chapter"))
     :office (get fields (keyword "Office"))
     :office-type (or (get fields (keyword "Office Type")) "Municipal")
     :status (or (get fields (keyword "Status")) "Running")
     :vote-percentage (get fields (keyword "Vote Percentage"))
     :vote-count (get fields (keyword "Vote Count"))
     :total-votes-cast (get fields (keyword "Total Votes Cast"))
     :ballots-counted (get fields (keyword "Ballots Counted"))
     :poll-close-time (get fields (keyword "poll close time"))
     :results-link (get fields (keyword "Results Link"))
     :website-url (get fields (keyword "Website URL"))
     :chapter-url (get fields (keyword "Chapter URL"))
     :endorsement-url (get fields (keyword "Endorsement Blog Post"))
     :candidate-photo-url (get fields (keyword "Candidate Photo URL"))
     :candidate-photo-alt (get fields (keyword "Alt Text"))
     :display-order (or (get fields (keyword "Display Order")) 999)}))

;; Fetch races from cached data.json (now from R2 or local)
(defn fetch-races-from-cache [on-success on-failure]
  (let [data-url (config/get-r2-url)]
    (ajax/GET data-url
      {:response-format (ajax/json-response-format {:keywords? true})
       :handler (fn [response]
                  (let [records (:records response)
                        races (map transform-record records)]
                    (js/console.log (str "Using cached data from " data-url))
                    (on-success races)))
       :error-handler (fn [error]
                        (js/console.log "Cache miss, falling back to Airtable API")
                        (on-failure error))})))

;; Fetch races from Airtable API (with auth)
(defn fetch-races-from-api [on-success on-failure]
  (let [{:keys [api-key base-id table-name]} airtable-config]
    (if (and api-key base-id)
      (ajax/GET (str "https://api.airtable.com/v0/" base-id "/" table-name)
        {:headers {"Authorization" (str "Bearer " api-key)}
         :response-format (ajax/json-response-format {:keywords? true})
         :handler (fn [response]
                    (let [records (:records response)
                          races (map transform-record records)]
                      (on-success races)))
         :error-handler (fn [error]
                          (on-failure error))})
      ;; No API credentials, return nil to trigger fallback
      (on-failure {:status 0 :status-text "No API credentials configured"}))))

;; Main fetch function - try cache first, fallback to API
(defn fetch-races [on-success on-failure]
  (fetch-races-from-cache
    on-success
    (fn [_cache-error]
      ;; Cache failed, try API
      (fetch-races-from-api on-success on-failure))))

;; Register effect handler for Airtable API calls
(rf/reg-fx
  :airtable/fetch-races
  (fn [{:keys [on-success on-failure]}]
    (fetch-races
      #(rf/dispatch (conj on-success %))
      #(rf/dispatch (conj on-failure %)))))

;; Update the events to use this new effect
(rf/reg-event-fx
  :fetch-races-from-airtable
  (fn [{:keys [db]} _]
    {:airtable/fetch-races
     {:on-success [:process-airtable-response]
      :on-failure [:airtable-request-failed]}}))

(rf/reg-event-fx
  :process-airtable-response
  (fn [{:keys [db]} [_ races]]
    {:dispatch [:set-races races]}))

(rf/reg-event-fx
  :airtable-request-failed
  (fn [{:keys [db]} [_ error]]
    (js/console.warn "Airtable API call failed, using fallback data:" error)
    {:dispatch-n [[:set-error "Using cached data. Airtable not connected."]
                  [:set-races (:races db)]]}))
