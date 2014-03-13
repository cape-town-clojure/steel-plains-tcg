(ns sptcg.util
  (:require [datomic.api :as d]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [sptcg.schema :as schema])
  (:import datomic.Util))

(defn read-all [f]
  (Util/readAll (io/reader f)))

(defn transact-all [conn f]
  (doseq [txd (read-all f)]
    (d/transact conn txd))
  :done)

(defn create-db [uri]
  (d/create-database uri))

(defn load-schema [conn]
  (d/transact conn (schema/schema)))

(defn load-data [conn]
  (transact-all conn (io/resource "data/initial.edn")))

(defn init-db [uri]
  (create-db uri)
  (let [conn (d/connect uri)]
    (load-schema conn)
    (load-data conn)
    conn))
