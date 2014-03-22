(ns sptcg.model
  (:require [schema.core :as s])
  #+clj (:require [schema.macros :as sm])
  #+cljs (:require-macros [schema.macros :as sm]))

(defn at-most [amount] (s/pred (partial >= amount) 'at-most))

(defn total-amount-at-least [amount]
  (s/pred
   (fn [deck-cards]
     (->> deck-cards
          (map #(or (:amount %) 1))
          (apply +)
          (<= amount)))
   'total-amount-at-least))

(defn one-or-many [schema]
  (s/if #(set? %) #{schema} schema))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Cards

(def Colours (s/enum :none :green :blue :red :white))

(def Mana
  "Can have no colour at all, a single colour, or any amount of
   colours. More than one colour makes this mana 'hybrid' mana.
   A lack of a colour is the same as providing the :none colour."
  {(s/optional-key :colour) (one-or-many Colours)
   :amount s/Int})

(def Card
  {:name s/Str})

(def LandSizes (s/enum 1 2 3 4 5))

(def Land
  (merge Card
         {:type (s/eq :land)
          :size LandSizes
          (s/optional-key :produces) (one-or-many Mana)}))

;; haven't yet specified the speed and size of basic lands.
;; not sure about their usage?
(def BasicLandTypes (s/enum :plains :mountain :river :hills :swamp :tundra :forest :mudflats))

(def BasicLand
  (merge Land
         {:sub-type BasicLandTypes}))

(def NonBasicLand
  (merge Land
         {(s/optional-key :sub-type) s/Str}))

(def SpellTypes (s/enum :creature :structure :effect :enchantment :equipment))

(def Spell
  (merge Card
         {:cost [Mana]
          :type (one-or-many SpellTypes)
          (s/optional-key :sub-type) s/Str
          (s/optional-key :produces) (one-or-many Mana)}))

(def Cards [(s/either BasicLand NonBasicLand Spell)])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Decks

(def DeckTypes (s/enum :land :spell))

(def BasicLandDeckCard
  {:card BasicLand
   :amount s/Num})

(def NonBasicLandDeckCard
  {:card NonBasicLand
   :amount (s/both s/Num
                   (at-most 4))})

(def LandDeck
  {:type (s/eq :land)
   :cards (s/both [(s/either BasicLandDeckCard
                             NonBasicLandDeckCard)]
                  (total-amount-at-least 30))})

(def SpellDeckCard
  {:card Spell
   :amount (s/both s/Num
                   (at-most 3))})

(def SpellDeck
  {:type (s/eq :spell)
   :cards (s/both [SpellDeckCard]
                  (total-amount-at-least 40))})

