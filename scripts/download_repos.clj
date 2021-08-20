#!/usr/bin/env bb
(require '[babashka.curl :as curl]
         '[cheshire.core :as json])

(use 'clojure.pprint)

(def config
  (edn/read-string (slurp "config.edn")))

(def username (:github-username config))
(def user-auth-token (:auth-token config))
(def org-name (:organization-name config))

(def request-args
  {:basic-auth [username user-auth-token]
   :headers {"Accept" "application/vnd.github.v3+json"}})

;(def token-check-resp
  ;(curl/get "https://api.github.com/user" request-args)) )

(def all-repos-url
  (str "https://api.github.com/orgs/" org-name "/repos"))


; For debugging
(defn parse-response [response]
  (let [body (json/parse-string (:body response) true)
         status (:status response)
         headers (:headers response)]
    (prn "RESPONSE ------------")
    (pprint status)
    ;(pprint headers)
    ;(pprint (first body))
    (prn "--------------------")
    body))


(defn request-repo-page [page]
  (-> (curl/request {:basic-auth [username user-auth-token]
                     :headers {"Accept" "application/vnd.github.v3+json"}
                     :url {:scheme "https"
                           :host   "api.github.com"
                           :port   443
                           :path   (str "/orgs/" org-name "/repos")
                           :method "get"
                           :query  (str "per_page=100&page=" page)}})
      parse-response))

(def page-range (map inc (range 12)))
(defn all-repos [] (map request-repo-page page-range))


;; Store the repo data so I don't have to run this again
(spit "output/repos-full.edn" (pr-str (flatten (all-repos))))

; Just the one repo actually
;(defn request-repo-page [repo-name]
  ;(-> (curl/request {:basic-auth [username user-auth-token]
                     ;:headers {"Accept" "application/vnd.github.v3+json"}
                     ;:url {:scheme "https"
                           ;:host   "api.github.com"
                           ;:port   443
                           ;:path   (str "/repos/" username "/" repo-name)
                           ;:method "get"}})
      ;parse-response))


;(spit "output/repos-single.edn" (pr-str (request-repo-page "ring-app-example" )))

