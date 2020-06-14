(ns petstore.ion
  (:require
    [datomic.client.api :as client]
    [fern :as f]
    [io.pedestal.interceptor :as i]
    [petstore.retry :as retry]))

(defrecord LazyCloudConnection [client-config db-name]
  i/IntoInterceptor
  (-interceptor [_]
    (i/map->Interceptor
      {:enter
       (fn [ctx]
         (let
           [client (retry/with-retry #(client/client client-config))
            ;_      (client/create-database client {:db-name db-name})
            cxn (retry/with-retry #(client/connect client {:db-name db-name}))]
           (update ctx :request assoc
                   :client client
                   :conn cxn
                   :db (retry/with-retry #(client/db cxn)))))})))

(defmethod f/literal 'vase.datomic.ion/client [_ client-config db-name]
  (->LazyCloudConnection client-config db-name))