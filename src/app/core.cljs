(ns app.core
  (:require [reagent.dom :as rdom]
            [re-frame.core :as rf]
            [app.model]
            [app.events]
            [app.airtable]
            [app.pages.results :refer [results-page]]))

(defn ^:dev/after-load mount-root []
  (rf/clear-subscription-cache!)
  (rdom/render [results-page]
               (.getElementById js/document "app")))

(defn init []
  (rf/dispatch-sync [:initialize-db])
  (rf/dispatch [:fetch-races])
  (rf/dispatch [:start-auto-refresh])
  (mount-root))

(defn reload []
  (mount-root))

(defn ^:export main []
  (init))
