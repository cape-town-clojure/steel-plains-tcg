(ns sptcg.model
  (:require [sptcg.card-schema :as card-schema]))

(def cardbase
  #{{:name     "Frozen Lake"
     :size     1
     :type     :land
     :sub-type "Lake"
     :produces #{{:colour :white :amount 1}
                 {:colour :blue :amount 1}}}
    {:name     "Plains"
     :size     1
     :type     :land
     :sub-type :plains
     :produces #{{:colour :white :amount 1}
                 {:colour :green :amount 1}}}
    {:name "Heal"
     :type :effect
     :cost #{{:colour :white :amount 1}}}
    {:name "Grizzly Bear"
     :type :creature
     :sub-type "Bear"
     :cost #{{:amount 1}
             {:colour :green :amount 1}}}
    {:name "Wild Growth"
     :type :enchantment
     :cost #{{:colour :green :amount 3}}}
    {:name "Ballista"
     :type :structure
     :cost #{{:amount 2}
             {:colour :white :amount 2}}}
    {:name "Shield"
     :type :equipment
     :cost #{{:colour :white :amount 1}}}})
