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

