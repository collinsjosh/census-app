(ns census-app.server
  (:gen-class)
  (:require [io.pedestal.http :as server]
            [io.pedestal.http.route :as route]
            [census-app.service :as service])
  (:import (java.net Authenticator PasswordAuthentication)))

;; This is an adapted service map, that can be started and stopped
;; From the REPL you can call server/start and server/stop on this service
(defonce runnable-service (server/create-server service/service))


;; I'm using a SOCKS Proxy for Heroku to present a consistent IP Address to
;; the Azure Firewall.
(System/setProperty "socksProxyHost" "speedway.usefixie.com")
(System/setProperty "socksProxyPort" "1080")

(java.net.Authenticator/setDefault
  (proxy [java.net.Authenticator] []
    (getPasswordAuthentication []
      (PasswordAuthentication.
        "fixie" (.toCharArray "czRD3RvJBTadGBt")))))


(defn run-dev
  "The entry-point for 'lein run-dev'"
  [& args]
  (println "\nCreating your [DEV] server...")
  (-> service/service             ;; start with production configuration
      (merge {:env   :dev
              ;; do not block thread that starts web server
              ::server/join?           false
              ;; Routes can be a function that resolve routes,
              ;;  we can use this to set the routes to be reloadable
              ::server/routes          #(route/expand-routes (deref #'service/routes))
              ;; all origins are allowed in dev mode
              ::server/allowed-origins {:creds true :allowed-origins (constantly true)}})
      ;; Wire up interceptor chains
      server/default-interceptors
      server/dev-interceptors
      server/create-server
      server/start))

(defn -main
  "The entry-point for 'lein run'"
  [& args]
  (println "\nCreating your server...")
  (server/start runnable-service))


;; If you package the service up as a WAR,
;; some form of the following function sections is required (for io.pedestal.servlet.ClojureVarServlet).

;;(defonce servlet  (atom nil))

(comment (defn servlet-init
           [_ config]
           ;; Initialize your app here.
           (reset! servlet (server/servlet-init service/service nil))
           )

         (defn servlet-service
           [_ request response]
           (server/servlet-service @servlet request response))

         (defn servlet-destroy
           [_]
           (server/servlet-destroy @servlet)
           (reset! servlet nil)))