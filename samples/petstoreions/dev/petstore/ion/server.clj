(ns petstore.ion.server
  "Houses the implementation for running the sample locally as a pedestal service."
  (:require
    [io.pedestal.http :as http]
    ;[com.cognitect.vase.api :as api]
    ))

(defn run-dev
      "Starts a service."
      [port service]
      (println "\nCreating your [DEV] server...")
      (-> service ;; start with the ion configuration
          ;; Remove the ion chain provider
          (dissoc ::http/chain-provider)
          (merge {:env                   :dev
                  ;; do not block thread that starts web server
                  ::http/join?           false
                  ::http/port            port
                  ::http/type            :jetty
                  ;; Routes can be a function that resolve routes,
                  ;;  we can use this to set the routes to be reloadable
                  ;::server/routes          #(route/expand-routes (deref #'ion/routes))
                  ;; all origins are allowed in dev mode
                  ::http/allowed-origins {:creds true :allowed-origins (constantly true)}
                  ;; Content Security Policy (CSP) is mostly turned off in dev mode
                  ::http/secure-headers  {:content-security-policy-settings {:object-src "'none'"}}})
          ;; Wire up interceptor chains

          http/default-interceptors
          http/dev-interceptors
          ;api/execute-startups
          http/create-server
          http/start))

(defn stop
      [s]
      (http/stop s))

(comment



  (http/stop s)


  )