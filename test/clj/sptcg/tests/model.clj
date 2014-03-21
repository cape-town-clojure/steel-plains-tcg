(ns sptcg.tests.model
  (:use expectations)
  (:require [schema.core :as s]
            [schema.macros :as sm]
            [sptcg.model :refer :all]
            [sptcg.tests.core :refer [expect-schema]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Cards

;;; Colours

(expect-schema Colours :none)

;;; Mana

(expect-schema Mana {:amount 0})
(expect-schema Mana {:colour :red :amount 5})
(expect-schema Mana {:colour #{:green :red} :amount 3})

;; Cards

(expect-schema Card {:name "Heal"})

;; Lands

(expect-schema Land {:name "Frozen Lake"
                     :size 1
                     :type :land})
(expect-schema Land {:name     "Frozen Lake"
                     :size     1
                     :type     :land
                     :produces {:colour :white :amount 1}})

(expect-schema BasicLand {:name     "Plains"
                          :size     1
                          :type     :land
                          :sub-type :plains})


(expect-schema NonBasicLand {:name     "Frozen Lake"
                             :size     1
                             :type     :land
                             :sub-type "Lake"})

;; Spells

(expect-schema Spell {:name "Heal"
                      :type :effect
                      :cost [{:colour :white :amount 1}]})
(expect-schema Spell {:name "Grizzly Bear"
                      :type :creature
                      :sub-type "Bear"
                      :cost [{:amount 1}
                             {:colour :green :amount 1}]})

;; Cards

(expect-schema Cards [{:name     "Plains"
                       :size     1
                       :type     :land
                       :sub-type :plains}
                      {:name     "Frozen Lake"
                       :size     1
                       :type     :land
                       :sub-type "Lake"}{:name "Heal"
                       :type :effect
                       :cost [{:colour :white :amount 1}]}
                      {:name "Grizzly Bear"
                       :type :creature
                       :sub-type "Bear"
                       :cost [{:amount 1}
                              {:colour :green :amount 1}]}])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Decks

(def plains      {:name     "Plains"
                  :size     1
                  :type     :land
                  :sub-type :plains})
(def frozen-lake {:name     "Frozen Lake"
                  :size     1
                  :type     :land
                  :produces {:colour :white :amount 1}})

(expect-schema BasicLandDeckCard {:card plains
                                  :amount 8})

(expect-schema NonBasicLandDeckCard {:card frozen-lake
                                     :amount 4})

(expect-schema LandDeck {:type :land
                         :cards [{:card plains :amount 26}
                                 {:card frozen-lake :amount 4}]})

(def creatures    [{:name     "Grizzly Bear"
                    :type     :creature
                    :sub-type "Bear"
                    :cost     [{:amount 1}
                               {:colour :green :amount 1}]}
                   {:name     "Grizzly Bear"
                    :type     :creature
                    :sub-type "Bear"
                    :cost     [{:amount 1}
                               {:colour :green :amount 1}]}
                   {:name     "Grizzly Bear"
                    :type     :creature
                    :sub-type "Bear"
                    :cost     [{:amount 1}
                               {:colour :green :amount 1}]}])
(def effects      [{:name "Heal"
                    :type :effect
                    :cost [{:colour :white :amount 1}]}
                   {:name "Heal"
                    :type :effect
                    :cost [{:colour :white :amount 1}]}
                   {:name "Heal"
                    :type :effect
                    :cost [{:colour :white :amount 1}]}])
(def enchantments [{:name "Wild Growth"
                    :type :enchantment
                    :cost [{:colour :green :amount 3}]}
                   {:name "Wild Growth"
                    :type :enchantment
                    :cost [{:colour :green :amount 3}]}
                   {:name "Wild Growth"
                    :type :enchantment
                    :cost [{:colour :green :amount 3}]}])
(def structures   [{:name "Ballista"
                    :type :structure
                    :cost [{:amount 2}
                           {:colour :white :amount 2}]}
                   {:name "Ballista"
                    :type :structure
                    :cost [{:amount 2}
                           {:colour :white :amount 2}]}
                   {:name "Ballista"
                    :type :structure
                    :cost [{:amount 2}
                           {:colour :white :amount 2}]}])
(def equipments   [{:name "Shield"
                    :type :equipment
                    :cost [{:colour :white :amount 1}]}
                   {:name "Shield"
                    :type :equipment
                    :cost [{:colour :white :amount 1}]}
                   {:name "Shield"
                    :type :equipment
                    :cost [{:colour :white :amount 1}]}])

(expect-schema SpellDeckCard {:card   (first creatures)
                              :amount 3})

(expect-schema SpellDeck {:type :spell
                          :cards (->> (concat creatures
                                              effects
                                              enchantments
                                              structures
                                              equipments)
                                      (map (fn [card] {:card card :amount 3})))})
