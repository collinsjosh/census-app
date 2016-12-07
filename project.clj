(defproject census-app "1.0.0"
  :description "A sample app for exploring census data"
  :url "http://census-app.azurewebsites.net"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [io.pedestal/pedestal.service "0.5.1"]

                 ;; Remove this line and uncomment one of the next lines to
                 ;; use Immutant or Tomcat instead of Jetty:
                 [io.pedestal/pedestal.jetty "0.5.1"]
                 ;; [io.pedestal/pedestal.immutant "0.5.1"]
                 ;;[io.pedestal/pedestal.tomcat "0.5.1"]

                 [ch.qos.logback/logback-classic "1.1.7" :exclusions [org.slf4j/slf4j-api]]
                 [org.slf4j/jul-to-slf4j "1.7.21"]
                 [org.slf4j/jcl-over-slf4j "1.7.21"]
                 [org.slf4j/log4j-over-slf4j "1.7.21"]
                 [org.clojure/java.jdbc "0.7.0-alpha1"]
                 ;;[com.microsoft.sqlserver/sqljdbc42 "4.2"]
                 [com.microsoft.sqlserver/sqljdbc4 "4.0"]
                 [org.clojure/data.json "0.2.6"]
                 [environ "1.0.0"]]
  :plugins [[ohpauleez/lein-pedestal "0.1.0-beta10"]
            [environ/environ.lein "0.3.1"]]
  :hooks [environ.leiningen.hooks]
  :pedestal {:server-ns "census-app.server"}
  :min-lein-version "2.0.0"
  :resource-paths ["config", "resources"]
  ;; If you use HTTP/2 or ALPN, use the java-agent to pull in the correct alpn-boot dependency
  ;:java-agents [[org.mortbay.jetty.alpn/jetty-alpn-agent "2.0.3"]]
  :profiles {:dev        {:aliases      {"run-dev" ["trampoline" "run" "-m" "census-api.server/run-dev"]}
                          :dependencies [[io.pedestal/pedestal.service-tools "0.5.1"]]
                          :env          {:jdbc-database-username "jcollins"
                                         :jdbc-database-password "Censusd3m0"}}
             :uberjar    {:aot [census-app.server]}
             :production {:env {:production true}}}
  :main ^{:skip-aot true} census-app.server)

