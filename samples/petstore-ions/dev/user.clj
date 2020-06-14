(ns user
  (:require
    [datomic.client.api :as d]
    [petstore.ion.server :as server]
    [clojure.tools.namespace.repl :refer (refresh)]
    [petstore.vase :as p.vase]
    [clojure.repl :refer :all]
    [clojure.spec.alpha :as s]
    [clojure.spec.gen.alpha :as sgen]
    [clojure.spec.test.alpha :as stest]
    [io.pedestal.http :as http]
    [com.cognitect.vase.api :as api]))

(stest/instrument)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Initialization
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def vase-server nil)

(defn start []
  (alter-var-root #'vase-server
                  (fn [& args] (server/run-dev 9090 (p.vase/vase-service-map)))))


(defn stop []
  (alter-var-root #'vase-server
                  (fn [s] (when s (server/stop s)))))

(defn reset []
  (stop)
  (refresh :after 'user/start))
