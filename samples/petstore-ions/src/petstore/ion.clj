(ns petstore.ion
  (:require
    [datomic.client.api :as client]
    [fern :as f]
    [io.pedestal.interceptor :as i]))

(defrecord LazyCloudConnection [client-config db-name]
  i/IntoInterceptor
  (-interceptor [_]
    (let [client (client/client client-config)
          _      (client/create-database client {:db-name db-name})
          ]
      (i/map->Interceptor
        {:enter
         (fn [ctx]
           (let [cxn (client/connect client {:db-name db-name})]
             (update ctx :request assoc
                     :client client
                     :conn cxn
                     :db (client/db cxn))))}))))

(defmethod f/literal 'vase.datomic.ion/client [_ client-config db-name]
  (->LazyCloudConnection client-config db-name))