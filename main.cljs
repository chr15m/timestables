(ns main
  (:require
    [reagent.core :as r]
    [reagent.dom :as rdom]))

(js/console.log "main.cljs loaded")

(def max-number 12)

(def reward-tiers
  {1 0
   2 0
   3 1
   4 2
   5 1
   6 2
   7 2
   8 3
   9 2
   10 0
   11 1
   12 3})

(def rewards
  {0 5
   1 10
   2 15
   3 25})

(def initial-state
  {:completed {}
   :current-problem nil})

(defn load-state []
  (let [stored-state (.getItem js/localStorage "times-table-state")]
    (if stored-state
      (-> stored-state
          js/JSON.parse
          (js->clj :keywordize-keys true))
      initial-state)))

(defonce state (r/atom (load-state)))

(defn save-state []
  (.setItem js/localStorage "times-table-state"
            (-> @state
                clj->js
                js/JSON.stringify)))

(defn get-completion-key [x y]
  (keyword (str x "x" y)))

(defn row-completed? [row]
  (every? #(get-in @state [:completed (get-completion-key row %)])
          (range 1 (inc max-number))))

(defn format-currency [amount]
  (str "£" (.toFixed (js/Number. amount) 2)))

(defn toggle-completion [x y]
  (swap! state update-in [:completed (get-completion-key x y)]
         #(if % nil true))
  (save-state))

(defn generate-problem [row]
  (let [uncompleted (remove #(get-in @state [:completed (get-completion-key row %)])
                            (range 1 (inc max-number)))]
    (when (seq uncompleted)
      (swap! state assoc :current-problem
             {:x row
              :y (rand-nth uncompleted)}))))

(defn problem-display []
  (when-let [{:keys [x y]} (:current-problem @state)]
    [:div.problem
     [:h3 "Practice Problem:"]
     [:p.large (str x " × " y " = ?")]
     [:p.answer {:on-click #(swap! state assoc :current-problem nil)}
      "Click to see answer: " (* x y)]]))

(defn times-table []
  [:div
   [:h1 "Times Tables Tracker"]
   [:div.grid 
    [:div.header]
    (for [x (range 1 (inc max-number))]
      [:div.header {:key (str "header-" x)} x])
    (for [x (range 1 (inc max-number))]
      (cons
        [:div.row {:key (str "row-" x)}
         [:div x]
         [:button.small
          {:on-click #(generate-problem x)
           :disabled (row-completed? x)}
          "?"]]
        (for [y (range 1 (inc max-number))]
          [:div.cell {:key (str x "x" y)}
           [:input {:type "checkbox"
                    :checked (boolean
                               (get-in @state
                                       [:completed (get-completion-key x y)]))
                    :on-change #(toggle-completion x y)}]])))]
   [problem-display]
   [:div.rewards
    [:h3 "Completed Rows:"]
    (for [x (range 1 (inc max-number))]
      (when (row-completed? x)
        [:p {:key (str "reward-" x)}
         "Row " x ": " (format-currency
                         (get rewards
                              (get reward-tiers x)))]))
    [:h3 "Total: "
     (format-currency
       (apply +
              (map (fn [i]
                     (when (row-completed? i)
                       (get rewards
                            (get reward-tiers i))))
                   (range 1 (inc max-number)))))]]])

(rdom/render [times-table]
             (.getElementById js/document "app"))
