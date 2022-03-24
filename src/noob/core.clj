(ns noob.core
  (:require [org.httpkit.server :as server]
  										[compojure.core :refer :all]
  										[compojure.route :as route]
  										[ring.middleware.defaults :refer :all]
  										[clojure.pprint :as pp]
  										[clojure.string :as str]
  										[clojure.data.json :as json])
  (:gen-class))

; Simple Body page
(defn simple-body-page 
	[req]
	{
		:status 200
		:headers {"Content-Type" "text/html"}
		:body "Hello world."
		})

; request example
(defn requst-example
	[req]
	{
		:status 200
		:headers {"Content-Type" "text/html"}
		:body (->> 
									(pp/pprint req)
									(str "Request Object: " req))
		})

; hello name
(defn requst-hello
	[req]
	{
		:status 200
		:headers {"Content-Type" "text/html"}
		:body (-> 
									(pp/pprint req)
									(str "Hello, " (:name (:params req))))
		})

; atom []
(def people-collections (atom []))
(defn add-person
	[firstname surname]
	(swap! people-collections conj {
		:firstname (str/capitalize firstname)
		:surname (str/capitalize surname)
		}))

(defn add-person-n
	[b]
	(pp/pprint "add n person")
	(pp/pprint b)
	(let [person (json/read-str b :key-fn keyword)]
		(add-person (:firstname person) (:surname person)))
	)

(defn add-person-handler
	[req]
	(pp/pprint "\n\nadd-person-handler")
	(pp/pprint (str "Headers: " (:headers req)))
	(pp/pprint (str "Body: " (:body req)))
	; (pp/pprint (slurp (clojure.java.io/reader (:body req))))
	(add-person-n (slurp (clojure.java.io/reader (:body req))))
	{
		:status 200
		:headers {"Content-Type" "text/json"}
		:body (str (json/write-str @people-collections))
		})

(defn get-person-handler
	[req]
	{
		:status 200
		:headers {"Content-Type" "text/json"}
		:body (str (json/write-str @people-collections))
		})

(defroutes app-routes
  (GET "/" [] simple-body-page)
  (GET "/request" [] requst-example)
  (GET "/hello" [] requst-hello)
  (GET "/get-people" [] get-person-handler)
  (POST "/add-person-handler" [] add-person-handler)
  (route/not-found "Error, page not found."))

(defn -main
  "This is our main entry point"
  [& args]
  (let [port (Integer/parseInt (or (System/getenv "PORT") "3000"))]
  	;Run server with Ring.defaults middleware
  	; site-defaults api-defaults
  	; The site-defaults default configuration adds ant9forgery CSRF protection and any post request which does not include a valid CSRF-token will be blocked. 
  	(server/run-server (wrap-defaults #'app-routes api-defaults) {:port port})

  	;Run server withou ring defaults
  	;(server/run-server #'app-routes {:port port})

  	(println (str "Running server at http://127.0.0.1:" port))))
