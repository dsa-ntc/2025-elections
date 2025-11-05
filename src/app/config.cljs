(ns app.config)

;; Airtable Configuration
;; Credentials loaded from environment variables at build time

;; Use goog.define to allow values to be set via closure-defines
(goog-define AIRTABLE_API_KEY "")
(goog-define AIRTABLE_BASE_ID "")
(goog-define R2_DATA_URL "")

(def airtable-config
  {:api-key AIRTABLE_API_KEY
   :base-id AIRTABLE_BASE_ID
   :table-name "Races"})

;; R2 configuration
(def r2-config
  {:data-url (if (not-empty R2_DATA_URL)
               R2_DATA_URL
               "/data.json")}) ; Fallback to local file in dev

;; Helper to get config value
(defn get-config [key]
  (get airtable-config key))

(defn get-r2-url []
  (:data-url r2-config))
