(ns sptcg.schema
  (:use [datomic-schema.schema :only [defpart defschema fields part]])
  (:require [datomic.api :as d])
  (:require [datomic-schema.schema :as s]))

;; Possible types on fields:
;; :keyword :string :boolean :long :bigint :float :double :bigdec :ref :instant :uuid :uri :bytes
;; :enum ["item1" "item2" "item3"]


;; Possible options on fields:
;; :unique-value :unique-identity :indexed :many :fulltext :component :nohistory "Some doc string"
;; [:arbitrary "Enum" :values]

;; See http://docs.datomic.com/schema.html for more detailed information

(defschema user
  (fields
   [username :string :indexed]
   [pwd :string "Hashed password string"]
   [email :string :indexed]))

(defn schema []
  (concat
   (s/build-parts d/tempid)
   (s/build-schema d/tempid)))
