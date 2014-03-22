(ns sptcg.schema
  (:use [datomic-schema.schema :only [defpart defschema fields part]])
  (:require
   [datomic.api :as d]
   [datomic-schema.schema :as s]
   [sptcg.card-schema :as card-schema]))

;; Possible types on fields:
;; :keyword :string :boolean :long :bigint :float :double :bigdec :ref :instant :uuid :uri :bytes
;; :enum ["item1" "item2" "item3"]


;; Possible options on fields:
;; :unique-value :unique-identity :indexed :many :fulltext :component :nohistory "Some doc string"
;; [:arbitrary "Enum" :values]

;; See http://docs.datomic.com/schema.html for more detailed information


(defschema player
  (fields
   [name :string :indexed]
   [pwd :string "Hashed password string"]
   [email :string :indexed :unique-value]))

(defschema game
  (fields
   [players :ref :many]
   [winner :ref]
   [size :long]))

(defschema owner
  (fields
   [player :ref]
   [game :ref]))

(defschema position
  (fields
   [x :long]
   [y :long]))

(defschema card
  (fields
   [cardtype :ref]
   [foil :boolean]))

(defschema deck
  (fields
   [name :string :indexed]
   [spells :ref :many]
   [lands :ref :many]))

(defschema cardtype
  (fields
   [name :string :indexed]
   [text :string]
   [type :enum (vec card-schema/card-types)]
   [subtype :string :many]
   [cost :ref :component :many]
   [produce :ref :component :many]
   [attack :long]
   [health :long]
   [deck :ref]
   [size :long]))

(defschema mana
  (fields
   [colour :enum (vec card-schema/colours)]
   [amount :long]))



(comment
  card
  rules
  creatures
  land
  player
  enchantment)


(defn schema []
  (concat
   (s/build-parts d/tempid)
   (s/build-schema d/tempid)))
