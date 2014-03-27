(ns sptcg.card-schema
  (:require [clojure.string :as string]
            [clojure.walk :as walk]
            [schema.core :as s])
  #+clj (:require [schema.macros :as sm])
  #+cljs (:require-macros [schema.macros :as sm]))

;; https://github.com/lynaghk/cljx
;; https://github.com/Prismatic/schema

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Schema predicates

(defn at-most [amount] (s/pred (partial >= amount) 'at-most))

(defn count-cards-in-deck [deck-cards]
  (->> deck-cards
       (map #(or (:amount %) 1))
       (apply +)))

(defn total-amount-at-least [amount]
  (s/pred
   #(<= amount (count-cards-in-deck %))
   'total-amount-at-least))

(defn one-or-many [schema]
  (s/if #(set? %) #{schema} schema))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Card Schema

(def colours #{:none :blue :green :red :white})

(def Colours (apply s/enum colours))

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

(def basic-land-types #{:plains :mountain :river :hills :swamp :tundra :forest :mudflats})

;; haven't yet specified the speed and size of basic lands.
;; not sure about their usage?
(def BasicLandTypes (apply s/enum basic-land-types))

(def BasicLand
  (merge Land
         {:sub-type BasicLandTypes}))

(def NonBasicLand
  (merge Land
         {(s/optional-key :sub-type) s/Str}))

(def card-types #{:land :creature :structure :effect :enchantment :equipment})

(def SpellTypes (apply s/enum (filter #(not= :land %) card-types)))

(def Spell
  (merge Card
         {:cost #{Mana}
          :type (one-or-many SpellTypes)
          (s/optional-key :sub-type) s/Str
          (s/optional-key :produces) (one-or-many Mana)}))

(def Cards [(s/either BasicLand NonBasicLand Spell)])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Deck Schema

(def DeckTypes (s/enum :land :spell))

(def BasicLandDeckCard
  {:card BasicLand
   :amount s/Num})

(def maximum-copies
  {:land 4
   :spell 3})

(def NonBasicLandDeckCard
  {:card NonBasicLand
   :amount (s/both s/Num
                   (at-most (maximum-copies :land)))})

(def minimum-deck-size
  {:land 30
   :spell 40})

(def LandDeck
  {:type (s/eq :land)
   :cards (s/both #{(s/either BasicLandDeckCard
                              NonBasicLandDeckCard)}
                  (total-amount-at-least (:land minimum-deck-size)))})

(def SpellDeckCard
  {:card Spell
   :amount (s/both s/Num
                   (at-most (maximum-copies :spell)))})

(def SpellDeck
  {:type (s/eq :spell)
   :cards (s/both #{SpellDeckCard}
                  (total-amount-at-least (:spell minimum-deck-size)))})

(defn deck-type-for-card [card]
  (if (= :land (:type card))
    :land :spell))

(defn card-in-deck? [deck card]
  (->> deck
       (filter #(= (:card %) card))
       seq))

(defn maybe-add-card-to-deck [deck card]
  (prn deck card)
  (let [deck-type (deck-type-for-card card)]
    (if (card-in-deck? (get deck deck-type) card)
      (walk/postwalk
       (fn [elem]
         (if (and (map? elem)
                  (= (:card elem) card)
                  (:amount elem)
                  (> (maximum-copies deck-type) (:amount elem)))
           (update-in elem [:amount] inc)
           elem))
       deck)
      (update-in deck [deck-type] conj {:card card :amount 1}))))

(defn remove-card-from-deck [deck card]
  (let [deck-type (deck-type-for-card card)
        sub-deck (get deck deck-type)]
    (if-let [deck-card (->> sub-deck
                            (filter #(= (:id card) (-> % :card :id)))
                            first)]
      (if (< 1 (:amount deck-card))
        (walk/postwalk
         (fn [elem]
           (if (and (map? elem)
                    (= (:card elem) card))
             (update-in elem [:amount] dec)
             elem))
         deck)
        (walk/postwalk
         (fn [elem]
           (if (= sub-deck elem)
             (->> sub-deck (remove #(= % deck-card)) vec)
             elem))
         deck))
      deck)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Helpers

(defn labelise-enum [enum] (-> enum name string/capitalize))
(def basic-land-type? basic-land-types) ;; alias for readability below

(defn card-type-label [type sub-type]
  (if (= :land type)
    (if sub-type
      (str (if (basic-land-type? sub-type)
             "Basic Land"
             "Land")
           " - "
           (labelise-enum sub-type))
      "Land")
    (str
     (labelise-enum type)
     (when sub-type
       (str " - " (labelise-enum sub-type))))))

(defn deck-card-count-label [type cards]
  (str (count-cards-in-deck cards)
       " of "
       (get minimum-deck-size type) " cards."))

(def colour-order-for-sorting {:none 0 :blue 1 :green 2 :red 3 :white 4})

(defn sort-manas [manas]
  (sort-by #(get colour-order-for-sorting (or (:colour %) :none)) manas))

(defn sorted-enum-list [enums]
  (-> enums
      (conj :all)
      sort))

(defn matches-type?
  [type card]
  (or (= :all type)
      (= (:type card) type)))

(defn matches-colour?
  [colour {:keys [cost produces]}]
  (or (= :all colour)
      (get (->> (concat cost produces)
                (map #(or (:colour %) :none))
                set)
           colour)))
