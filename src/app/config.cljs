(ns app.config)

;; Airtable Configuration
;; Credentials loaded from environment variables at build time

;; Use goog.define to allow values to be set via closure-defines
(goog-define AIRTABLE_API_KEY "")
(goog-define AIRTABLE_BASE_ID "")

(def airtable-config
  {:api-key AIRTABLE_API_KEY
   :base-id AIRTABLE_BASE_ID
   :table-name "Races"})

;; Helper to get config value
(defn get-config [key]
  (get airtable-config key))
