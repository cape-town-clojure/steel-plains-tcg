(ns sptcg.tests.model
  (:use expectations)
  (:require [schema.core :as s]
            [schema.macros :as sm]
            [sptcg.model :refer :all]
            [sptcg.tests.core :refer [expect-schema]]))

;;; Factions

(expect-schema Factions :none)

(expect-schema Faction {})
(expect-schema Faction {:faction :none})
(expect-schema Faction {:faction #{:green :red}})

;;; Mana

(expect-schema Mana {:amount 0})
(expect-schema Mana {:faction :red :amount 5})

;; Cards

(expect-schema CardTypes :land)

(expect-schema Card {:name "Heal" :type :effect})
(expect-schema Card {:name "Grizzly Bear"
                     :type :creature
                     :faction :green
                     :sub-type "Bear"})

;; Lands

(expect-schema Land {:name "Frozen Lake"
                     :size 1
                     :type :land})
(expect-schema Land {:name "Frozen Lake"
                     :size 1
                     :type :land
                     :produces {:faction :white :amount 1}})
