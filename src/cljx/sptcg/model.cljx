(ns sptcg.model
  (:require [schema.core :as s])
  #+clj (:require [schema.macros :as sm])
  #+cljs (:require-macros [schema.macros :as sm]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Cards

(def Factions (s/enum :none :green :blue :red :white))

(def Faction
  {(s/optional-key :faction) (s/either Factions #{Factions})})

(def Mana
  (merge Faction {:amount s/Int}))

(def CardTypes (s/enum :creature :structure :land :effect :enchantment :equipment))

(def Card
  (merge Faction
         {:name s/Str
          :type (s/either CardTypes #{CardTypes})
          (s/optional-key :sub-type) s/Keyword}))

(def LandSizes (s/enum 1 2 3 4 5))

(def Land
  (merge Card
         {:size LandSizes
          :produces (s/either Mana #{Mana})}))

;; haven't yet specified the speed and size of basic lands.
;; not sure about their usage?
(def BasicLandTypes (s/enum :plains :mountain :river :hills :swamp :tundra :forest :mudflats))

(def BasicLand
  (merge Land
         {:sub-type BasicLandTypes}))

(def NonBasicLand
  (merge Land
         {:sub-type (s/pred (complement BasicLandTypes))}))

(def Spell
  (merge Card
         {:cost [Mana]}))

(def Cards [(s/both Land Spell)])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Decks

(def DeckTypes (s/enum :land :spell))

(def Deck
  {:type DeckTypes})

(defn count-at-least [n] (comp (partial < n) count))
(defn count-at-most [n]  (comp (partial > n) count))

(def BasicLandDeckCard
  {:card BasicLand
   :amount s/Num})

(def NonBasicLandDeckCard
  {:card NonBasicLand
   :amount (s/both s/Num
                   (s/pred (count-at-most 4)))})

(def SpellDeckCard
  {:card Spell
   :amount (s/both s/Num
                   (s/pred (count-at-most 3)))})

(def LandDeck
  (merge Deck
         {:cards (s/both [(s/either BasicLandDeckCard NonBasicLandDeckCard)]
                         (s/pred (count-at-least 30)))}))

(def SpellDeck
  (merge Deck
         {:cards (s/both [SpellDeckCard]
                         (s/pred (count-at-least 40)))}))
