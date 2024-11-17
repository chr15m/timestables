(ns main
  (:require
    [reagent.core :as r]
    [reagent.dom :as rdom]))

(js/console.log "main.cljs loaded")

(def max-number 12)

(def initial-state
  {:completed {}
   :current-problem nil
   :rewards {1 0.10  ; £0.10 for 1-3 times tables
             2 0.10
             3 0.10
             4 0.20  ; £0.20 for 4-6 times tables
             5 0.20
             6 0.20
             7 0.50  ; £0.50 for 7-9 times tables
             8 0.50
             9 0.50
             10 1.00 ; £1.00 for 10-12 times tables
             11 1.00
             12 1.00}})

(def state (r/atom initial-state))

(defn get-completion-key [x y]
  (str x "x" y))

(defn row-completed? [row]
  (every? #(get-in @state [:completed (get-completion-key row %)])
          (range 1 (inc max-number))))

(defn format-currency [amount]
  (str "£" (.toFixed (js/Number. amount) 2)))

(defn toggle-completion [x y]
  (swap! state update-in [:completed (get-completion-key x y)]
         #(if % nil true)))

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
    [:div.header]  ; Empty corner cell
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
         "Row " x ": " (format-currency (get-in @state [:rewards x]))]))]])

(rdom/render [times-table]
             (.getElementById js/document "app"))
