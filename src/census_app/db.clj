(ns census-app.db
  (:require
    [clojure.java.jdbc :as jdbc]
    [clojure.set :as set]
    [environ.core :refer [env]]))


(def db-spec {:classname              "com.microsoft.jdbc.sqlserver.SQLServerDriver"
              :subprotocol            "sqlserver"
              :subname                "//aloha-808.database.windows.net:1433;"
              :database               "census"
              :user                   (env :jdbc-database-username)
              :password               (env :jdbc-database-password)
              :encrypt                true
              :trustServerCertificate false
              :hostNameInCertificate  "*.database.windows.net"
              :loginTimeout 30})

              (defn test-query []
                (jdbc/with-db-connection [connection db-spec]
                                         (jdbc/query connection
                                                     ["SELECT COUNT(*) AS CountyCount
                                          FROM dbo.census_counties"]
                                                     {:result-set-fn first})))

                                      (defn all-states-query []
                                        (jdbc/with-db-connection [connection db-spec]
                                                                 (jdbc/query connection
                                                                             ["SELECT *
                                       FROM dbo.census_counties
                                       WHERE County_State = '' and fips > 1"])))


              (defn counties-by-state-query [state-fips-code]
                (jdbc/with-db-connection [connection db-spec]
                                         (jdbc/query connection
                                                     ["SELECT *
                                       FROM dbo.census_counties
                                       WHERE fips between (? + 1) and (? + 999)"
                                                      state-fips-code state-fips-code])))

                                      (defn population-query [fips-code]
                                        (jdbc/with-db-connection [connection db-spec]
                                                                 (jdbc/query connection
                                                                             ["SELECT POP010210 AS TOTAL_POP,
                                       cd.AGE135212 AS LT5YEARS_PCT,
                                       cd.AGE295212 AS LT18YEARS_PCT,
                                       cd.AGE775212 AS GT65YEARS_PCT,
                                       cd.SEX255212 AS FEMALE_PCT,
                                       cc.County_Name AS COUNTY,
                                       CC.County_State AS STATE
                                       FROM dbo.census_data cd, dbo.census_counties cc
                                       WHERE cd.fips = ?
                                       AND cc.fips = cd.fips"
                                                                              fips-code]
                                                                             {:result-set-fn first})))





              (defn add-19-64-pct [pop-map]
                (let [female (:female_pct pop-map)]
                  (assoc pop-map :lt64years_pct (- 100 (+ (:lt5years_pct pop-map)
                                                          (:lt18years_pct pop-map)
                                                          (:gt65years_pct pop-map))))))

                                      (defn add-male-pct [pop-map]
                                        (let [female (:female_pct pop-map)]
                                          (assoc pop-map :male_pct (- 100 female))))

              (defn state-clean-up [pop-map]
                (if (empty? (:county_name pop-map))
                  (-> pop-map
                      (dissoc :state)
                      (set/rename-keys {:county :state}))))

                                      (defn population-data [fips-code]
                                        (let [results (population-query fips-code)]
                                          (if (empty? results)
                                            results
                                            (-> results
                                                (add-male-pct)
                                                (add-19-64-pct)
                                                (state-clean-up)))))

              (defn all-states []
                (let [results (all-states-query)]
                  (for [state results]
                    {:id    (:fips state)
                     :state (:county_name state)})))

                                      (defn counties-by-state [fips-code]
                                        (let [results (counties-by-state-query fips-code)]
                                          (assoc (population-data fips-code) :counties
                                                                             (for [county results]
                                                                               {:id     (:fips county)
                                                                                :county (:county_name county)}))))


              (defn chart-data [fips-code]
                (let [data (population-data fips-code)
                      all-0-5 (* (:total_pop data) (/ (:lt5years_pct data) 100))
                      female-0-5 (int (* all-0-5 (/ (:female_pct data) 100)))
                      male-0-5 (- (int (- all-0-5 female-0-5)))

                      all-6-18 (* (:total_pop data) (/ (:lt18years_pct data) 100))
                      female-6-18 (int (* all-6-18 (/ (:female_pct data) 100)))
                      male-6-18 (- (int (- all-6-18 female-6-18)))

                      all-65 (* (:total_pop data) (/ (:gt65years_pct data) 100))
                      female-65 (int (* all-65 (/ (:female_pct data) 100)))
                      male-65 (- (int (- all-65 female-65)))

                      all-19-64 (* (:total_pop data)
                                   (/ (- 100 (+ (:lt5years_pct data)
                                                (:lt18years_pct data)
                                                (:gt65years_pct data))) 100))
                      female-19-64 (int (* all-19-64 (/ (:female_pct data) 100)))
                      male-19-64 (- (int (- all-19-64 female-19-64)))

                      location-name (if-not (empty? (:county data))
                                      (str (:county data) ", " (:state data))
                                      (str (:state data)))]
                  {:chart       {:type "bar"}
                   :title       {:text (str "Population pyramid for " location-name)}
                   :subtitle    {:text "Source:  U.S. Census, 2010"}
                   :xAxis       [{:categories ["0-5" "6-18" "19-64" "65+"]
                                  :reversed   false
                                  :labels     {:step 1}}
                                 {:categories ["0-5" "6-18" "19-64" "65+"]
                                  :reversed   false
                                  :opposite   true
                                  :linkTo     0
                                  :labels     {:step 1}}]
                   :yAxis       {:title  {:text ""}
                                 :labels {:formatter ""}}

                   :plotOptions {:series {:stacking "normal"}}
                   :series      [{:name "Male"
                                  :data [male-0-5, male-6-18, male-19-64, male-65]}
                                 {:name "Female"
                                  :data [female-0-5, female-6-18, female-19-64, female-65]}]}))