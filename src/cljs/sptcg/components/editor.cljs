(ns sptcg.components.editor
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.reader :as reader]))

(defn handle-change [e cursor owner]
  (let [value (.. e -target -value)]
    (try
      (let [data (reader/read-string value)]
        (if (= (set (keys @cursor)) (set (keys data)))
          (do
            (om/transact! cursor (fn [_] data))
            (om/set-state! owner :value value))
          (om/update-state! owner :value identity)))
      (catch :default ex
        (om/set-state! owner :value value)))))

(defn pr-map-cursor [cursor]
  (pr-str
    (into cljs.core.PersistentHashMap.EMPTY
      (om/value cursor))))

(defn editor [[_ cursor :as original] owner opts]
  (reify
    om/IInitState
    (init-state [_]
      {:value (pr-map-cursor cursor)
       :editing false})
    om/IRenderState
    (render-state [_ {:keys [editing value]}]
      (dom/div #js {:className "editor"}
        (dom/div nil
          (dom/label #js {:className "inspector"} "path:")
          (dom/code nil (pr-str (om/path cursor))))
        (dom/div nil
          (dom/label #js {:className "inspector"} "value:")
          (dom/input
            #js {:className "edit"
                 :value (if editing
                          value
                          (pr-map-cursor cursor))
                 :onFocus (fn [e]
                            (om/set-state! owner :editing true)
                            (om/set-state! owner :value
                              (pr-map-cursor (second (om/get-props owner)))))
                 :onBlur (fn [e] (om/set-state! owner :editing false))
                 :onChange #(handle-change % cursor owner)}))
        (apply om/build* original)))))
