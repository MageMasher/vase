(ns petstore.vase
  (:require
    [clojure.java.io :as io]
    [com.cognitect.vase.api :as api]
    [com.cognitect.vase.fern :as fern :refer [load-from-file prepare-service]]
    [com.cognitect.vase.try :as try :refer [try->]]
    [datomic.client.api :as client]
    [datomic.ion.lambda.api-gateway :as apigw]
    [fern :as f]
    [fern.easy :as fe]
    [io.pedestal.interceptor.chain :as chain]
    [io.pedestal.http :as http]
    [io.pedestal.interceptor :as i]
    [io.pedestal.ions :as provider]
    [petstore.service :as service]
    [petstore.ion :as ion])
  (:import (clojure.lang IObj)))




;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

; Preload interceptors available to all
(def stock-interceptor-syms
  '[
    io.pedestal.http.route/path-params-decoder
    io.pedestal.ions/ion-provider
    ])

;; TODO - attach metadata to the val for nice errors
;; Should indicate it is a builtin
(defn expose-sym
  [s]
  {:pre [(symbol? s) (resolve s)]}
  (let [var (resolve s)
        val (var-get var)]
    (if (instance? IObj val)
      {s (with-meta val (meta var))}
      {s val})))

(defn- expose-as-env
  [syms]
  (reduce merge {} (map expose-sym syms)))

(defn fern->service-map
  [filename & {:as opts}]
  (try->
    filename
    load-from-file
    (:! java.io.IOException ioe (fe/print-other-exception ioe filename))


    (merge (expose-as-env stock-interceptor-syms))

    prepare-service
    (:! Throwable t (fe/print-evaluation-exception t))))

(defn vase-service-map
  []
  (fern->service-map (io/resource "vase/petstore.fern")))

(defn handler*
  "Ion handler"
  [service-map]
  (-> service-map
      (assoc :io.pedestal.http/chain-provider provider/ion-provider )
      api/execute-startups
      http/default-interceptors
      http/create-provider))

(def handler (handler* service/service
                       ;(vase-service-map)
                       ))

(def app (apigw/ionize handler))
