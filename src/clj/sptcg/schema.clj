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

(def basic-land-types #{:plains :mountain :river :hills :swamp :tundra :forest :mudflats})
(def card-types #{:land :creature :structure :effect :enchantment :equipment})

;; The user is a person that owns cards
(defschema user
  (fields
   [name :string :indexed]
   [pwd :string "Hashed password string"]
   [email :string :indexed :unique-value]))

;; The Player represents a player in a single given game
(defschema player
  (fields
   [user :ref]
   [hand :ref :many]
   [land-deck :ref :many]
   [spell-deck :ref :many]))

(defschema game
  (fields
   [players :ref :many]
   [winner :ref]
   [size :long]))

(defschema owner
  (fields
   [user :ref]
   [player :ref]
   [game :ref]))

;; Entities are entities in a given game.
(defschema entity
  (fields
   [x :long]
   [y :long]
   [health :long]
   [attack :long]
   [card :ref "The card used to create this entity, if any"]
   [game :ref "The game this entity is in"]))

;; An individual card, owned by a user (not in a game)
(defschema card
  (fields
   [cardtype :ref]
   [foil :boolean]))

;; A deck owned by a user, refers to specific cards, not carddefs
(defschema deck
  (fields
   [name :string :indexed]
   [spells :ref :many]
   [lands :ref :many]))

;; A definition of a card
(defschema carddef
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

;; A production or cost entry for a card.
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
