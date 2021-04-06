(ns short.views
  (:require [hiccup.core :refer [html]]
            [hiccup.page :refer [html5 include-css include-js]]))


(def partial-form-url
  (html
   [:form
    {:onsubmit "return false"}
    [:fieldset
     [:label {:for "nameField"}
      "Paste here your url"]
     [:input {:type "text" :placeholder "https://example.com" :id "url"}]
     [:button {:class "button-primary"
               :onclick "createShort()"}
      "Short!"]]]
   [:div {:id "short-link"} ""]))


(def partial-form-short-url
  (html
   [:form
    {:onsubmit "return false"}
    [:fieldset
     [:label {:for "short-url"}
      "Paste here your short url to view stats"]
     [:input {:type "text"
              :placeholder "https://short.in"
              :name "short-url"
              :id "short-url"}]
     [:button {:class "button-primary"
               :onclick "getShortInfo()"}
      "Get Info"]]]
   [:div {:id "link-info"} "link info"]))


(defn main []
  (html5 {:lang "en-US"}
    [:head
     [:title "Enshort yourself"]
     (include-css "//fonts.googleapis.com/css?family=Roboto:300,300italic,700,700italic")
     (include-css "//cdnjs.cloudflare.com/ajax/libs/normalize/8.0.1/normalize.css")
     (include-css "//cdnjs.cloudflare.com/ajax/libs/milligram/1.4.1/milligram.css")
     (include-js "script.js")]
    [:body
     [:div {:class "container"}
      [:h1 "Shorten your URLs"]
      partial-form-url
      partial-form-short-url]]))
