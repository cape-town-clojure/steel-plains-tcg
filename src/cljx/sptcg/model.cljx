(ns sptcg.model
  (:require [schema.core :as s])
  #+clj (:require [schema.macros :as sm])
  #+cljs (:require-macros [schema.macros :as sm]))

(defn is? [v] (s/pred #(= v %)))

(defn at-most [n] (partial >= n))

(defn total-amount-at-least [n]
  (fn [cards]
    (->> cards
         (map (fn [card]
                (or (:amount card) 1)))
         (apply +)
         (<= n))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Cards

(def Colours (s/enum :none :green :blue :red :white))

(def Mana
  {(s/optional-key :colour) (s/either Colours #{Colours})
   :amount s/Int})

(def Card
  {:name s/Str})

(def LandSizes (s/enum 1 2 3 4 5))

(def Land
  (merge Card
         {:type (is? :land)
          :size LandSizes
          (s/optional-key :produces) (s/either Mana #{Mana})}))

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
          :type (s/either SpellTypes #{SpellTypes})
          (s/optional-key :sub-type) s/Str}))

(def Cards [(s/both Land Spell)])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Decks

(def DeckTypes (s/enum :land :spell))

(def BasicLandDeckCard
  {:card BasicLand
   :amount s/Num})

(def NonBasicLandDeckCard
  {:card NonBasicLand
   :amount (s/both s/Num
                   (s/pred (at-most 4)))})

(def LandDeck
  {:type (is? :land)
   :cards (s/both [(s/either BasicLandDeckCard NonBasicLandDeckCard)]
                  (s/pred (total-amount-at-least 30)))})

(def SpellDeckCard
  {:card Spell
   :amount (s/both s/Num
                   (s/pred (at-most 3)))})

(def SpellDeck
  {:type (is? :spell)
   :cards (s/both [SpellDeckCard]
                  (s/pred (total-amount-at-least 40)))})

