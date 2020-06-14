(ns petstore.datomic
  (:require [datomic.client.api :as d]
            [petstore.retry :as retry]))

(def petstore-schema
  [{:db/ident       :pet-store.pet/id
    :db/doc         "The id of a pet"
    :db/unique      :db.unique/identity
    :db/valueType   :db.type/long
    :db/cardinality :db.cardinality/one}
   {:db/ident       :pet-store.pet/name
    :db/doc         "The name of a pet"
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one}
   {:db/ident       :pet-store.pet/tag
    :db/doc         "The tag of a pet"
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one}])

(def seed-data
  [{:pet-store.pet/id   1
    :pet-store.pet/name "Yogi"
    :pet-store.pet/tag  "dog"}
   {:pet-store.pet/id   2
    :pet-store.pet/name "Dante"
    :pet-store.pet/tag  "cat"}])

(defn- has-ident?
  [db ident]
  (contains? (d/pull db {:eid ident :selector [:db/ident]})
             :db/ident))

(defn- fresh-db?
  [db]
  (not (has-ident? db :pet-store.pet/id)))

(defn load-dataset
  "Given `conn`, transacts the petstore schema and seed data if necessary.
  Truthy if changes were applied."
  [conn]
  (let [db (d/db conn)]
    (when (fresh-db? db)
      (let [xact #(d/transact conn {:tx-data %})]
        (xact petstore-schema)
        (xact seed-data)))))

(defn ensure-db
  "Given `client` and `db-name`, ensures the db is created and
  initialized as per the fn which `setup-sym` resolves to. This a unary fn taking
  a Datomic connection."
  ([client db-name setup-sym]
   (let [setup-fn (requiring-resolve setup-sym)
         _        (d/create-database client {:db-name db-name})
         conn     (d/connect client {:db-name db-name})]
     (if setup-fn
       (setup-fn conn)
       (throw (ex-info (format "Unable to resolve %s" setup-sym) {}))))))

(comment
  (ensure-db
    (petstore.service/get-client "rec-prod" "us-east-1")
    "petstore-ions"
    `load-dataset)
  )