(ns census-app.service
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.log :as log]
            [io.pedestal.http.body-params :as body-params]
            [ring.util.response :as ring-resp]
            [census-app.db :as db]))


(defn response [status body]
  {:status status :body body})

(def ok (partial response 200))

(def response-code
  {:name :response-code
   :leave
         (fn [context]
           (if-let [item (:result context)]
             (assoc context :response (response 200 item))
             context))})

(def counties-by-state
  {:name :counties-by-state
   :enter
         (fn [context]
           (if-let [db-id (get-in context [:request :path-params :state])]
             (if-let [the-list (db/counties-by-state db-id)]
               (assoc context :result the-list)
               context)
             context))})

(def all-states
  {:name :all-states
   :enter
         (fn [context]
           (if-let [the-list (db/all-states)]
             (assoc context :result the-list)
             context))})


(def location-data
  {:name :location-data
   :enter
         (fn [context]
           (if-let [db-id (get-in context [:request :path-params :county])]
             (if-let [the-list (db/population-data db-id)]
               (assoc context :result the-list)
               context)
             context))})




(def inspector
  {:name  :inspector
   :enter (fn [context] (do (log/info :db-id (get-in context [:request :path-params :id])) context))
   :leave (fn [context] (do (log/info :result (keys context)) context))})

(def interceptor-template
  {:name :interceptor-template
   :enter (fn [context] ())
   :leave (fn [context] ())})


;; Defines "/" and "/about" routes with their associated :get handlers.
;; The interceptors defined after the verb map (e.g., {:get home-page}
;; apply to / and its children (/about).
(def common-interceptors [(body-params/body-params) http/html-body])

;; Tabular routes
(def routes #{["/locations" :get [http/json-body response-code all-states]]
              ["/locations/:state" :get [http/json-body response-code counties-by-state]]
              ["/locations/:state/:county" :get [http/json-body response-code location-data]]})




;; Consumed by census-api.server/create-server
;; See http/default-interceptors for additional options you can configure
(def service {:env :prod
              ;; You can bring your own non-default interceptors. Make
              ;; sure you include routing and set it up right for
              ;; dev-mode. If you do, many other keys for configuring
              ;; default interceptors will be ignored.
              ;; ::http/interceptors []
              ::http/routes routes

              ;; Uncomment next line to enable CORS support, add
              ;; string(s) specifying scheme, host and port for
              ;; allowed source(s):
              ;;
              ;; "http://localhost:8080"
              ;;
              ;;::http/allowed-origins ["scheme://host:port"]

              ;; Root for resource interceptor that is available by default.
              ::http/resource-path "/public"

              ;; Either :jetty, :immutant or :tomcat (see comments in project.clj)
              ::http/type :tomcat
              ;;::http/host "localhost"
              ::http/port 80
              ;; Options to pass to the container (Jetty)
              ::http/container-options {:h2c? true
                                        :h2? false
                                        ;:keystore "test/hp/keystore.jks"
                                        ;:key-password "password"
                                        ;:ssl-port 8443
                                        :ssl? false}})

