(ns sptcg.deck-builder.card
  (:require-macros [cljs.core.async.macros :as asyncm :refer (go go-loop alt!)])
  (:require [cljs.core.async :as async :refer (<! >! put! chan)]
            [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [sptcg.card-schema :as card-schema]
            [sptcg.model :as model]))

(defn mana [{:keys [amount colour]} owner]
  (reify
    om/IDisplayName (display-name [_] "Mana")
    om/IRenderState
    (render-state [_ state]
      (html
       [:span
        (if colour
          (for [i (range amount)]
            [:span {:class colour} "@"])
          [:span amount])]))))

(defn manas [data owner]
  (reify
    om/IDisplayName (display-name [_] "Manas")
    om/IRenderState
    (render-state [_ state]
      (html
       [:span.mana
        (if (set? data)
          (om/build-all mana (card-schema/sort-manas data))
          (om/build mana data))]))))

(defn land [{:keys [size sub-type produces]} owner]
  (reify
    om/IDisplayName (display-name [_] "LandCard")
    om/IRenderState
    (render-state [_ state]
      (html
       [:div.land
        [:p (card-schema/card-type-label :land sub-type)]
        [:hr]
        [:p "Size: " size]
        (when produces
          [:p "Produces: " (om/build manas produces)])]))))

(defn spell [{:keys [type cost sub-type produces]} owner]
  (reify
    om/IDisplayName (display-name [_] "SpellCard")
    om/IRenderState
    (render-state [_ state]
      (html
       [:div.spell
        [:p (card-schema/card-type-label type sub-type)]
        [:hr]
        [:p "Cost: " (om/build manas cost)]
        (when produces
          [:p "Produces: " (om/build manas produces)])]))))

(defn hidden [^boolean bool]
  (if bool
    #js {:display "none"}
    #js {:display "block"}))

(defn card [{:keys [hidden? name type id] :as data}
            owner
            {:keys [class action-label] :as opts}]
  (reify
    om/IDisplayName (display-name [_] "Card")
    om/IRenderState
    (render-state [_ {:keys [control-chan hovered? copy?]}]
      (html
       [:div.card
        (cond-> {:class class
                 :style (hidden hidden?)
                 :onMouseOut #(om/set-state! owner :hovered? false)
                 :onMouseOver #(om/set-state! owner :hovered? true)}
                hovered? (assoc :onClick #(put! control-chan [:action-card id])))
        (when-not copy?
          (list
           [:h3 name]
           (om/build (if (= :land type) land spell) data)
           (when hovered?
             [:button.pure-button
              [:small "Click to " action-label]])))]))))

(defn deck-card [{card* :card amount :amount hidden? :hidden? :as data} owner opts]
  (reify
    om/IDisplayName (display-name [_] "DeckCard")
    om/IRenderState
    (render-state [_ {:keys [control-chan]}]
      (html
       [:div.deck-card
        {:style (hidden hidden?)}
        (for [i (range amount)]
          (om/build card
                    card*
                    {:init-state {:control-chan control-chan
                                  :copy? (not (zero? i))}
                     :opts (assoc opts :class (str "card" i))}))]))))

(defn card-list [data owner {:keys [item-component display-name] :as opts}]
  (reify
    om/IDisplayName (display-name [_] (str display-name "CardList"))
    om/IRenderState
    (render-state [_ {:keys [control-chan]}]
      (html
       [:div.card-list
        (if (seq data)
          (om/build-all item-component data
                        {:init-state {:control-chan control-chan}
                         :opts (select-keys opts [:action-label])})
          "No cards.")]))))

